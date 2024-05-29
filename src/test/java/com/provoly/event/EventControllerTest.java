package com.provoly.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;

import com.provoly.TestDataService;
import com.provoly.event.dto.OperatorEventWriteDto;

import io.quarkus.security.UnauthorizedException;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventControllerTest {
    @Inject
    EventController eventController;

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
        var creationDate = Instant.parse(LocalDate.now().atStartOfDay() + ":00.000Z");

        // when
        var events = eventController.getEvents(
                1,
                1,
                null,
                null,
                creationDate,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());
        //then
        assertThat(events).hasSize(1);
        assertThat(events.stream().toList().getFirst().getCreationDate().isAfter(creationDate));
        assertThat(events.stream().toList().getFirst().getCreationDate().isBefore(creationDate.plus(1, ChronoUnit.DAYS)));
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
        assertThat(events).extracting("category").containsOnly(Category.MANIFESTATION, Category.REPORT);
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
                List.of("EP_ARMOIRE"));
        //then
        assertThat(events).extracting("equipment").extracting("family").contains("Armoire", "Armoire", "Armoire", "Armoire");
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
    void should_return_event_with_default_sort_on_status_criticality() {
        // when
        var events = eventController.getEvents(
                1,
                5,
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());
        //then
        assertThat(events).extracting("status").containsExactly(Status.NEW, Status.NEW, Status.NEW, Status.NEW,
                Status.IN_PROGRESS);
        assertThat(events).extracting("criticality").containsExactly(Criticality.LOW, Criticality.LOW, Criticality.LOW,
                Criticality.LOW, Criticality.HIGH);
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
        assertThat(events).extracting("procedureProgress").containsExactly(100f, 100f, 33f);
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
                .extracting("serviceTitle").isNotEmpty();
        assertThat(result.get(Status.IN_PROGRESS).events())
                .extracting("serviceCount").containsExactly(1L);

        assertThat(result.get(Status.DONE).events())
                .extracting("serviceTitle").isNotEmpty();
        assertThat(result.get(Status.DONE).events())
                .extracting("serviceCount").containsExactly(1L);

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
        var firstEventId = eventController
                .getEvents(1, 1, null, null, null, List.of(), List.of(), List.of(), List.of(), List.of())
                .stream()
                .toList()
                .getFirst()
                .getId();

        // when
        var event = eventController.getEventDetails(firstEventId);
        //then
        assertThat(event).extracting("id").isEqualTo(firstEventId);
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
                Category.OPERATOR_EVENT, null, null, null);

        // then
        assertThatThrownBy(() -> eventController.saveEventOperator(event))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @TestSecurity(user = "reader")
    void should_throw_if_category_invalid_when_create_event() {
        // given
        var event = new OperatorEventWriteDto(UUID.randomUUID(), "event operator", "desc", Criticality.MEDIUM, "adress", null,
                Category.REPORT, null, null, null);

        // then
        assertThatThrownBy(() -> eventController.saveEventOperator(event))
                .isInstanceOf(ConstraintViolationException.class);
    }
}