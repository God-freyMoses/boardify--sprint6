package com.shaper.server.model.enums;

public enum NotificationType {
    TASK_COMPLETED("Task Completed"),
    DOCUMENT_SIGNED("Document Signed"),
    SIGNATURE_REQUEST("Signature Request"),
    REMINDER("Reminder"),
    OVERDUE_TASK("Overdue Task"),
    ONBOARDING_STARTED("Onboarding Started"),
    ONBOARDING_COMPLETED("Onboarding Completed"),
    GENERAL("General");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}