package com.provoly.event;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;

import com.provoly.TestDataService;
import com.provoly.event.dto.AlertEventWriteDto;
import com.provoly.event.dto.OperatorEventWriteDto;
import com.provoly.event.dto.ReportEventWriteDto;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventServiceTest {

    @Inject
    EventService eventService;

    @Inject
    TestDataService dataService;

    @BeforeAll
    public void init() {
        dataService.init();
    }

    @AfterAll
    public void clean() {
        dataService.clean();
    }

    @Test
    void should_throw_exception_create_event_name_with_already_exists() {
        // given
        var event = new OperatorEventWriteDto(UUID.randomUUID(), "operator1", "desc", Criticality.HIGH,
                null, null, Category.OPERATOR, null, null, null);

        // then
        assertThatThrownBy(() -> eventService.saveOrUpdateEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void should_throw_exception_create_operator_event_with_missing_dates() {
        // given
        var event = new OperatorEventWriteDto(UUID.randomUUID(), "tutu", "desc", Criticality.HIGH, null, null,
                Category.MANIFESTATION, null, null, null);

        // then
        assertThatThrownBy(() -> eventService.saveOrUpdateEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Properties 'startDate' and 'endDate' are required for 'MANIFESTATION' category");
    }

    @Test
    void should_throw_exception_create_operator_event_with_invalid_dates() {
        // given
        var event = new OperatorEventWriteDto(UUID.randomUUID(), "tutu", "desc", Criticality.HIGH, null, null,
                Category.MANIFESTATION, Instant.now(), Instant.now().minusMillis(1000), null);

        // then
        assertThatThrownBy(() -> eventService.saveOrUpdateEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End date is invalid: it must be after start date");
    }

    @Test
    void should_throw_exception_create_alert_event_missing_equipment_id() {
        // given
        var event = new AlertEventWriteDto(UUID.randomUUID(), "tata", "desc", Criticality.HIGH, null, null,
                null, "ref", null);

        // then
        assertThatThrownBy(() -> eventService.saveOrUpdateEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Alert event must reference an equipment");
    }

    @Test
    void should_throw_exception_update_alert_event() {
        // given
        var eventAlertId = eventService
                .getEvents(1, 1, null, null, null, List.of(), List.of(), List.of(Category.LIMIT.name()), List.of(),
                        List.of())
                .stream()
                .toList()
                .getFirst()
                .getId();

        var event = new AlertEventWriteDto(eventAlertId, "tutu",
                "desc", Criticality.HIGH, null, null, null, "ref", null);

        // then
        assertThatThrownBy(() -> eventService.saveOrUpdateEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("It's not possible to update event");
    }

    @Test
    void should_throw_exception_update_report_event_externalSourceRef_property() {
        // given
        var eventReportId = eventService
                .getEvents(1, 1, null, null, null, List.of(), List.of(), List.of(Category.REPORT.name()), List.of(),
                        List.of())
                .stream()
                .toList()
                .getFirst()
                .getId();

        var event = new ReportEventWriteDto(eventReportId, "toto",
                "desc", Criticality.HIGH, null, null, null, "ref", null);

        // then
        assertThatThrownBy(() -> eventService.saveOrUpdateEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("It's not possible to update externalSourceRef value");
    }

    @Test
    void should_throw_exception_create_event_with_invalid_domain() {
        // given
        var event = new OperatorEventWriteDto(UUID.randomUUID(), "new event", "desc", Criticality.HIGH,
                null, null, Category.OPERATOR, null, null, "invalid_domain");

        // then
        assertThatThrownBy(() -> eventService.saveOrUpdateEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid");
    }

    @Test
    void should_close_event() {
        // given
        var eventIdInProgress = eventService
                .getEvents(1, 1, null, null, null, List.of(), List.of(Status.IN_PROGRESS.name()), List.of(), List.of(),
                        List.of())
                .stream()
                .toList()
                .getFirst()
                .getId();

        // when
        eventService.closeEventById(eventIdInProgress);

        // then
        var event = eventService.getEventDetails(eventIdInProgress);
        assertThat(event.getCloseDate()).isNotNull();
        assertThat(event.getStatus()).isEqualTo(Status.DONE);

    }

    @Test
    void should_not_close_already_closed_event() {
        // given
        var eventIdDone = eventService
                .getEvents(1, 1, null, null, null, List.of(), List.of(Status.DONE.name()), List.of(), List.of(), List.of())
                .stream()
                .toList()
                .getFirst()
                .getId();
        var oldEvent = eventService.getEventDetails(eventIdDone);

        // when
        eventService.closeEventById(eventIdDone);

        // then
        var event = eventService.getEventDetails(eventIdDone);
        assertThat(event.getCloseDate()).isEqualTo(oldEvent.getCloseDate());
    }

    @Test
    void should_return_empty_summary_event_and_count_0_when_no_event(){
        // given
        clean();

        // when
        var result = eventService.getEventSummariesGroupByStatus(10,null);

        // then
        assertThat(result.get(Status.NEW)).extracting("count").isEqualTo(0L);
        assertThat(result.get(Status.IN_PROGRESS)).extracting("count").isEqualTo(0L);
        assertThat(result.get(Status.DONE)).extracting("count").isEqualTo(0L);

        init();
    }
}
