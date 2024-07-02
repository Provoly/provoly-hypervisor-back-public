package com.provoly.procedure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;

import com.provoly.TestDataService;
import com.provoly.action.dto.ActionWriteDto;
import com.provoly.action.dto.EmailActionWriteDto;
import com.provoly.action.dto.OtherActionWriteDto;
import com.provoly.action.dto.PhoneActionWriteDto;
import com.provoly.event.*;
import com.provoly.event.dto.ReportEventWriteDto;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProcedureControllerTest {
    @Inject
    ProcedureController procedureController;

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
    @TestSecurity(user = "reader")
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
    @TestSecurity(user = "reader")
    void should_throw_procedure_not_found() {
        assertThatThrownBy(() -> procedureController.getProcedureDetails(666))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @TestSecurity(user = "reader")
    void should_update_report_event_in_procedure() {
        // given
        var eventReportId = eventController
                .getEvents(1, 1, null, null, null, List.of(Criticality.HIGH.name()), List.of(),
                        List.of(Category.REPORT.name()),
                        List.of(), List.of())
                .stream()
                .toList()
                .getFirst()
                .getId();

        ReportEventWriteDto reportDto = new ReportEventWriteDto(eventReportId,
                "Maintenance ouvrage updated",
                "description",
                Criticality.HIGH,
                "new address",
                null,
                Category.REPORT,
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
        procedureController.updateProcedure(procedureId, dto);
        var updatedEvent = eventController.getEventDetails(eventReportId);

        // then
        assertThat(updatedEvent)
                .extracting("name", "address")
                .containsExactly("Maintenance ouvrage updated", "new address");

    }

    @Test
    @TestSecurity(user = "reader")
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
    @TestSecurity(user = "reader")
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
    @TestSecurity(user = "reader")
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
    @TestSecurity(user = "reader")
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
    @TestSecurity(user = "reader")
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
}
