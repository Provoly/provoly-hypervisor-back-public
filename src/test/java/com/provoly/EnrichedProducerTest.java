package com.provoly;

import static com.provoly.service.ServiceStatus.ASKED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import jakarta.inject.Inject;

import com.provoly.equipment.EquipmentService;
import com.provoly.equipment.EquipmentWriteDto;
import com.provoly.equipmentEnriched.EquipmentEnriched;
import com.provoly.event.Criticality;
import com.provoly.event.EventService;
import com.provoly.event.dto.EventWriteDto;
import com.provoly.service.ServiceService;
import com.provoly.service.ServiceWriteDto;
import com.provoly.user.Role;
import com.provoly.user.UserService;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;

import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
public class EnrichedProducerTest {
    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Inject
    EquipmentService equipmentService;

    @Inject
    EventService eventService;

    @Inject
    ServiceService serviceService;

    @InjectMock
    UserService mock;

    @Inject
    TestDataService dataService;

    @BeforeEach
    public void init() {
        dataService.initUser();
        given(mock.getCurrentUserName()).willReturn("reader");
        given(mock.getCurrentUserFullName()).willReturn("name");
        given(mock.getCurrentUserSubject()).willReturn(dataService.getUser().getSubject());
        given(mock.getCurrentUser()).willReturn(dataService.getUser());
        given(mock.hasRole(Role.STR_EVENT_WRITE)).willReturn(true);
    }

    @AfterEach
    public void cleanTopic() {
        companion.topics().delete("equipment");
        dataService.clean();
    }

    @Test
    public void should_consume_enriched_equipment_when_create_equipment() {
        // given
        companion.registerSerde(EquipmentEnriched.class, new ObjectMapperSerde<>(EquipmentEnriched.class));

        var equipment = new EquipmentWriteDto("technical_id", 0, "equipment", "306", "EP", "Armoire", "CHALONS-COMMUN",
                "CHALONS",
                "address", "CENTRE", null, false, null);
        equipmentService.saveOrUpdateEquipments(List.of(equipment));

        // when
        var result = companion.consume(EquipmentEnriched.class)
                .withOffsetReset(OffsetResetStrategy.EARLIEST)
                .fromTopics("equipment").awaitRecords(1, Duration.ofSeconds(5)).getFirstRecord();

        assertThat(result.value()).extracting("code").isEqualTo(equipment.code());
        assertThat(result.value()).extracting("place").isEqualTo(equipment.district());
    }

    @Test
    public void should_consume_enriched_equipment_when_update_events() {
        // given
        // save equipments
        companion.registerSerde(EquipmentEnriched.class, new ObjectMapperSerde<>(EquipmentEnriched.class));
        var equipment = new EquipmentWriteDto("technical_id", 0, "equipment", "306", "EP", "Armoire", "CHALONS-COMMUN",
                "CHALONS",
                "address", "CENTRE", null, false, null);
        var equipment2 = new EquipmentWriteDto("technical_id2", 0, "equipment2", "307", "EP", "Armoire", "AGGLO-COMMUN",
                "CHALONS",
                "address", "CENTRE", null, false, null);
        equipmentService.saveOrUpdateEquipments(List.of(equipment, equipment2)); // 2 messages

        // save event
        var equipId = equipmentService.getEquipments(List.of("CHALONS-COMMUN"), null, 1, 10).stream().findFirst().get().getId();
        var event = new EventWriteDto(null,
                "saved event",
                "desc",
                Criticality.HIGH,
                "OUTOFORDER",
                null,
                "address",
                equipId,
                "EP",
                null,
                null,
                null,
                null,
                "creator");
        var savedEvent = eventService.saveEvent(event); // 1 message

        // update event
        var equipId2 = equipmentService.getEquipments(List.of("AGGLO-COMMUN"), null, 1, 10).stream().findFirst().get().getId();
        var eventUpdated = new EventWriteDto(savedEvent.getId(),
                "totoooo",
                "desc",
                Criticality.HIGH,
                "OUTOFORDER",
                null,
                "address",
                equipId2,
                "EP",
                null,
                null,
                null,
                null,
                "creator");
        eventService.updateEvent(savedEvent.getId(), eventUpdated); // 2 messages : one for updated event and one for previous equipment

        // when
        var result = companion.consume(EquipmentEnriched.class)
                .withOffsetReset(OffsetResetStrategy.EARLIEST)
                .fromTopics("equipment").awaitRecords(5, Duration.ofSeconds(5));

        // then
        assertThat(result).hasSize(5);
    }

