package com.lifeos.shared.service;

import java.util.Map;

public interface DiscordAlertService {
    boolean isConfigured();
    void sendSyncAlert(String deviceId, int sessions, int logs);
    void sendOfflineWarning(String deviceId);
    void sendErrorLogAlert(String deviceId, String tag, String errorMessage);
    void sendDailySummary(Map<String, String> activitySummary, String dateLabel);
}
