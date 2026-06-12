package com.lifeos.shared.service;

public interface CleanupService {
    void cleanupOldLogs();
    void cleanupOldMetrics();
}
