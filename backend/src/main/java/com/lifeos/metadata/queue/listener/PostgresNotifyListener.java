package com.lifeos.metadata.queue.listener;

import com.lifeos.metadata.queue.worker.MetadataEnrichmentWorker;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresNotifyListener implements CommandLineRunner {

    private final DataSource dataSource;
    private final MetadataEnrichmentWorker enrichmentWorker;
    
    private volatile boolean isRunning = true;
    private Thread listenerThread;

    @Override
    public void run(String... args) {
        log.info("Starting Postgres LISTEN/NOTIFY daemon for metadata_enrichment_channel...");
        listenerThread = new Thread(this::listenLoop, "PgNotifyListener-Thread");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void listenLoop() {
        while (isRunning) {
            try (Connection conn = dataSource.getConnection()) {
                // Unwrap the Postgres connection safely
                PGConnection pgConn = conn.unwrap(PGConnection.class);

                // Issue LISTEN command
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("LISTEN metadata_enrichment_channel");
                }

                log.info("Successfully bound to metadata_enrichment_channel. Waiting for NOTIFY events...");

                while (isRunning && !conn.isClosed()) {
                    PGNotification[] notifications = pgConn.getNotifications(1000); // Wait up to 1s
                    
                    if (notifications != null && notifications.length > 0) {
                        for (PGNotification notification : notifications) {
                            log.debug("Received NOTIFY on '{}' with payload: {}", 
                                    notification.getName(), notification.getParameter());
                        }
                        
                        // We received notifications. Wake up the worker!
                        log.info("Waking up MetadataEnrichmentWorker to process pending jobs...");
                        enrichmentWorker.processPendingJobs();
                    }
                }
            } catch (Exception e) {
                if (isRunning) {
                    log.error("Error in Postgres LISTEN/NOTIFY loop. Retrying in 5 seconds...", e);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    @PreDestroy
    public void stop() {
        isRunning = false;
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
    }
}
