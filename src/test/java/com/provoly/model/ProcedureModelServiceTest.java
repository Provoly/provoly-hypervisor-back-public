package com.provoly.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import jakarta.inject.Inject;

import com.provoly.TestDataService;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProcedureModelServiceTest {

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
    @TestSecurity(user = "reader")
    void should_throw_invalid_domain_when_list_procedure_model() {
        // when
        assertThatThrownBy(() -> procedureModelService.getProceduresModel(1, 3, null, null, List.of("toto"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @TestSecurity(user = "reader")
    void should_throw_invalid_sort_when_list_procedure_model() {
        // when
        assertThatThrownBy(() -> procedureModelService.getProceduresModel(1, 3, "toto", null, List.of(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not possible to sort");
    }

    @Test
    @TestSecurity(user = "reader")
    void should_throw_invalid_domain_when_save_procedure_model() {
        // given
        var procedureModelToSave = new ProcedureModelWriteDto(null, "invalid domain proc model", "desc", "toto", "bloom",
                List.of());

        // when
        assertThatThrownBy(() -> procedureModelService.saveProcedureModel(procedureModelToSave))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @TestSecurity(user = "reader")
    void should_throw_name_already_exists_when_save_procedure_model() {
        // given
        var procedureModelToSave = new ProcedureModelWriteDto(null, "model1", "desc", "EP", "tecna", List.of());

        // when
        assertThatThrownBy(() -> procedureModelService.saveProcedureModel(procedureModelToSave))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exist");
    }

    @Test
    @TestSecurity(user = "reader")
    void should_throw_creator_is_changed_when_udpate_procedure_model() {
        // given
        var id = procedureModelService.getProceduresModel(1, 1, null, null, List.of(), "flora model2")
                .stream()
                .findFirst()
                .get()
                .getId();

        var procedureModelToUpdate = new ProcedureModelWriteDto(id, "proc model1 updated", "desc", "EP", "musa", List.of());

        // when
        assertThatThrownBy(() -> procedureModelService.updateProcedureModel(id, procedureModelToUpdate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("It's not possible to update Procedure model creator");
    }
}