    @Test
    public void should_consume_enriched_equipment_when_create_events() {
        // given
        companion.registerSerde(EquipmentEnriched.class, new ObjectMapperSerde<>(EquipmentEnriched.class));
        var equipment = new EquipmentWriteDto("technical_id", 0, "equipment", "306", "EP", "Armoire", "CHALONS-COMMUN",
                "CHALONS",
                "address", "CENTRE", null, false, null);
        var equipment2 = new EquipmentWriteDto("technical_id2", 0, "equipment2", "307", "EP", "Armoire", "AGGLO-COMMUN",
                "CHALONS",
                "address", "CENTRE", null, false, null);
        equipmentService.saveOrUpdateEquipments(List.of(equipment, equipment2)); // 2 messages

        var equipId = equipmentService.getEquipments(List.of("CHALONS-COMMUN"), null, 1, 10).stream().findFirst().get().getId();

        var event = new EventWriteDto(null,
                "toto",
                "desc",
                Criticality.HIGH,
                "OUTOFORDER",
                null,
                "adress",
                equipId,
                "EP",
                null,
                null,
                null,
                null,
                "source",
                null);
        eventService.saveEvent(event); // 1 messages

        // when
        var result = companion.consume(EquipmentEnriched.class)
                .withOffsetReset(OffsetResetStrategy.EARLIEST)
                .fromTopics("equipment").awaitRecords(3, Duration.ofSeconds(5));

        assertThat(result.getLastRecord().value().getEvents()).hasSize(1);
    }

    @Test
    public void should_consume_enriched_equipment_when_create_service() {
        // given
        companion.registerSerde(EquipmentEnriched.class, new ObjectMapperSerde<>(EquipmentEnriched.class));
        var equipment = new EquipmentWriteDto("technical_id", 0, "306", "306", "EP", "Armoire", "CHALONS-COMMUN", "CHALONS",
                "address", "CENTRE", null, false, null);
        equipmentService.saveOrUpdateEquipments(List.of(equipment)); // 1 messages

        var service = new ServiceWriteDto("technical_id1",
                null,
                "",
                "306",
                Instant.now(),
                Instant.now(),
                Instant.now(),
                Instant.now(),
                null,
                "EP",
                ASKED,
                "CURA");
        serviceService.saveOrUpdateServices(List.of(service)); // 1 message

        // when
        var result = companion.consume(EquipmentEnriched.class)
                .withOffsetReset(OffsetResetStrategy.EARLIEST)
                .fromTopics("equipment").awaitRecords(2, Duration.ofSeconds(5));

        assertThat(result.getLastRecord().value().getNbServicesAskedInProgress()).isEqualTo(1);
        assertThat(result.getLastRecord().value().getServices()).extracting("category").containsExactly("CURA");
    }

    @Test
    public void should_remove_closed_event_on_enriched_equipment_when_event_is_closed() {
        // given
        companion.registerSerde(EquipmentEnriched.class, new ObjectMapperSerde<>(EquipmentEnriched.class));
        var equipment = new EquipmentWriteDto("technical_id", 0, "306", "306", "EP", "Armoire", "CHALONS-COMMUN", "CHALONS",
                "address", "CENTRE", null, false, null);
        equipmentService.saveOrUpdateEquipments(List.of(equipment)); // 1 messages

        var equipId = equipmentService.getEquipments(List.of("CHALONS-COMMUN"), null, 1, 10).stream().findFirst().get().getId();

        var event = new EventWriteDto(null,
                "toto",
                "desc",
                Criticality.HIGH,
                "OUTOFORDER",
                null,
                "adress",
                equipId,
                "EP",
                null,
                null,
                null,
                null,
                "source",
                null);
        var savedEvent = eventService.saveEvent(event); // 1 messages

        // when
        eventService.closeEvent(savedEvent); // 1 message

        var result = companion.consume(EquipmentEnriched.class)
                .withOffsetReset(OffsetResetStrategy.EARLIEST)
                .fromTopics("equipment").awaitRecords(3, Duration.ofSeconds(5));

        assertThat(result.getRecords().get(1).value().getEvents()).hasSize(1);
        assertThat(result.getLastRecord().value().getEvents()).isEmpty();
    }
}
