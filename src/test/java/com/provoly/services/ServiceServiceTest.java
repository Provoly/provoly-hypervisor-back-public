package com.provoly.services;

import static com.provoly.service.ServiceStatus.ASKED;
import static com.provoly.service.ServiceStatus.DONE;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import jakarta.inject.Inject;

import com.provoly.TestDataService;
import com.provoly.equipment.EquipmentService;
import com.provoly.service.ServiceService;
import com.provoly.service.ServiceWriteDto;

import io.quarkus.test.junit.QuarkusTest;

import org.assertj.core.api.Assertions;
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
    TestDataService testDataService;

    @BeforeEach
    public void init() {
        testDataService.init();
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
        Assertions.assertThat(equipment.getServices()).hasSize(1);

    }

    @Test
    void should_throw_when_missing_close_date_on_done_service() {
        // Given
        var service = new ServiceWriteDto("technical_id", "",
                "A-230", Instant.now(), Instant.now(), Instant.now(), Instant.now(), null, "EP", DONE, "invalid");

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
}
