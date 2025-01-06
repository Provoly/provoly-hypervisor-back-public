package com.provoly.equipmentenriched;

import jakarta.enterprise.context.ApplicationScoped;

import com.provoly.equipment.Equipment;
import com.provoly.event.Event;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EquipmentEnrichedProducer {

    private final Logger log;
    private final Emitter<EquipmentEnriched> equipmentEmitter;

    public EquipmentEnrichedProducer(Logger log,
            @Channel("equipment") Emitter<EquipmentEnriched> equipmentEmitter) {
        this.log = log;
        this.equipmentEmitter = equipmentEmitter;
    }

    // No way to execute send inside a tx. Even if waiting for Ack
    // https://github.com/quarkusio/quarkus/issues/25066#issuecomment-1111890626
    // https://smallrye.io/smallrye-reactive-messaging/smallrye-reactive-messaging/3.3/emitter/emitter.html
    // Synchronized added to avoid :  ARJUNA012107: CheckedAction::check - atomic action 0:ffffd45f4a4b:c020:6628c471:18 commiting with 2 threads active!

    public synchronized void updateFor(Event event) {
        if (event.getEquipment() == null) {
            log.debugf("No equipment for event %s, do nothing", event.getId());
            return;
        }

        log.debugf("Enriched equipment from event %s and send it to topic 'equipment'", event.getId());
        var equipment = event.getEquipment();
        var enriched = new EquipmentEnriched(equipment);
        QuarkusTransaction.suspendingExisting().run(() -> this.send(enriched));
    }

    public synchronized void updateFor(Equipment equipment) {
        log.debugf("Enriched equipment %s and send it to topic 'equipment'", equipment.getId());
        var enriched = new EquipmentEnriched(equipment);
        QuarkusTransaction.suspendingExisting().run(() -> this.send(enriched));
    }

    public void send(EquipmentEnriched enriched) {
        equipmentEmitter.send(KafkaRecord.of(enriched.getCode(), enriched));
    }

}
