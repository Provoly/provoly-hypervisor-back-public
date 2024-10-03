package com.provoly.notification;

import java.time.Instant;

/**
 *
 * @param text notification message
 * @param link value to use to generate link
 * @param creationDate
 * @param creator user who initiated the notification
 */
public record ProvolyNotification(ProvolyNotificationMessage text,
        String link,
        Instant creationDate,
        String creator) {
}
