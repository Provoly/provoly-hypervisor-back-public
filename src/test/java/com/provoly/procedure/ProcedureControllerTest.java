package com.provoly.procedure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import jakarta.inject.Inject;

import com.provoly.TestDataService;
import com.provoly.event.Category;
import com.provoly.event.Criticality;
import com.provoly.event.EventController;
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
        UUID id = dataService.getProcedureId1();
        // when
        var procedure = procedureController.getProcedureDetail(id);

        //then
        assertThat(procedure).extracting("id").isEqualTo(id);
        assertThat(procedure.events()).hasSize(1);
    }

    @Test
    @TestSecurity(user = "reader")
    void should_throw_procedure_not_found() {
        assertThatThrownBy(() -> procedureController.getProcedureDetail(UUID.randomUUID()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @TestSecurity(user = "reader")
    void should_update_report_event_in_procedure() {
        // given
        UUID eventReportId = eventController
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
                "external_source",
                null);

        UUID procedureId = dataService.getProcedureId3();
        ProcedureWriteDto dto = new ProcedureWriteDto(
                procedureId,
                "procedure maintenance",
                List.of(reportDto));

        // when
        procedureController.updateProcedure(dto);
        var updatedEvent = eventController.getEventDetails(eventReportId);

        // then
        assertThat(updatedEvent)
                .extracting("name", "address")
                .containsExactly("Maintenance ouvrage updated", "new address");

    }
}
