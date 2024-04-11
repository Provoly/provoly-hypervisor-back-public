package com.provoly.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import jakarta.inject.Inject;

import com.provoly.TestDataService;
import com.provoly.action.Service;
import com.provoly.equipment.EquipmentService;
import com.provoly.event.Status;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.*;

@QuarkusTest
public class MetricsServiceTest {

    @Inject
    MetricsService metricsService;

    @Inject
    EquipmentService equipmentService;

    @Inject
    TestDataService dataService;

    @BeforeEach
    public void init() {
        dataService.init();
    }

    @AfterEach
    public void clean() {
        dataService.clean();
    }

    @Test
    void should_get_equipment_with_event_metrics() {
        // when
        var result = metricsService.getEquipmentsWithEventMetrics();

        //then
        assertThat(result).extracting("nbEquipWithEvent_A").isEqualTo(0L);
        assertThat(result).extracting("totalEquipWithEvent_A").isEqualTo(1L);
        assertThat(result).extracting("nbServiceTodoWithEquip_A").isEqualTo(0L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_A").isEqualTo(0L);

        assertThat(result).extracting("nbEquipWithEvent_FL").isEqualTo(0L);
        assertThat(result).extracting("totalEquipWithEvent_FL").isEqualTo(1L);
        assertThat(result).extracting("nbServiceTodoWithEquip_FL").isEqualTo(0L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_FL").isEqualTo(0L);

        assertThat(result).extracting("nbEquipWithEvent_unmanaged").isEqualTo(3L);
        assertThat(result).extracting("totalEquipWithEvent_unmanaged").isEqualTo(4L);
        assertThat(result).extracting("nbServiceTodoWithEquip_unmanaged").isEqualTo(0L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_unmanaged").isEqualTo(0L);
    }

    @Test
    void should_get_equipment_with_event_metrics_with_services() {
        // given
        var equipA = equipmentService.getEquipmentByName("A-230");

        dataService.persistService(new Service(UUID.randomUUID(), Instant.now(), Status.NEW, "new service 1", equipA));
        dataService.persistService(new Service(UUID.randomUUID(), Instant.now(), Status.IN_PROGRESS, "new service 2", equipA));

        var equipUnmanaged = equipmentService.getEquipmentByName("P-1000");

        dataService.persistService(new Service(UUID.randomUUID(), Instant.now(), Status.NEW, "new service 3", equipUnmanaged));
        dataService.persistService(
                new Service(UUID.randomUUID(), Instant.now(), Status.IN_PROGRESS, "new service 4", equipUnmanaged));

        // when
        var result = metricsService.getEquipmentsWithEventMetrics();

        // then
        assertThat(result).extracting("nbEquipWithEvent_A").isEqualTo(0L);
        assertThat(result).extracting("totalEquipWithEvent_A").isEqualTo(1L);
        assertThat(result).extracting("nbServiceTodoWithEquip_A").isEqualTo(1L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_A").isEqualTo(1L);

        assertThat(result).extracting("nbEquipWithEvent_FL").isEqualTo(0L);
        assertThat(result).extracting("totalEquipWithEvent_FL").isEqualTo(1L);
        assertThat(result).extracting("nbServiceTodoWithEquip_FL").isEqualTo(0L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_FL").isEqualTo(0L);

        assertThat(result).extracting("nbEquipWithEvent_unmanaged").isEqualTo(3L);
        assertThat(result).extracting("totalEquipWithEvent_unmanaged").isEqualTo(4L);
        assertThat(result).extracting("nbServiceTodoWithEquip_unmanaged").isEqualTo(1L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_unmanaged").isEqualTo(1L);
    }

    @Test
    void should_get_0_in_metrics_when_0_equipments() {
        // given
        dataService.clean();
        // when
        var result = metricsService.getEquipmentsWithEventMetrics();

        //then
        assertThat(result).extracting("nbEquipWithEvent_A").isEqualTo(0L);
        assertThat(result).extracting("totalEquipWithEvent_A").isEqualTo(0L);
        assertThat(result).extracting("nbServiceTodoWithEquip_A").isEqualTo(0L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_A").isEqualTo(0L);

        assertThat(result).extracting("nbEquipWithEvent_FL").isEqualTo(0L);
        assertThat(result).extracting("totalEquipWithEvent_FL").isEqualTo(0L);
        assertThat(result).extracting("nbServiceTodoWithEquip_FL").isEqualTo(0L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_FL").isEqualTo(0L);

        assertThat(result).extracting("nbEquipWithEvent_unmanaged").isEqualTo(0L);
        assertThat(result).extracting("totalEquipWithEvent_unmanaged").isEqualTo(0L);
        assertThat(result).extracting("nbServiceTodoWithEquip_unmanaged").isEqualTo(0L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_unmanaged").isEqualTo(0L);
    }

}
