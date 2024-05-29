package com.provoly.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;

import com.provoly.TestDataService;
import com.provoly.equipment.EquipmentService;
import com.provoly.event.Category;
import com.provoly.event.Criticality;
import com.provoly.event.EventService;
import com.provoly.event.dto.ReportEventWriteDto;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.*;

@QuarkusTest
public class MetricsServiceTest {

    @Inject
    MetricsService metricsService;

    @Inject
    EquipmentService equipmentService;

    @Inject
    EventService eventService;

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
        var result = metricsService.getEquipmentsWithEventMetrics(List.of(), List.of(), List.of());

        //then
        assertThat(result).extracting("nbEquipWithEvent_A").isEqualTo(0L);
        assertThat(result).extracting("totalEquipWithEvent_A").isEqualTo(1L);
        assertThat(result).extracting("nbServiceTodoWithEquip_A").isEqualTo(0L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_A").isEqualTo(0L);

        assertThat(result).extracting("nbEquipWithEvent_FL").isEqualTo(1L);
        assertThat(result).extracting("totalEquipWithEvent_FL").isEqualTo(1L);
        assertThat(result).extracting("nbServiceTodoWithEquip_FL").isEqualTo(0L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_FL").isEqualTo(0L);

        assertThat(result).extracting("nbEquipWithEvent_unmanaged").isEqualTo(3L);
        assertThat(result).extracting("totalEquipWithEvent_unmanaged").isEqualTo(4L);
        assertThat(result).extracting("nbServiceTodoWithEquip_unmanaged").isEqualTo(1L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_unmanaged").isEqualTo(0L);
    }

    @Test
    void should_get_equipment_from_agglo_with_event_metrics_with_criticality_low_medium_and_category_alert() {
        // when
        var result = metricsService.getEquipmentsWithEventMetrics(List.of("LOW", "MEDIUM"),
                List.of("ALERT_LIMIT", "ALERT_MALFUNCTION"), List.of("AGGLO_COMMUN"));

        //then
        assertThat(result).extracting("nbEquipWithEvent_A").isEqualTo(0L);
        assertThat(result).extracting("totalEquipWithEvent_A").isEqualTo(1L);
        assertThat(result).extracting("nbServiceTodoWithEquip_A").isEqualTo(0L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_A").isEqualTo(0L);

        assertThat(result).extracting("nbEquipWithEvent_FL").isEqualTo(1L);
        assertThat(result).extracting("totalEquipWithEvent_FL").isEqualTo(1L);
        assertThat(result).extracting("nbServiceTodoWithEquip_FL").isEqualTo(0L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_FL").isEqualTo(0L);

        assertThat(result).extracting("nbEquipWithEvent_unmanaged").isEqualTo(1L);
        assertThat(result).extracting("totalEquipWithEvent_unmanaged").isEqualTo(4L);
        assertThat(result).extracting("nbServiceTodoWithEquip_unmanaged").isEqualTo(0L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_unmanaged").isEqualTo(0L);
    }

    @Test
    void should_get_distinct_equipment_with_event_metrics_when_linked_to_many_events() {
        //given
        var result = metricsService.getEquipmentsWithEventMetrics(List.of(), List.of(), List.of());
        assertThat(result).extracting("nbEquipWithEvent_unmanaged").isEqualTo(3L);

        // when adding a new event for an unmanaged equipment
        var equipUnmanaged = equipmentService.getEquipmentByName("P-1000");
        eventService.saveOrUpdateEvent(new ReportEventWriteDto(UUID.randomUUID(),
                "new report event",
                "description",
                Criticality.LOW,
                "address",
                equipUnmanaged.getId(),
                Category.REPORT,
                "ref",
                "EP"));

        var resultUpdated = metricsService.getEquipmentsWithEventMetrics(List.of(), List.of(), List.of());

        //then
        assertThat(resultUpdated).extracting("nbEquipWithEvent_unmanaged")
                .isEqualTo(result.nbEquipWithEvent_unmanaged());

    }

    @Test
    void should_get_0_in_metrics_when_0_equipments() {
        // given
        dataService.clean();

        // when
        var result = metricsService.getEquipmentsWithEventMetrics(List.of(), List.of(), List.of());

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

    @Test
    void should_get_equipment_FL_by_entities() {
        // when
        var result = metricsService.getTotalEquipmentsByEntity("EP_FOYER_LUMINEUX");

        //then
        assertThat(result).extracting("CHA_managed").isEqualTo(1L);
        assertThat(result).extracting("CHA_unmanaged").isEqualTo(0L);
        assertThat(result).extracting("CH_managed").isEqualTo(0L);
        assertThat(result).extracting("CH_unmanaged").isEqualTo(0L);

        assertThat(result).extracting("FAGN_managed").isEqualTo(0L);
        assertThat(result).extracting("FAGN_unmanaged").isEqualTo(1L);
        assertThat(result).extracting("SMP_managed").isEqualTo(0L);
        assertThat(result).extracting("SMP_unmanaged").isEqualTo(0L);
    }

    @Test
    void should_get_equipment_A_by_entities() {
        // when
        var result = metricsService.getTotalEquipmentsByEntity("EP_ARMOIRE");

        //then
        assertThat(result).extracting("CHA_managed").isEqualTo(0L);
        assertThat(result).extracting("CHA_unmanaged").isEqualTo(1L);
        assertThat(result).extracting("CH_managed").isEqualTo(0L);
        assertThat(result).extracting("CH_unmanaged").isEqualTo(0L);

        assertThat(result).extracting("FAGN_managed").isEqualTo(0L);
        assertThat(result).extracting("FAGN_unmanaged").isEqualTo(0L);
        assertThat(result).extracting("SMP_managed").isEqualTo(1L);
        assertThat(result).extracting("SMP_unmanaged").isEqualTo(0L);
    }

    @Test
    void should_get_equipment_A_by_entities_no_equipment() {
        // given
        dataService.clean();

        // when
        var result = metricsService.getTotalEquipmentsByEntity("EP_ARMOIRE");

        //then
        assertThat(result).extracting("CHA_managed").isEqualTo(0L);
        assertThat(result).extracting("CHA_unmanaged").isEqualTo(0L);
        assertThat(result).extracting("CH_managed").isEqualTo(0L);
        assertThat(result).extracting("CH_unmanaged").isEqualTo(0L);

        assertThat(result).extracting("FAGN_managed").isEqualTo(0L);
        assertThat(result).extracting("FAGN_unmanaged").isEqualTo(0L);
        assertThat(result).extracting("SMP_managed").isEqualTo(0L);
        assertThat(result).extracting("SMP_unmanaged").isEqualTo(0L);
    }

    @Test
    void should_get_equipment_null_family_should_throw_exception() {
        //then
        assertThatThrownBy(() -> metricsService.getTotalEquipmentsByEntity(null))
                .hasMessageContaining("Code null invalid");

    }

}
