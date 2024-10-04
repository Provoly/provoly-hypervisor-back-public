package com.provoly.notification;

import jakarta.enterprise.context.ApplicationScoped;

import com.provoly.event.Event;
import com.provoly.user.UserService;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

@ApplicationScoped
public class NotificationProducer {

    public static final int MAX_NOTIFICATION_SIZE = 200;
    private final Logger log;

    private final UserService userService;

    private final Emitter<ProvolyNotification> notificationEmitter;

    public NotificationProducer(Logger log, UserService userService,
            @Channel("notifications") Emitter<ProvolyNotification> notificationEmitter) {
        this.log = log;
        this.userService = userService;
        this.notificationEmitter = notificationEmitter;
    }

    public synchronized void sendNotificationFor(Event event) {
        var user = userService.getCurrentUser();
        log.debugf("Send notification for event %s created by %s", event.getId(), user.getUsername());
        var notification = new ProvolyNotification(
                new ProvolyNotificationMessage(event.getName(), truncateDescription(event.getDescription())),
                "/journal/%s".formatted(event.getId().toString()),
                event.getCreationDate(),
                user.getSubject().toString());
        QuarkusTransaction.suspendingExisting().run(() -> this.send(notification));
    }

    public void send(ProvolyNotification notification) {
        notificationEmitter.send(KafkaRecord.of(notification.creationDate().toString(), notification));
    }

    private static String truncateDescription(String description) {
        return description.substring(0, Math.min(description.length(), MAX_NOTIFICATION_SIZE));
    }
}
