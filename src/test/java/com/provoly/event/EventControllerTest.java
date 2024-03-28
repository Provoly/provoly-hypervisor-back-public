package com.provoly.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;

import com.provoly.event.dto.OperatorEventWriteDto;

import io.quarkus.security.UnauthorizedException;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;

import org.junit.jupiter.api.Test;

@QuarkusTest
public class EventControllerTest {
    @Inject
    EventController eventController;

    @Test
    void should_throw_forbidden_if_user_is_not_authenticated() {
        assertThatThrownBy(() -> eventController.getEvents(
                1,
                0,
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @TestSecurity(user = "reader")
    void should_return_first_two_event_when_get_events() {
        // when
        var events = eventController.getEvents(
                1,
                2,
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());
        //then
        assertThat(events).hasSize(2);
    }

    @Test
    @TestSecurity(user = "reader")
    void should_return_empty_event_when_at_least_criticality_status_category_has_empty_values() {
        // given
        var creationDate = Instant.parse("2024-01-20T00:00:00.000Z");

        var event = eventController.getEventDetails(UUID.fromString("b0d1a93c-ec57-401c-9a74-2cc364bbab9f"));

        // when
        var events = eventController.getEvents(
                1,
                20,
                null,
                null,
                null,
                List.of(""),
                List.of(),
                List.of(),
                List.of(),
                List.of());
        //then
        assertThat(events).isEmpty();
    }

    @Test
    @TestSecurity(user = "reader")
    void should_return_event_created_on_corresponding_date() {
        // given
        var creationDate = Instant.parse("2024-01-20T00:00:00.000Z");

        var event = eventController.getEventDetails(UUID.fromString("b0d1a93c-ec57-401c-9a74-2cc364bbab9f"));

        // when
        var events = eventController.getEvents(
                1,
                20,
                null,
                null,
                creationDate,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());
        //then
        assertThat(events).extracting("creationDate").containsExactly(Instant.parse("2024-01-20T11:12:39.375184Z"));
    }

    @Test
    @TestSecurity(user = "reader")
    void should_return_event_high_and_low_criticality_when_get_events() {
        // when
        var events = eventController.getEvents(
                1,
                20,
                null,
                null,
                null,
                List.of(Criticality.HIGH.name(), Criticality.LOW.name()),
                List.of(),
                List.of(),
                List.of(),
                List.of());
        //then
        assertThat(events).extracting("criticality").containsOnly(Criticality.LOW, Criticality.HIGH);
    }

    @Test
    @TestSecurity(user = "reader")
    void should_return_event_new_and_done_status_when_get_events() {
        // when
        var events = eventController.getEvents(
                1,
                20,
                null,
                null,
                null,
                List.of(),
                List.of(Status.DONE.name(), Status.NEW.name()),
                List.of(),
                List.of(),
                List.of());
        //then
        assertThat(events).extracting("status").containsOnly(Status.DONE, Status.NEW);
    }

    @Test
    @TestSecurity(user = "reader")
    void should_return_event_manifestation_or_limit_and_in_progress_status_when_get_events() {
        // when
        var events = eventController.getEvents(
                1,
                20,
                null,
                null,
                null,
                List.of(),
                List.of(Status.IN_PROGRESS.name()),
                List.of("MANIFESTATION", "ALERT_LIMIT", "REPORT"),
                List.of(),
                List.of());
        //then
        assertThat(events).extracting("status").containsOnly(Status.IN_PROGRESS);
        assertThat(events).extracting("category").containsOnly(OperatorCategory.MANIFESTATION, ReportCategory.REPORT);
    }

    @Test
    @TestSecurity(user = "reader")
    void should_return_event_with_corresponding_equipment_entity() {
        // when
        var events = eventController.getEvents(
                1,
                20,
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of("AGGLO_COMMUN"),
                List.of());
        //then
        assertThat(events).extracting("equipment").extracting("entity").containsOnly("AGGLO_COMMUN");
    }

    @Test
    @TestSecurity(user = "reader")
    void should_throw_invalid_equipment_entity() {
        assertThatThrownBy(() -> eventController.getEvents(
                1,
                20,
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of("AGGLO_COMMUN", "invalid"),
                List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @TestSecurity(user = "reader")
    void should_return_event_with_corresponding_familiy() {
        // when
        var events = eventController.getEvents(
                1,
                20,
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of("EP_O"));
        //then
        assertThat(events).extracting("equipment").extracting("type").containsOnly("Ouvrage");
    }

    @Test
    @TestSecurity(user = "reader")
    void should_throw_invalid_familiy() {
        assertThatThrownBy(() -> eventController.getEvents(
                1,
                20,
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of("invalid")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @TestSecurity(user = "reader")
    void should_throw_forbidden_if_page_is_not_positive_when_get_events() {
        assertThatThrownBy(() -> eventController.getEvents(
                0,
                2,
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @TestSecurity(user = "reader")
    void should_throw_forbidden_if_pageSize_is_not_positive_when_get_events() {
        assertThatThrownBy(() -> eventController.getEvents(
                1,
                0,
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @TestSecurity(user = "reader")
    void should_return_event_with_default_sort_on_status() {
        // when
        var events = eventController.getEvents(
                1,
                3,
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());
        //then
        assertThat(events).extracting("status").containsExactly(Status.NEW, Status.NEW, Status.IN_PROGRESS);
    }

    @Test
    @TestSecurity(user = "reader")
    void should_return_event_with_desc_sort_procedure_progress() {
        // when
        var events = eventController.getEvents(
                1,
                3,
                Sort.PROCEDURE_PROGRESS.getName(),
                SortOrder.DESC.name(),
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());
        //then
        assertThat(events).extracting("procedureProgress").containsExactly(100f, 0f, 0f);
    }

    @Test
    @TestSecurity(user = "reader")
    void should_throw_exception_invalid_sort() {
        // given
        String invalidSort = "invalid";

        // then
        assertThatThrownBy(() -> eventController.getEvents(
                1,
                1,
                invalidSort,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(invalidSort);
    }

    @Test
    @TestSecurity(user = "reader")
    void should_return_limited_event_summary_by_status() {
        // given
        int limit = 1;

        // when
        var result = eventController.getEventSummaries(limit, null);

        // then
        assertThat(result.get(Status.NEW).events()).hasSize(limit);
        assertThat(result.get(Status.IN_PROGRESS).events()).hasSize(limit);
        assertThat(result.get(Status.DONE).events()).hasSize(limit);

        assertThat(result.get(Status.IN_PROGRESS).events())
                .extracting("serviceTitle").containsOnlyNulls();
        assertThat(result.get(Status.IN_PROGRESS).events())
                .extracting("serviceCount").containsExactly(1L);

        assertThat(result.get(Status.DONE).events())
                .extracting("serviceTitle").isNotEmpty();
        assertThat(result.get(Status.DONE).events())
                .extracting("serviceCount").containsExactly(2L);

        assertThat(result.get(Status.NEW).events())
                .extracting("manifestation").containsOnlyNulls();

    }

    @Test
    @TestSecurity(user = "reader")
    void should_throw_exception_invalid_criticality_when_get_summaries() {
        // given
        String invalidCriticality = "invalid";

        // then
        assertThatThrownBy(() -> eventController.getEventSummaries(1, invalidCriticality))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(invalidCriticality);
    }

    @Test
    @TestSecurity(user = "reader")
    void should_return_limited_event_summary_by_status_filtered_by_criticality() {
        // given
        int limit = 1;
        String criticality = "HIGH";

        // when
        var result = eventController.getEventSummaries(limit, criticality);

        // then
        assertThat(result.get(Status.IN_PROGRESS).events()).extracting("criticality")
                .containsExactly(Criticality.valueOf(criticality));
        assertThat(result.get(Status.DONE).events()).isEmpty();
        assertThat(result.get(Status.NEW).events()).isEmpty();

    }

    @Test
    @TestSecurity(user = "reader")
    void should_return_event_by_id() {
        // when
        final UUID id = UUID.fromString("01ffde9d-30d2-4273-b61f-c6addd8747c8");
        var event = eventController.getEventDetails(id);
        //then
        assertThat(event).extracting("id").isEqualTo(id);
    }

    @Test
    @TestSecurity(user = "reader")
    void should_throw_not_found_when_invalid_event_id() {
        assertThatThrownBy(() -> eventController.getEventDetails(UUID.randomUUID()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @TestSecurity(user = "reader")
    void should_throw_create_event_missing_required_property() {
        // given
        var event = new OperatorEventWriteDto(UUID.randomUUID(), null, "desc", null, null, null, null, null, null, null);

        // then
        assertThatThrownBy(() -> eventController.saveEventOperator(event))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @TestSecurity(user = "reader")
    void should_throw_if_name_blank_when_create_event() {
        // given
        var event = new OperatorEventWriteDto(UUID.randomUUID(), "", "desc", Criticality.MEDIUM, "adress", null,
                OperatorCategory.OPERATOR_EVENT, null, null, null);

        // then
        assertThatThrownBy(() -> eventController.saveEventOperator(event))
                .isInstanceOf(ConstraintViolationException.class);
    }
}