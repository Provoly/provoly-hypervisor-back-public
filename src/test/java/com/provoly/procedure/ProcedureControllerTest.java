package com.provoly.procedure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.ForbiddenException;

import com.provoly.TestDataService;
import com.provoly.action.dto.*;
import com.provoly.comment.CommentWriteDto;
import com.provoly.event.Criticality;
import com.provoly.event.Event;
import com.provoly.event.EventController;
import com.provoly.event.Status;
import com.provoly.event.dto.ExternalEventWriteDto;
import com.provoly.event.dto.InternalEventWriteDto;
import com.provoly.user.Role;
import com.provoly.user.UserService;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;

import org.junit.jupiter.api.*;

@QuarkusTest
public class ProcedureControllerTest {
    @Inject
    ProcedureController procedureController;

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
        given(mock.hasRole(Role.STR_EVENT_WRITE)).willReturn(true);
    }

    @AfterEach
    public void clean() {
        dataService.clean();
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_return_procedure_by_id() {
        // given
        var id = dataService.getProcedure1().getId();
        // when
        var procedure = procedureController.getProcedureDetails(id);

        //then
        assertThat(procedure).extracting("id").isEqualTo(id);
        assertThat(procedure.events()).hasSize(1);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read" })
    void should_throw_procedure_not_found() {
        assertThatThrownBy(() -> procedureController.getProcedureDetails(666))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_read", "event_write" })
    void should_not_update_external_event_in_procedure() {
        // given
        var param = new EventController.EventParameters();
        param.criticality = List.of(Criticality.HIGH.name());
        param.category = List.of("OUTOFORDER");
        var event = eventController
                .getEvents(1, 1, null, null, param)
                .stream()
                .toList()
                .getFirst();

        var reportDto = new ExternalEventWriteDto(event.id(),
                "Maintenance ouvrage updated",
                "description",
                Criticality.HIGH,
                "OUTOFORDER",
                null,
                "new address",
                null,
                null,
                null,
                null,
                null,
                null,
                "external_source_ref",
                null);

        Integer procedureId = dataService.getProcedure3().getId();
        ProcedureWriteDto dto = new ProcedureWriteDto(
                procedureId,
                "procedure maintenance",
                "desc",
                List.of(reportDto),
                List.of());

        // when
        assertThatThrownBy(() -> procedureController.updateProcedure(procedureId, dto))
                .isInstanceOf(ForbiddenException.class);

    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_write", "event_read" })
    void should_update_event_in_procedure() {
        // given
        var param = new EventController.EventParameters();
        param.criticality = List.of(Criticality.MEDIUM.name());
        param.category = List.of("OUTOFORDER");
        var eventId = eventController
                .getEvents(1, 1, null, null, param)
                .stream()
                .toList()
                .getFirst()
                .id();

        var reportDto = new InternalEventWriteDto(eventId,
                "Maintenance ouvrage updated",
                "description",
                Criticality.HIGH,
                "OUTOFORDER",
                null,
                "new address",
                null,
                null,
                null,
                null,
                null,
                null,
                "creator");

        var procedure = dataService.getProcedure1();

        List<ActionWriteDto> actionWrite = procedure.getActions()
                .stream()
                .map(a -> new ActionWriteDto(a.getId(), "ASKED_SERVICE", a.getStatus()))
                .toList();
        ProcedureWriteDto dto = new ProcedureWriteDto(
                procedure.getId(),
                "procedure maintenance",
                "desc",
                List.of(reportDto),
                actionWrite);

        // when
        procedureController.updateProcedure(procedure.getId(), dto);
        var updatedEvent = eventController.getEventDetails(eventId);

        // then
        assertThat(updatedEvent)
                .extracting("name", "address")
                .containsExactly("Maintenance ouvrage updated", "new address");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_write", "event_read" })
    void should_reset_event_status_when_delete_procedure() {
        // given
        var procedure = dataService.getProcedure3();
        var eventsId = procedure.getEvents().stream().map(Event::getId).toList();

        // when
        procedureController.deleteProcedure(procedure.getId());
        var resetEvents = eventsId.stream().map(eventId -> eventController.getEventDetails(eventId)).toList();

        // then
        assertThat(resetEvents)
                .extracting("status")
                .containsExactly(Status.NEW, Status.NEW);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_proc_write" })
    void should_throw_action_invalid_email() {
        // given
        Integer procedureId = dataService.getProcedure3().getId();
        ProcedureWriteDto dto = new ProcedureWriteDto(
                procedureId,
                "procedure maintenance",
                "desc",
                List.of(),
                List.of(new EmailActionWriteDto(UUID.randomUUID(), "EMAIL", Status.NEW, "toto", "invalid@")));

        // when
        assertThatThrownBy(() -> procedureController.updateProcedure(procedureId, dto))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("must be a well-formed email address");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_proc_write" })
    void should_throw_action_invalid_number() {
        // given
        Integer procedureId = dataService.getProcedure3().getId();
        ProcedureWriteDto dto = new ProcedureWriteDto(
                procedureId,
                "procedure maintenance",
                "desc",
                List.of(),
                List.of(new PhoneActionWriteDto(UUID.randomUUID(), "SMS", Status.NEW, "toto", "toto")));

        // when
        assertThatThrownBy(() -> procedureController.updateProcedure(procedureId, dto))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("'number' must match ");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_proc_write" })
    void should_throw_action_invalid_type() {
        // given
        Integer procedureId = dataService.getProcedure3().getId();
        ProcedureWriteDto dto = new ProcedureWriteDto(
                procedureId,
                "procedure maintenance",
                "desc",
                List.of(),
                List.of(new ActionWriteDto(UUID.randomUUID(), "TOTO", Status.NEW)));

        // when
        assertThatThrownBy(() -> procedureController.updateProcedure(procedureId, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid action type");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_proc_write" })
    void should_throw_action_invalid_property() {
        // given
        Integer procedureId = dataService.getProcedure3().getId();
        ProcedureWriteDto dto = new ProcedureWriteDto(
                procedureId,
                "procedure maintenance",
                "desc",
                List.of(),
                List.of(new OtherActionWriteDto(UUID.randomUUID(), "OTHER", Status.NEW, null)));

        // when
        assertThatThrownBy(() -> procedureController.updateProcedure(procedureId, dto))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("name: must not be null");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_write", "event_read" })
    void should_throw_forbiden_when_update_action_from_procedure_without_event_proc_write() {
        // given
        Integer procedureId = dataService.getProcedure1().getId();
        ProcedureWriteDto dto = new ProcedureWriteDto(
                procedureId,
                "procedure maintenance",
                "desc",
                List.of(),
                List.of());

        // then
        assertThatThrownBy(() -> procedureController.updateProcedure(procedureId, dto))
                .isInstanceOf(io.quarkus.security.ForbiddenException.class)
                .hasMessageContaining("Missing permission to add");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_proc_write" })
    void should_throw_forbiden_when_terminate_action_from_procedure_without_event_proc_write() {
        // given
        given(mock.hasRole(Role.STR_EVENT_WRITE)).willReturn(false);
        var procedure = dataService.getProcedure1();
        List<ActionWriteDto> actionWrite = procedure.getActions()
                .stream()
                .map(a -> new ActionWriteDto(a.getId(), "ASKED_SERVICE", Status.DONE))
                .toList();

        ProcedureWriteDto dto = new ProcedureWriteDto(
                procedure.getId(),
                "procedure maintenance",
                "desc",
                List.of(),
                actionWrite);

        // then
        assertThatThrownBy(() -> procedureController.updateProcedure(procedure.getId(), dto))
                .isInstanceOf(io.quarkus.security.ForbiddenException.class)
                .hasMessageContaining("Missing permission to update action status");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_write", "event_read" })
    void should_close_procedure() {
        // given
        var procedureId = dataService.getProcedure1().getId();
        var closedComment = new CommentWriteDto(UUID.randomUUID(), "close procedure");

        // when
        procedureController.closeAllProcedureEvents(procedureId, closedComment);
        var procedure = procedureController.getProcedureDetails(procedureId);

        // then
        assertThat(procedure.closeComment()).isNotNull();
        assertThat(procedure.events()).extracting("status").containsExactly(Status.DONE);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_write" })
    void should_throw_error_procedure_already_done() {
        // given
        var procedureId = dataService.getProcedure1().getId();
        var closedComment = new CommentWriteDto(UUID.randomUUID(), "close procedure");

        procedureController.closeAllProcedureEvents(procedureId, closedComment);

        // then
        assertThatThrownBy(() -> procedureController.closeAllProcedureEvents(procedureId, closedComment))
                .isInstanceOf(jakarta.ws.rs.ForbiddenException.class);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_write", "event_proc_write" })
    void should_throw_error_when_add_close_event_to_procedure() {
        // given
        var procedureId = dataService.getProcedure1().getId();
        var event = dataService.getDoneEvent();

        // then
        assertThatThrownBy(() -> procedureController.addEventToProcedure(procedureId, event.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_write" })
    void should_throw_error_when_add_event_to_closed_procedure() {
        // given
        var procedureId = dataService.getProcedure1().getId();

        // when
        procedureController.closeAllProcedureEvents(procedureId, new CommentWriteDto(UUID.randomUUID(), "message"));

        // then
        assertThatThrownBy(() -> procedureController.addEventToProcedure(procedureId, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @TestSecurity(user = "reader", roles = { "event_write", "event_read" })
    void should_add_event_to_procedure() {
        // given
        var procedureId = dataService.getProcedure1().getId();
        var event = dataService.getEvent1();

        // when
        procedureController.addEventToProcedure(procedureId, event.getId());

        // then
        var updatedProc = procedureController.getProcedureDetails(procedureId);
        assertThat(updatedProc.events()).extracting("id").contains(event.getId());
    }
}
