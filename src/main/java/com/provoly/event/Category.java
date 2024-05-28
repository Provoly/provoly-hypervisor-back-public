package com.provoly.event;

public enum Category {
    MANIFESTATION(EventType.OPERATOR),
    OPERATOR_EVENT(EventType.OPERATOR),
    REPORT(EventType.REPORT),
    ALERT_LIMIT(EventType.ALERT),
    ALERT_MALFUNCTION(EventType.ALERT);

    private final EventType eventType;

    Category(EventType eventType) {
        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }

    public static Category fromString(String category) {
        return category == null ? null : valueOf(category);
    }
}
