package com.provoly.procedure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;

import com.provoly.TestDataService;
import com.provoly.action.AskedService;
import com.provoly.action.TodoAction;
import com.provoly.equipment.EquipmentService;
import com.provoly.event.Category;
import com.provoly.event.Criticality;
import com.provoly.event.EventService;
import com.provoly.event.Status;
import com.provoly.event.dto.AlertEventWriteDto;
import com.provoly.event.dto.ReportEventWriteDto;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProcedureServiceTest {
    @Inject
    ProcedureService procedureService;

    @Inject
    EventService eventService;

    @Inject
    EquipmentService equipmentService;

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
    void procedure_progress_action_should_return_half_terminated() {
        // given
        var intervention = new AskedService(UUID.randomUUID(), Instant.now(), Status.NEW, "my_intervention");
        var todo = new TodoAction(UUID.randomUUID(), Instant.now(), Status.DONE, "my_todo");

        Procedure procedure = new Procedure(UUID.randomUUID(), "my_procedure");
        procedure.addAction(intervention);
        procedure.addAction(todo);

        // when
        var result = procedure.getProcedureProgress();

        //then
        assertThat(result).isEqualTo(50.0f);
    }

    @Test
    void procedure_progress_action_should_return_parsed_long() {
        // given
        var intervention = new AskedService(UUID.randomUUID(), Instant.now(), Status.NEW, "my_intervention");
        var intervention2 = new AskedService(UUID.randomUUID(), Instant.now(), Status.NEW, "my_intervention2");
        var todo = new TodoAction(UUID.randomUUID(), Instant.now(), Status.DONE, "my_todo");

        Procedure procedure = new Procedure(UUID.randomUUID(), "my_procedure");
        procedure.addAction(intervention);
        procedure.addAction(intervention2);
        procedure.addAction(todo);

        // when
        var result = procedure.getProcedureProgress();

        //then
        assertThat(result).isEqualTo(33.0f);
    }

    @Test
    void procedure_progress_action_should_return_none_when_no_actions() {
        // given
        Procedure procedure = new Procedure(UUID.randomUUID(), "my_procedure");

        // when
        var result = procedure.getProcedureProgress();

        //then
        assertThat(result).isZero();
    }

    @Test
    void procedure_progress_action_should_return_none_when_only_no_done_actions() {
        // given
        var intervention = new AskedService(UUID.randomUUID(), Instant.now(), Status.IN_PROGRESS,
                "my_intervention");
        var todo = new TodoAction(UUID.randomUUID(), Instant.now(), Status.NEW, "my_todo");

        Procedure procedure = new Procedure(UUID.randomUUID(), "my_procedure");
        procedure.addAction(intervention);
        procedure.addAction(todo);

        // when
        var result = procedure.getProcedureProgress();

        //then
        assertThat(result).isZero();
    }

    @Test
    void should_close_all_procedure_events() {
        // given
        var procedureId = dataService.getProcedureId1();

        // when
        procedureService.closeAllProcedureEvents(procedureId);

        //then
        assertThat(procedureService.getProcedureDetails(procedureId).getEvents()).extracting("status")
                .containsExactly(Status.DONE);
    }

    @Test
    void should_update_events_procedure_except_alert_type() {
        // given
        var equip = equipmentService.getEquipmentByName("A-230");

        var report = new ReportEventWriteDto(UUID.randomUUID(), "report event", "", Criticality.HIGH, "", null,
                Category.REPORT, "", "EP");
        var alert = new AlertEventWriteDto(UUID.randomUUID(), "alert event", "", Criticality.HIGH, "",
                equip.getId(), Category.MALFUNCTION, "", "EP");
        var procedureId = UUID.randomUUID();

        eventService.saveOrUpdateEvent(report);
        eventService.saveOrUpdateEvent(alert);

        procedureService.saveProcedure(procedureId, List.of(report.getId(), alert.getId()));

        // when
        ReportEventWriteDto reportUpdated = new ReportEventWriteDto(report.getId(), "report event updated", "",
                Criticality.HIGH, "", null, Category.REPORT, "", "EP");
        AlertEventWriteDto alertUpdated = new AlertEventWriteDto(UUID.randomUUID(), "alert event updated", "", Criticality.HIGH,
                "", equip.getId(), Category.MALFUNCTION, "", "EP");

        var procedureWriteUpdated = new ProcedureWriteDto(procedureId, "procedure with alert and report",
                List.of(reportUpdated, alertUpdated));
        procedureService.updateProcedure(procedureWriteUpdated);

        //then
        assertThat(procedureService.getProcedureDetails(procedureId).getEvents()).extracting("name")
                .containsExactlyInAnyOrder("report event updated", "alert event");
    }

}
