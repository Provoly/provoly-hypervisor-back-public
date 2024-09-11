package com.provoly.procedure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;

import com.provoly.TestDataService;
import com.provoly.action.AskedService;
import com.provoly.action.OtherAction;
import com.provoly.action.dto.OtherActionWriteDto;
import com.provoly.event.Status;
import com.provoly.model.ProcedureModelService;
import com.provoly.model.ProcedureModelWriteDto;

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
    ProcedureModelService procedureModelService;

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
        var intervention = new AskedService(UUID.randomUUID(), Status.NEW, "my_intervention", 1);
        var todo = new OtherAction(UUID.randomUUID(), Status.DONE, "other action", 2);

        Procedure procedure = new Procedure("my_procedure", "desc");
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
        var intervention = new AskedService(UUID.randomUUID(), Status.NEW, "my_intervention", 1);
        var intervention2 = new AskedService(UUID.randomUUID(), Status.NEW, "my_intervention2", 2);
        var todo = new OtherAction(UUID.randomUUID(), Status.DONE, "other action", 3);

        Procedure procedure = new Procedure("my_procedure", "desc");
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
        Procedure procedure = new Procedure("my_procedure", "desc");

        // when
        var result = procedure.getProcedureProgress();

        //then
        assertThat(result).isZero();
    }

    @Test
    void procedure_progress_action_should_return_none_when_only_undone_actions() {
        // given
        var service = new AskedService(UUID.randomUUID(), Status.IN_PROGRESS, "my_intervention", 1);
        var todo = new OtherAction(UUID.randomUUID(), Status.NEW, "other action", 2);

        Procedure procedure = new Procedure("my_procedure", "desc");
        procedure.addAction(service);
        procedure.addAction(todo);

        // when
        var result = procedure.getProcedureProgress();

        //then
        assertThat(result).isZero();
    }

    @Test
    void should_close_all_procedure_events() {
        // given
        var procedureId = dataService.getProcedure1().getId();

        // when
        procedureService.closeAllProcedureEventsById(procedureId);

        //then
        assertThat(procedureService.getProcedureDetails(procedureId).getEvents()).extracting("status")
                .containsExactly(Status.DONE);
    }

    @Test
    // @TestSecurity(user = "reader")
    void should_set_event_in_progress_when_add_event_to_procedure() {
        // given
        var procedure = dataService.getProcedure1();
        var newEvent = dataService.getEvent1();

        // when
        procedure.addEvent(newEvent);

        //then
        assertThat(newEvent.getStatus()).isEqualTo(Status.IN_PROGRESS);
    }

    @Test
    //   @TestSecurity(user = "reader")
    void should_duplicate_action_when_associate_procedure_to_events() {
        // given
        var actionModelId = UUID.randomUUID();
        var dto = new ProcedureModelWriteDto(null, "my model 6", "desc", "EP", "tecna",
                List.of(new OtherActionWriteDto(actionModelId, "OTHER", Status.NEW, "other")));

        var model = procedureModelService.saveProcedureModel(dto);
        var event = dataService.getEvent1();
        var procedureId = procedureModelService.associateProcedureModelToEvents(model.getId(), List.of(event.getId())).getId();

        // when
        var procedure = procedureService.getProcedureDetails(procedureId);

        // then
        assertThat(procedure.getActions()).hasSize(1);
        assertThat(procedure.getActions().stream().toList().getFirst()).extracting("id").isNotEqualTo(actionModelId);
    }
}
