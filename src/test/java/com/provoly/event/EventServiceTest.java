package com.provoly.event;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.Instant;
import java.util.UUID;

import jakarta.inject.Inject;

import com.provoly.event.dto.AlertEventWriteDto;
import com.provoly.event.dto.OperatorEventWriteDto;
import com.provoly.event.dto.ReportEventWriteDto;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Test;

@QuarkusTest
public class EventServiceTest {

    @Inject
    EventService eventService;

    @Test
    void should_throw_exception_create_event_name_with_already_exists() {
        // given
        var event = new OperatorEventWriteDto(UUID.randomUUID(), "Signalement encombrant", "desc", Criticality.HIGH,
                null, null, OperatorCategory.OPERATOR_EVENT, null, null, null);

        // then
        assertThatThrownBy(() -> eventService.saveOrUpdateOperatorEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void should_throw_exception_create_operator_event_with_missing_dates() {
        // given
        var event = new OperatorEventWriteDto(UUID.randomUUID(), "tutu", "desc", Criticality.HIGH, null, null,
                OperatorCategory.MANIFESTATION, null, null, null);

        // then
        assertThatThrownBy(() -> eventService.saveOrUpdateOperatorEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Properties 'startDate' and 'endDate' are required for 'MANIFESTATION' category");
    }

    @Test
    void should_throw_exception_create_operator_event_with_invalid_dates() {
        // given
        var event = new OperatorEventWriteDto(UUID.randomUUID(), "tutu", "desc", Criticality.HIGH, null, null,
                OperatorCategory.MANIFESTATION, Instant.now(), Instant.now().minusMillis(1000), null);

        // then
        assertThatThrownBy(() -> eventService.saveOrUpdateOperatorEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End date is invalid: it must be after start date");
    }

    @Test
    void should_throw_exception_create_alert_event_missing_equipment_id() {
        // given
        var event = new AlertEventWriteDto(UUID.randomUUID(), "tata", "desc", Criticality.HIGH, null, null,
                null, "ref", null);

        // then
        assertThatThrownBy(() -> eventService.saveAlertEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Alert event must reference an equipment");
    }

    @Test
    void should_throw_exception_update_alert_event() {
        // given
        var event = new AlertEventWriteDto(UUID.fromString("01ffde9d-30d2-4273-b61f-c6addd8747c8"), "tutu",
                "desc", Criticality.HIGH, null, null, null, "ref", null);

        // then
        assertThatThrownBy(() -> eventService.saveAlertEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("it's not possible to update it");
    }

    @Test
    void should_throw_exception_update_report_event_externalSourceRef_property() {
        // given
        var event = new ReportEventWriteDto(UUID.fromString("ebbfbbd1-b8de-467c-8a9b-88443a49f807"), "toto",
                "desc", Criticality.HIGH, null, null, null, "ref", null);

        // then
        assertThatThrownBy(() -> eventService.saveOrUpdateReportEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("It's not possible to update externalSourceRef value");
    }

    @Test
    void should_throw_exception_create_event_with_invalid_domain() {
        // given
        var event = new OperatorEventWriteDto(UUID.randomUUID(), "new event", "desc", Criticality.HIGH,
                null, null, OperatorCategory.OPERATOR_EVENT, null, null, "invalid_domain");

        // then
        assertThatThrownBy(() -> eventService.saveOrUpdateOperatorEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid");
    }

    @Test
    void should_close_event() {
        // given
        var eventId = UUID.fromString("9f15215e-94bd-4056-89e6-1e69f0691419");

        // when
        eventService.closeEventById(eventId);

        // then
        var event = eventService.getEventDetails(eventId);
        assertThat(event.getCloseDate()).isNotNull();
        assertThat(event.getStatus()).isEqualTo(Status.DONE);

    }

    @Test
    void should_not_close_already_closed_event() {
        // given
        var eventId = UUID.fromString("01ffde9d-30d2-4273-b61f-c6addd8747c8");
        var oldEvent = eventService.getEventDetails(eventId);

        // when
        eventService.closeEventById(eventId);

        // then
        var event = eventService.getEventDetails(eventId);
        assertThat(event.getCloseDate()).isEqualTo(oldEvent.getCloseDate());
    }
}
