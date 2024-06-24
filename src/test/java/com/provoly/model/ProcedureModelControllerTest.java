package com.provoly.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.NoSuchElementException;

import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;

import com.provoly.TestDataService;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProcedureModelControllerTest {
    @Inject
    ProcedureModelController procedureModelController;

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
    void should_return_procedure_model_list_default_sort() {
        // when
        var procedures = procedureModelController.getProceduresModel(1, 3, null, null, List.of(), null);

        //then
        assertThat(procedures).extracting("name").containsExactly("flora model2", "model", "model1");
    }

    @Test
    @TestSecurity(user = "reader")
    void should_return_procedure_model_that_contains_flo_value() {
        // when
        var procedures = procedureModelController.getProceduresModel(1, 3, null, null, List.of(), "flo");

        //then
        assertThat(procedures).extracting("name").containsExactly("flora model2", "model", "model1");
    }

    @Test
    @TestSecurity(user = "reader")
    void should_return_procedure_model_that_contains_flo_value_sort_on_id_desc() {
        // when
        var procedures = procedureModelController.getProceduresModel(1, 3, "id", "DESC", List.of(), "flo");

        //then
        assertThat(procedures).extracting("name").containsExactly("flora model2", "model1", "model");
    }

    @Test
    @TestSecurity(user = "reader")
    void should_return_procedure_model_that_contains_flo_value_with_domain_EP() {
        // when
        var procedures = procedureModelController.getProceduresModel(1, 3, null, null, List.of("EP"), "flo");

        //then
        assertThat(procedures).extracting("name").containsExactly("flora model2", "model");
    }

    @Test
    @TestSecurity(user = "reader")
    void should_return_procedure_model_empty_list_when_filter_on_vp_domain() {
        // when
        var procedures = procedureModelController.getProceduresModel(1, 3, null, null, List.of("VP"), null);

        //then
        assertThat(procedures).hasSize(1);
    }

    @Test
    @TestSecurity(user = "reader")
    void should_throw_procedure_not_found() {
        assertThatThrownBy(() -> procedureModelController.getProcedureModelDetails(666))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @TestSecurity(user = "reader")
    void should_save_procedure_model() {
        // given
        var procedureModelToSave = new ProcedureModelWriteDto(null, "new proc model", "desc", "EP", "bloom", List.of());
        // when

        var savedProcedureModel = procedureModelController.saveProcedureModel(procedureModelToSave);

        //then
        assertThat(savedProcedureModel).extracting("id").isNotNull();
    }

    @Test
    @TestSecurity(user = "reader")
    void should_udpate_procedure_model() {
        // given
        var id = procedureModelController.getProceduresModel(1, 1, null, null, List.of(), "flora model2")
                .stream()
                .findFirst()
                .get()
                .id();
        var procedureModelToUpdate = new ProcedureModelWriteDto(id, "flora model2", "desc updated", "EP", "stella", List.of());

        // when
        procedureModelController.updateProcedureModel(id, procedureModelToUpdate);
        var udpatedProcedureModel = procedureModelController.getProcedureModelDetails(id);

        //then
        assertThat(udpatedProcedureModel).extracting("description").isEqualTo("desc updated");
    }

    @Test
    @TestSecurity(user = "reader")
    void should_throw_associate_procedure_model_invalid_id() {
        assertThatThrownBy(() -> procedureModelController.associateProcedureModelToEvents(666, List.of(666)))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @TestSecurity(user = "reader")
    void should_throw_associate_procedure_invalid_event_id() {
        // given
        var dto = new ProcedureModelWriteDto(null, "my model", "desc", "EP", "techna", List.of());
        var id = procedureModelController.saveProcedureModel(dto).id();

        assertThatThrownBy(() -> procedureModelController.associateProcedureModelToEvents(id, List.of(666)))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @TestSecurity(user = "reader")
    void should_throw_associate_procedure_event_already_associated_to_procedure() {
        // given
        var dto = new ProcedureModelWriteDto(null, "my model 2", "desc", "EP", "tecna", List.of());
        var aleardyAssociateEventId = dataService.getProcedure1().getEvents().getFirst().getId();

        var id = procedureModelController.saveProcedureModel(dto).id();

        assertThatThrownBy(() -> procedureModelController.associateProcedureModelToEvents(id, List.of(aleardyAssociateEventId)))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining(" is already associated to a procedure");
    }

    @Test
    @TestSecurity(user = "reader")
    void should_increment_model_useCount_when_associate_procedure_to_events() {
        // given
        var dto = new ProcedureModelWriteDto(null, "my model 3", "desc", "EP", "tecna", List.of());

        var model = procedureModelController.saveProcedureModel(dto);
        var event = dataService.getEvent1();

        // when
        var procedure = procedureModelController.associateProcedureModelToEvents(model.id(), List.of(event.getId()));
        model = procedureModelController.getProcedureModelDetails(model.id());

        // then
        assertThat(model.useCount()).isEqualTo(1);
    }

}
