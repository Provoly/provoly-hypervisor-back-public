package com.provoly.event;

import static com.provoly.event.Criticality.HIGH;
import static com.provoly.event.Criticality.LOW;
import static com.provoly.event.Criticality.MEDIUM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;

import com.provoly.TestDataService;
import com.provoly.comment.CommentWriteDto;
import com.provoly.event.dto.EventWriteDto;
import com.provoly.user.UserService;

import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class EventControllerTest {
    @Inject
    EventController eventController;

    @Inject
    TestDataService dataService;

    @InjectMock
    UserService mock;

    @BeforeEach
    public void init() {
        dataService.init();
        given(mock.getCurrentUserName()).willReturn("reader");
        given(mock.getCurrentUserFullName()).willReturn("name");
        given(mock.getCurrentUserSubject()).willReturn(dataService.getUser().getSubject());
        given(mock.getCurrentUser()).willReturn(dataService.getUser());
    }

    @AfterEach
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
                null))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_return_first_two_event_when_get_events() {
        // when
        var events = eventController.getEvents(
                1,
                2,
                null,
                null,
                new EventController.EventParameters());
        //then
        assertThat(events).hasSize(2);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_return_empty_event_when_at_least_criticality_status_category_has_empty_values() {
        // given
        var param = new EventController.EventParameters();
        param.category = List.of("");
        // when
        var events = eventController.getEvents(
                1,
                20,
                null,
                null,
                param);
        //then
        assertThat(events).isEmpty();
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_return_event_created_on_corresponding_date() {
        // given
        var param = new EventController.EventParameters();
        var creationDate = Instant.parse(LocalDate.now().atStartOfDay() + ":00.000Z");
        param.creationDate = creationDate;
        // when
        var events = eventController.getEvents(
                1,
                1,
                null,
                null,
                param);
        //then
        assertThat(events).hasSize(1);
        assertThat(events.stream().toList().getFirst().creationDate()).isAfter(creationDate);
        assertThat(events.stream().toList().getFirst().creationDate()).isBefore(creationDate.plus(1, ChronoUnit.DAYS));
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_return_event_high_and_low_criticality_when_get_events() {
        // given
        var param = new EventController.EventParameters();
        param.criticality = List.of(Criticality.HIGH.name(), LOW.name());
        // when
        var events = eventController.getEvents(
                1,
                20,
                null,
                null,
                param);
        //then
        assertThat(events).extracting("criticality").containsOnly(LOW, Criticality.HIGH);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_return_event_new_and_done_status_when_get_events() {
        // given
        var param = new EventController.EventParameters();
        param.status = List.of(Status.DONE.name(), Status.NEW.name());
        // when
        var events = eventController.getEvents(
                1,
                20,
                null,
                null,
                param);
        //then
        assertThat(events).extracting("status").containsOnly(Status.DONE, Status.NEW);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_return_event_manifestation_or_limit_and_in_progress_status_when_get_events() {
        // given
        var param = new EventController.EventParameters();
        param.status = List.of(Status.IN_PROGRESS.name());
        param.category = List.of("MANIFESTATION", "LIMIT", "OUTOFORDER");

        // when
        var events = eventController.getEvents(
                1,
                20,
                null,
                null,
                param);
        //then
        assertThat(events).extracting("status").containsOnly(Status.IN_PROGRESS);
        assertThat(events).extracting("category").containsOnly("MANIFESTATION", "OUTOFORDER");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_return_event_with_corresponding_equipment_entity() {
        // given
        var param = new EventController.EventParameters();
        param.entity = List.of("AGGLO-COMMUN");

        // when
        var events = eventController.getEvents(
                1,
                20,
                null,
                null,
                param);
        //then
        assertThat(events).extracting("equipment").extracting("entity").containsOnly("AGGLO-COMMUN");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_throw_invalid_equipment_entity() {
        // given
        var param = new EventController.EventParameters();
        param.entity = List.of("AGGLO-COMMUN", "invalid");

        assertThatThrownBy(() -> eventController.getEvents(
                1,
                20,
                null,
                null,
                param))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_return_event_with_corresponding_family() {
        // given
        var param = new EventController.EventParameters();
        param.family = List.of("EP_ARMOIRE");

        // when
        var events = eventController.getEvents(
                1,
                20,
                null,
                null,
                param);
        //then
        assertThat(events).extracting("equipment").extracting("family").contains("EP_ARMOIRE", "EP_ARMOIRE", "EP_ARMOIRE",
                "EP_ARMOIRE");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_throw_invalid_family() {
        // given
        var param = new EventController.EventParameters();
        param.family = List.of("invalid");

        assertThatThrownBy(() -> eventController.getEvents(
                1,
                20,
                null,
                null,
                param))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_throw_forbidden_if_page_is_not_positive_when_get_events() {
        assertThatThrownBy(() -> eventController.getEvents(
                0,
                2,
                null,
                null,
                new EventController.EventParameters()))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_throw_forbidden_if_pageSize_is_not_positive_when_get_events() {
        assertThatThrownBy(() -> eventController.getEvents(
                1,
                0,
                null,
                null,
                new EventController.EventParameters()))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_return_event_with_default_sort_on_status_criticality() {
        // when
        var events = eventController.getEvents(
                1,
                5,
                null,
                null,
                new EventController.EventParameters());
        //then
        assertThat(events).extracting("status").containsExactly(Status.NEW, Status.NEW, Status.NEW, Status.IN_PROGRESS,
                Status.IN_PROGRESS);
        assertThat(events).extracting("criticality").containsExactly(LOW, LOW, LOW,
                Criticality.HIGH, MEDIUM);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_return_event_with_desc_sort_procedure_progress() {
        // when
        var events = eventController.getEvents(
                1,
                3,
                EventSort.PROCEDURE_PROGRESS.getName(),
                SortOrder.DESC.name(),
                new EventController.EventParameters());
        //then
        assertThat(events).extracting("procedureProgress").containsExactly(100f, 100f, 0.0f);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_return_event_with_desc_sort_name() {
        // when
        var events = eventController.getEvents(
                1,
                3,
                EventSort.NAME.getName(),
                SortOrder.DESC.name(),
                new EventController.EventParameters());
        //then
        assertThat(events).extracting("name").containsExactly("report3", "report2", "report1");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_return_event_with_desc_sort_id() {
        // when
        var events = eventController.getEvents(
                1,
                3,
                EventSort.ID.getName(),
                SortOrder.DESC.name(),
                new EventController.EventParameters());
        //then
        assertThat(events).extracting("name").containsExactly("limit1", "malfunction1", "report3");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_return_event_with_asc_sort_source() {
        // when
        var events = eventController.getEvents(
                1,
                5,
                EventSort.SOURCE.getName(),
                null,
                new EventController.EventParameters());
        //then
        assertThat(events).extracting("externalSourceRef").containsExactly("citylinx", "Hyperviseur", "Hyperviseur",
                "Hyperviseur", "source");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_return_event_with_desc_sort_criticality() {
        // when
        var events = eventController.getEvents(
                2,
                3,
                EventSort.CRITICALITY.getName(),
                SortOrder.DESC.name(),
                new EventController.EventParameters());
        //then
        assertThat(events).extracting("criticality").containsExactly(LOW, MEDIUM, MEDIUM);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_return_event_with_desc_sort_category() {
        // when
        var events = eventController.getEvents(
                2,
                3,
                EventSort.CATEGORY.getName(),
                SortOrder.ASC.name(),
                new EventController.EventParameters());
        //then
        assertThat(events).extracting("category").containsExactly("OUTOFORDER", "MANIFESTATION", "MANIFESTATION");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_throw_exception_invalid_sort() {
        // given
        String invalidSort = "invalid";

        // then
        assertThatThrownBy(() -> eventController.getEvents(
                1,
                1,
                invalidSort,
                null,
                new EventController.EventParameters()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(invalidSort);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
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
                .extracting("serviceCount").containsExactly(0L);

        assertThat(result.get(Status.NEW).events())
                .extracting("startDate").containsOnlyNulls();
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_throw_exception_invalid_criticality_when_get_summaries() {
        // given
        String invalidCriticality = "invalid";

        // then
        assertThatThrownBy(() -> eventController.getEventSummaries(1, invalidCriticality))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(invalidCriticality);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
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
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_return_event_by_id() {
        var firstEventId = eventController
                .getEvents(1, 1, null, null, new EventController.EventParameters())
                .stream()
                .toList()
                .getFirst()
                .id();

        // when
        var event = eventController.getEventDetails(firstEventId);
        //then
        assertThat(event).extracting("id").isEqualTo(firstEventId);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_throw_not_found_when_invalid_event_id() {
        assertThatThrownBy(() -> eventController.getEventDetails(666))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_write" })
    void should_throw_create_event_missing_required_property() {
        // given
        var event = new EventWriteDto(null,
                null,
                "desc",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        // then
        assertThatThrownBy(() -> eventController.saveEvent(event))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_write" })
    void should_throw_if_name_blank_when_create_event() {
        // given
        var event = dataService.buildEvent("", "LIMIT", MEDIUM, false);

        // then
        assertThatThrownBy(() -> eventController.saveEvent(event))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_write" })
    void should_throw_if_category_invalid_when_create_event() {
        // given
        var event = dataService.buildEvent("out of order", "OUTOF", MEDIUM, false);

        // then
        assertThatThrownBy(() -> eventController.saveEvent(event))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_throw_if_role_event_write_is_missing() {
        // given
        var event = dataService.buildEvent("out of order", "OUTOF", MEDIUM, false);

        // then
        assertThatThrownBy(() -> eventController.saveEvent(event))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_write", "event_read" })
    void should_increment_comment_count_and_get_last_comment_when_add_new_comment_on_event() {
        // given
        var eventId = dataService.getEvent1().getId();
        var comment = new CommentWriteDto(UUID.randomUUID(), "message");
        eventController.saveOrUpdateCommentForEvent(eventId, comment);

        var comment2 = new CommentWriteDto(UUID.randomUUID(), "message2");

        // when
        eventController.saveOrUpdateCommentForEvent(eventId, comment2);
        var eventWithComment = eventController.getEventDetails(eventId);

        //then
        assertThat(eventWithComment.getCommentCount()).isEqualTo(2);
        assertThat(eventWithComment.getLastComment().id()).isEqualTo(comment2.id());
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_write", "event_read" })
    void should_sort_comment_on_modification_date_when_add_new_comment_on_event() {
        // given
        var eventId = dataService.getEvent1().getId();
        var comment = new CommentWriteDto(UUID.randomUUID(), "message");
        var comment2 = new CommentWriteDto(UUID.randomUUID(), "message2");
        eventController.saveOrUpdateCommentForEvent(eventId, comment);
        eventController.saveOrUpdateCommentForEvent(eventId, comment2);

        // when
        eventController.saveOrUpdateCommentForEvent(eventId,
                new CommentWriteDto(comment.id(), "message updated"));
        var comments = eventController.getCommentsForEvent(eventId);

        //then
        assertThat(comments).extracting("message").containsExactly("message updated", "message2");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_write", "event_read" })
    void should_close_event() {
        // given
        var eventId = dataService.getEvent1().getId();
        var closedComment = new CommentWriteDto(UUID.randomUUID(), "close event");

        // when
        eventController.closeEvent(eventId, closedComment);
        var event = eventController.getEventDetails(eventId);

        //then
        assertThat(event.getLastComment()).extracting("message").isEqualTo("close event");
        assertThat(event.getStatus()).isEqualTo(Status.DONE);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_write", "event_read" })
    void should_throw_error_when_event_already_done() {
        // given
        var eventId = dataService.getDoneEvent().getId();
        var closedComment = new CommentWriteDto(UUID.randomUUID(), "close event");

        // when
        assertThatThrownBy(() -> eventController.closeEvent(eventId, closedComment))
                .isInstanceOf(jakarta.ws.rs.ForbiddenException.class);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_search_event_with_name_contains_report() {
        // given
        var param = new EventController.EventParameters();
        param.name = "report";
        param.id = "report";
        // when
        var events = eventController.getEvents(
                1,
                10,
                EventSort.NAME.getName(),
                SortOrder.ASC.name(),
                param);
        //then
        assertThat(events).extracting("name").containsExactly("report1", "report2", "report3");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_search_event_with_equipment_contains_name() {
        // given
        var param = new EventController.EventParameters();
        param.equipment = "C-76";
        param.name = "C-76";
        // when
        var events = eventController.getEvents(
                1,
                10,
                null,
                null,
                param);
        //then
        assertThat(events).extracting("equipment").extracting("name").containsExactly("C-763", "C-762", "C-762");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_search_events_with_hyperviseur_source() {
        // given
        var param = new EventController.EventParameters();
        param.source = List.of("Hyperviseur");
        // when
        var events = eventController.getEvents(
                1,
                10,
                null,
                null,
                param);
        //then
        assertThat(events).extracting("externalSourceRef").containsExactly("Hyperviseur", "Hyperviseur", "Hyperviseur");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_search_events_with_known_source() {
        // given
        var param = new EventController.EventParameters();
        param.source = List.of("citylinx");
        // when
        var events = eventController.getEvents(
                1,
                10,
                null,
                null,
                param);
        //then
        assertThat(events).extracting("externalSourceRef").containsExactly("citylinx");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_search_events_with_unknown_source() {
        // given
        var param = new EventController.EventParameters();
        param.source = List.of("unknown");
        // when
        var events = eventController.getEvents(
                1,
                10,
                null,
                null,
                param);
        //then
        assertThat(events).isEmpty();
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_search_events_with_hyperviseur_known_source() {
        // given
        var param = new EventController.EventParameters();
        param.source = List.of("Hyperviseur", "citylinx");
        // when
        var events = eventController.getEvents(
                1,
                10,
                null,
                null,
                param);
        //then
        assertThat(events).extracting("externalSourceRef").containsExactly("citylinx", "Hyperviseur", "Hyperviseur",
                "Hyperviseur");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_search_events_with_hyperviseur_unknown_source() {
        // given
        var param = new EventController.EventParameters();
        param.source = List.of("Hyperviseur", "unknown");
        // when
        var events = eventController.getEvents(
                1,
                10,
                null,
                null,
                param);
        //then
        assertThat(events).extracting("externalSourceRef").containsExactly("Hyperviseur", "Hyperviseur", "Hyperviseur");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_get_event_by_source_and_external_id() {
        // given
        var externalEvent = dataService.getExternalEvent();
        // when
        var event = eventController.getEventDetailsBySourceAndExternalId(externalEvent.getExternalSourceRef(),
                externalEvent.getExternalId());
        //then
        assertThat(event).isNotNull();
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read", "event_write" })
    void should_return_event_closed_from_close_date_and_not_before() {
        // given
        var event = new EventWriteDto(null,
                "closed",
                "desc",
                HIGH,
                "LIMIT",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "creator");
        var createdEvent = eventController.saveEvent(event);
        eventController.closeEvent(createdEvent.getId(), new CommentWriteDto(UUID.randomUUID(), "closed"));

        var param = new EventController.EventParameters();
        var closeDate = Instant.now().plus(Period.ofDays(1));
        param.closeDate = closeDate;

        // when
        var events = eventController.getEvents(
                1,
                1,
                null,
                null,
                param);
        //then
        assertThat(events).hasSize(1);
        assertThat(events.stream().toList().getFirst().closeDate()).isAfter(closeDate);
    }

}