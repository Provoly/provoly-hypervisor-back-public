package com.provoly.procedure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.provoly.model.ProcedureModelWriteDto;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;

import com.provoly.TestDataService;
import com.provoly.action.AskedService;
import com.provoly.action.TodoAction;
import com.provoly.event.Status;

import io.quarkus.test.junit.QuarkusTest;

import jakarta.ws.rs.ForbiddenException;
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
        var intervention = new AskedService(UUID.randomUUID(), Instant.now(), Status.NEW, "my_intervention");
        var intervention2 = new AskedService(UUID.randomUUID(), Instant.now(), Status.NEW, "my_intervention2");
        var todo = new TodoAction(UUID.randomUUID(), Instant.now(), Status.DONE, "my_todo");

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
    void procedure_progress_action_should_return_none_when_only_no_done_actions() {
        // given
        var intervention = new AskedService(UUID.randomUUID(), Instant.now(), Status.IN_PROGRESS,
                "my_intervention");
        var todo = new TodoAction(UUID.randomUUID(), Instant.now(), Status.NEW, "my_todo");

        Procedure procedure = new Procedure("my_procedure", "desc");
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
        var procedureId = dataService.getProcedure1().getId();

        // when
        procedureService.closeAllProcedureEvents(procedureId);

        //then
        assertThat(procedureService.getProcedureDetails(procedureId).getEvents()).extracting("status")
                .containsExactly(Status.DONE);
    }

    @Test
    @TestSecurity(user = "reader")
    void should_set_event_in_progress_when_add_event_to_procedure() {
        // given
        var procedure = dataService.getProcedure1();
        var newEvent = dataService.getEvent1();

        // when
        procedure.addEvent(newEvent);

        //then
        assertThat(newEvent.getStatus()).isEqualTo(Status.IN_PROGRESS);
    }
}
