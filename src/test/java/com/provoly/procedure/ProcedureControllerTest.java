package com.provoly.procedure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import jakarta.inject.Inject;

import com.provoly.event.Criticality;
import com.provoly.event.EventDatabaseReader;
import com.provoly.event.ReportCategory;
import com.provoly.event.dto.ReportEventWriteDto;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;

import org.junit.jupiter.api.Test;

@QuarkusTest
public class ProcedureControllerTest {
    @Inject
    ProcedureController procedureController;

    @Inject
    EventDatabaseReader databaseReader;

    @Test
    @TestSecurity(user = "reader")
    void should_return_procedure_by_id() {
        // given
        final UUID id = UUID.fromString("5c925baf-7164-4bfe-af37-f0860aacc570");

        // when
        var procedure = procedureController.getProcedureDetail(id);

        //then
        assertThat(procedure).extracting("id").isEqualTo(id);
        assertThat(procedure.events()).hasSize(2);
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
        final UUID eventReportId = UUID.fromString("eb8e2f6b-f33e-408c-bb6c-e042f204d687");
        ReportEventWriteDto reportDto = new ReportEventWriteDto(eventReportId,
                "Maintenance ouvrage updated",
                "description",
                Criticality.MEDIUM,
                "new address",
                null,
                ReportCategory.REPORT,
                "external_source3",
                null);

        UUID procedureId = UUID.fromString("5c925baf-7164-4bfe-af37-f0860aacc570");
        ProcedureWriteDto dto = new ProcedureWriteDto(
                procedureId,
                "procedure maintenance",
                List.of(reportDto));

        // when
        procedureController.updateProcedure(dto);
        var updatedEvent = databaseReader.getEventById(eventReportId);

        // then
        assertThat(updatedEvent)
                .extracting("name", "address")
                .containsExactly("Maintenance ouvrage updated", "new address");

    }
}
