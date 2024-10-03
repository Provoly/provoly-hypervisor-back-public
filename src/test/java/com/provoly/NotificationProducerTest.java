package com.provoly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Duration;

import jakarta.inject.Inject;

import com.provoly.event.Criticality;
import com.provoly.event.EventService;
import com.provoly.event.dto.EventWriteDto;
import com.provoly.notification.ProvolyNotification;
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
public class NotificationProducerTest {
    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Inject
    EventService eventService;

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
    }

    @AfterEach
    public void cleanTopic() {
        companion.topics().delete("notifications");
    }

    @Test
    public void should_consume_notification_when_create_events() {
        // given
        companion.registerSerde(ProvolyNotification.class, new ObjectMapperSerde<>(ProvolyNotification.class));

        var event = new EventWriteDto(null,
                "toto",
                "desc",
                Criticality.HIGH,
                "OUTOFORDER",
                null,
                "address",
                null,
                "EP",
                null,
                null,
                null);
        eventService.saveEvent(event); // 1 message
        eventService.saveEvent(event); // 1 message

        // when
        var result = companion.consume(ProvolyNotification.class)
                .withOffsetReset(OffsetResetStrategy.EARLIEST)
                .fromTopics("notifications").awaitRecords(2, Duration.ofSeconds(5));

        assertThat(result.getLastRecord().value().text().title()).isEqualTo("toto");
    }
}
