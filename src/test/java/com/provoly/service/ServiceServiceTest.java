package com.provoly.service;

import static com.provoly.service.ServiceStatus.ASKED;
import static com.provoly.service.ServiceStatus.DONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import jakarta.inject.Inject;

import com.provoly.TestDataService;
import com.provoly.action.AskedService;
import com.provoly.equipment.EquipmentService;
import com.provoly.procedure.ProcedureService;
import com.provoly.service.coswin.CoswinClient;
import com.provoly.service.coswin.CoswinServiceWriteDto;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ServiceServiceTest {

    @Inject
    ServiceService serviceService;

    @Inject
    EquipmentService equipmentService;

    @Inject
    ProcedureService procedureService;

    @InjectMock
    CoswinClient mock;

    @Inject
    TestDataService testDataService;

    @BeforeEach
    public void init() throws IOException {
        testDataService.init();
        given(mock.sendExternalService(any(CoswinServiceWriteDto.class))).willReturn("externalId");
    }

    @AfterEach
    public void clean() {
        testDataService.clean();
    }

    @Test
    void should_set_service_in_equipment_when_create_service() {
        // Given
        var service = new ServiceWriteDto("technical_id", "",
                "A-230", Instant.now(), Instant.now(), Instant.now(), Instant.now(), null, "EP", ASKED, "CURA");
        serviceService.saveOrUpdateServices(List.of(service));

        // When
        var equipment = equipmentService.getEquipmentByName("A-230");
        assertThat(equipment.getServices()).hasSize(1);

    }

    @Test
    void should_throw_when_missing_close_date_on_done_service() {
        // Given
        var service = new ServiceWriteDto("technical_id", "",
                "A-230", Instant.now(), Instant.now(), Instant.now(), Instant.now(), null, "EP", DONE, "CURA");

        // When
        assertThatThrownBy(() -> serviceService.saveOrUpdateServices(List.of(service)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("without closeDate");
    }

    @Test
    void should_throw_when_missing_start_date_on_asked_service() {
        // Given
        var service = new ServiceWriteDto("technical_id", "",
                "A-230", Instant.now(), Instant.now(), null, Instant.now(), null, "EP", ASKED, "CURA");

        // When
        assertThatThrownBy(() -> serviceService.saveOrUpdateServices(List.of(service)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("without startDate or endDate");
    }

    @Test
    void should_throw_when_action_not_found() {
        // Given
        var externalService = new ExternalServiceWriteDto("name", null, "1-MINEUR", "Luminaire - Accidente menacant de tomber",
                "desc", "EP");

        // When
        assertThatThrownBy(() -> serviceService.createExternalService(UUID.randomUUID(), externalService))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void should_throw_when_equipment_not_found() {
        // Given
        var externalService = new ExternalServiceWriteDto("name", UUID.randomUUID(), "1-MINEUR",
                "Luminaire - Accidente menacant de tomber", "desc", "EP");
        var proc = testDataService.getProcedure1();
        var action = proc.getActions().stream().toList().getFirst();

        // When
        assertThatThrownBy(() -> serviceService.createExternalService(action.getId(), externalService))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void should_throw_when_action_already_linked_with_external_service() {
        // Given
        var externalService = new ExternalServiceWriteDto("name", null, "1-MINEUR", "Luminaire - Accidente menacant de tomber",
                "desc", "EP");
        var proc = testDataService.getProcedure3();
        var action = (AskedService) proc.getActions().stream().toList().getFirst();

        // When
        assertThatThrownBy(() -> serviceService.createExternalService(action.getId(), externalService))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_throw_when_invalid_priority() {
        // Given
        var externalService = new ExternalServiceWriteDto("name", null, "toto", "Luminaire - Accidente menacant de tomber",
                "desc", "EP");
        var proc = testDataService.getProcedure1();
        var action = proc.getActions().stream().toList().getFirst();

        // When
        assertThatThrownBy(() -> serviceService.createExternalService(action.getId(), externalService))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_throw_when_invalid_type() {
        // Given
        var externalService = new ExternalServiceWriteDto("name", null, "1-MINEUR", "toto", "desc", "EP");
        var proc = testDataService.getProcedure1();
        var action = proc.getActions().stream().toList().getFirst();

        // When
        assertThatThrownBy(() -> serviceService.createExternalService(action.getId(), externalService))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_set_external_id_in_action() throws IOException {
        // Given
        var externalService = new ExternalServiceWriteDto("name", null, "1-MINEUR", "Luminaire - Accidente menacant de tomber",
                "desc", "EP");
        var proc = testDataService.getProcedure1();
        var action = proc.getActions().stream().toList().getFirst();

        // When
        var id = serviceService.createExternalService(action.getId(), externalService);

        // Then
        var updatedAction = (AskedService) procedureService.getProcedureDetails(proc.getId()).getActions().stream().toList()
                .getFirst();
        assertThat(updatedAction.getServiceExternalId()).isEqualTo(id);
    }
}
