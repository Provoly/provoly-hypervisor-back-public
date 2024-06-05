package com.provoly.event;

public enum Category {
    MANIFESTATION(EventType.OPERATOR),
    OPERATOR(EventType.OPERATOR),
    REPORT(EventType.REPORT),
    LIMIT(EventType.ALERT),
    MALFUNCTION(EventType.ALERT);

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
