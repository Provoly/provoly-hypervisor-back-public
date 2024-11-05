package com.provoly.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import com.provoly.TestDataService;
import com.provoly.equipment.EquipmentService;
import com.provoly.equipment.EquipmentWriteDto;
import com.provoly.event.Criticality;
import com.provoly.event.EventService;
import com.provoly.event.dto.EventWriteDto;
import com.provoly.user.UserService;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @InjectMock
    UserService mock;

    @BeforeEach
    public void init() {
        dataService.init();
        given(mock.getCurrentUserName()).willReturn("reader");
        given(mock.getCurrentUserFullName()).willReturn("name");
        given(mock.getCurrentUserSubject()).willReturn(dataService.getUser().getSubject());
        given(mock.getCurrentUser()).willReturn(dataService.getUser());
    }

    @AfterEach
    public void clean() {
        dataService.clean();
    }

    private void saveDeletedEPEquipment() {
        var equipment = new EquipmentWriteDto(Map.of("id", "id"), 0, "deleted", "deleted", "EP", "Armoire", "FAGNIERES-COMMUN",
                "CHALONS", "adr", "CENTRE", null, true, Map.of("managed", 1));
        equipmentService.saveOrUpdateEquipments(List.of(equipment));
    }

    private void saveDeletedVPEquipment() {
        var equipment = new EquipmentWriteDto(Map.of("id", "id"), 0, "deleted_VP", "deleted_VP", "VP", "Camera", "AGGLO-COMMUN",
                "CHALONS", "adr", "CENTRE", null, true, Map.of("managed", 1));
        equipmentService.saveOrUpdateEquipments(List.of(equipment));
    }

    @Test
    void should_get_equipment_with_event_metrics() {
        // when
        var result = metricsService.getEpEquipmentsWithEvent(List.of(), List.of(), List.of(), List.of());

        //then
        assertThat(result).extracting("nbEquipWithEvent_A").isEqualTo(0L);
        assertThat(result).extracting("totalEquipWithEvent_A").isEqualTo(1L);
        assertThat(result).extracting("nbServiceTodoWithEquip_A").isEqualTo(0L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_A").isEqualTo(0L);

        assertThat(result).extracting("nbEquipWithEvent_FL").isEqualTo(1L);
        assertThat(result).extracting("totalEquipWithEvent_FL").isEqualTo(1L);
        assertThat(result).extracting("nbServiceTodoWithEquip_FL").isEqualTo(0L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_FL").isEqualTo(0L);

        assertThat(result).extracting("nbEquipWithEvent_unmanaged").isEqualTo(2L);
        assertThat(result).extracting("totalEquipWithEvent_unmanaged").isEqualTo(4L);
        assertThat(result).extracting("nbServiceTodoWithEquip_unmanaged").isEqualTo(1L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_unmanaged").isEqualTo(0L);
    }

    @Test
    void should_get_equipment_with_event_metrics_ignore_deleted_equipment() {
        // given
        saveDeletedEPEquipment();
        var equipmentId = equipmentService.getEquipmentByName("deleted").getId();
        var event = new EventWriteDto(null, "deleted", "desc", Criticality.LOW, "LIMIT", null, "address", equipmentId, "EP",
                null, null, null, null, null, null);
        eventService.saveEvent(event);
        // when
        var result = metricsService.getEpEquipmentsWithEvent(List.of(), List.of(), List.of(), List.of());

        //then
        assertThat(result).extracting("nbEquipWithEvent_A").isEqualTo(0L);
        assertThat(result).extracting("totalEquipWithEvent_A").isEqualTo(1L);
    }

    @Test
    void should_get_ep_equipment_with_event_detailed_metrics() {
        // when
        var result = metricsService.getEpEquipmentWithEventDetailed();

        //then
        assertThat(result.getManifestation()).extracting("nbEquipWithEvent_A").isEqualTo(1L);

        assertThat(result.getAnomaly())
                .extracting("nbEquipWithEvent_FL").isEqualTo(1L);

        assertThat(result).extracting("totalEquipWithEvent_A").isEqualTo(1L);
        assertThat(result).extracting("totalEquipWithEvent_FL").isEqualTo(1L);
        assertThat(result).extracting("totalEquipWithEvent_unmanaged").isEqualTo(4L);

        assertThat(result).extracting("nbServiceTodoWithEquip_A").isEqualTo(0L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_A").isEqualTo(0L);

        assertThat(result).extracting("nbServiceTodoWithEquip_FL").isEqualTo(0L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_FL").isEqualTo(0L);

        assertThat(result).extracting("nbServiceTodoWithEquip_unmanaged").isEqualTo(1L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_unmanaged").isEqualTo(0L);
    }

    @Test
    void should_get_equipment_with_event_metrics_filter_on_place() {
        // when
        var result = metricsService.getEpEquipmentsWithEvent(List.of(), List.of(), List.of(), List.of("CENTRE"));

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
        assertThat(result).extracting("totalEquipWithEvent_unmanaged").isEqualTo(2L);
        assertThat(result).extracting("nbServiceTodoWithEquip_unmanaged").isEqualTo(1L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_unmanaged").isEqualTo(0L);
    }

    @Test
    void should_get_equipment_from_agglo_with_event_metrics_with_criticality_low_medium_and_category_alert() {
        // when
        var result = metricsService.getEpEquipmentsWithEvent(List.of(Criticality.LOW.name(), Criticality.MEDIUM.name()),
                List.of("LIMIT", "OUTOFORDER"), List.of("AGGLO-COMMUN"), List.of());

        //then
        assertThat(result).extracting("nbEquipWithEvent_A").isEqualTo(0L);
        assertThat(result).extracting("totalEquipWithEvent_A").isEqualTo(0L);
        assertThat(result).extracting("nbServiceTodoWithEquip_A").isEqualTo(0L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_A").isEqualTo(0L);

        assertThat(result).extracting("nbEquipWithEvent_FL").isEqualTo(1L);
        assertThat(result).extracting("totalEquipWithEvent_FL").isEqualTo(1L);
        assertThat(result).extracting("nbServiceTodoWithEquip_FL").isEqualTo(0L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_FL").isEqualTo(0L);

        assertThat(result).extracting("nbEquipWithEvent_unmanaged").isEqualTo(1L);
        assertThat(result).extracting("totalEquipWithEvent_unmanaged").isEqualTo(2L);
        assertThat(result).extracting("nbServiceTodoWithEquip_unmanaged").isEqualTo(0L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_unmanaged").isEqualTo(0L);
    }

    @Test
    void should_get_distinct_equipment_with_event_metrics_when_linked_to_many_events() {
        //given
        var result = metricsService.getEpEquipmentsWithEvent(List.of(), List.of(), List.of(), List.of());
        assertThat(result).extracting("nbEquipWithEvent_unmanaged").isEqualTo(2L);

        // when adding a new event for an unmanaged equipment
        var equipUnmanaged = equipmentService.getEquipmentByName("C-762");

        eventService.saveEvent(new EventWriteDto(null,
                "new report event",
                "description",
                Criticality.LOW,
                "LIMIT",
                null,
                "address",
                equipUnmanaged.getId(),
                "EP",
                null,
                null,
                null));

        var resultUpdated = metricsService.getEpEquipmentsWithEvent(List.of(), List.of(), List.of(), List.of());

        //then
        assertThat(resultUpdated).extracting("nbEquipWithEvent_unmanaged")
                .isEqualTo(result.getNbEquipWithEvent_unmanaged());

    }

    @Test
    void should_get_0_in_metrics_when_0_equipments() {
        // given
        dataService.clean();

        // when
        var result = metricsService.getEpEquipmentsWithEvent(List.of(), List.of(), List.of(), List.of());

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
        var result = metricsService.getTotalEpEquipmentsByEntity("EP_FOYER_LUMINEUX");

        //then
        assertThat(result).extracting("aggloManaged").isEqualTo(1L);
        assertThat(result).extracting("aggloUnmanaged").isEqualTo(0L);
        assertThat(result).extracting("chManaged").isEqualTo(0L);
        assertThat(result).extracting("chUnmanaged").isEqualTo(0L);

        assertThat(result).extracting("fagnManaged").isEqualTo(0L);
        assertThat(result).extracting("fagnUnmanaged").isEqualTo(1L);
        assertThat(result).extracting("smpManaged").isEqualTo(0L);
        assertThat(result).extracting("smpUnmanaged").isEqualTo(0L);
    }

    @Test
    void should_get_equipment_A_by_entities() {
        // when
        var result = metricsService.getTotalEpEquipmentsByEntity("EP_ARMOIRE");

        //then
        assertThat(result).extracting("aggloManaged").isEqualTo(0L);
        assertThat(result).extracting("aggloUnmanaged").isEqualTo(1L);
        assertThat(result).extracting("chManaged").isEqualTo(0L);
        assertThat(result).extracting("chUnmanaged").isEqualTo(0L);

        assertThat(result).extracting("fagnManaged").isEqualTo(0L);
        assertThat(result).extracting("fagnUnmanaged").isEqualTo(0L);
        assertThat(result).extracting("smpManaged").isEqualTo(1L);
        assertThat(result).extracting("smpUnmanaged").isEqualTo(0L);
    }

    @Test
    void should_get_equipment_A_by_entities_ignore_deleted_equipment() {
        //given
        saveDeletedEPEquipment();
        // when
        var result = metricsService.getTotalEpEquipmentsByEntity("EP_ARMOIRE");

        //then
        assertThat(result).extracting("aggloManaged").isEqualTo(0L);
        assertThat(result).extracting("aggloUnmanaged").isEqualTo(1L);
        assertThat(result).extracting("chManaged").isEqualTo(0L);
        assertThat(result).extracting("chUnmanaged").isEqualTo(0L);

        assertThat(result).extracting("fagnManaged").isEqualTo(0L);
        assertThat(result).extracting("fagnUnmanaged").isEqualTo(0L);
        assertThat(result).extracting("smpManaged").isEqualTo(1L);
        assertThat(result).extracting("smpUnmanaged").isEqualTo(0L);
    }

    @Test
    void should_get_equipment_A_by_entities_no_equipment() {
        // given
        dataService.clean();

        // when
        var result = metricsService.getTotalEpEquipmentsByEntity("EP_ARMOIRE");

        //then
        assertThat(result).extracting("aggloManaged").isEqualTo(0L);
        assertThat(result).extracting("aggloUnmanaged").isEqualTo(0L);
        assertThat(result).extracting("chManaged").isEqualTo(0L);
        assertThat(result).extracting("chUnmanaged").isEqualTo(0L);

        assertThat(result).extracting("fagnManaged").isEqualTo(0L);
        assertThat(result).extracting("fagnUnmanaged").isEqualTo(0L);
        assertThat(result).extracting("smpManaged").isEqualTo(0L);
        assertThat(result).extracting("smpUnmanaged").isEqualTo(0L);
    }

    @Test
    void should_get_equipment_null_family_returns_empty_result() {
        // when
        var result = metricsService.getTotalEpEquipmentsByEntity(null);

        // then
        assertThat(result).extracting("aggloManaged").isEqualTo(0L);
        assertThat(result).extracting("aggloUnmanaged").isEqualTo(0L);
        assertThat(result).extracting("chManaged").isEqualTo(0L);
        assertThat(result).extracting("chUnmanaged").isEqualTo(0L);

        assertThat(result).extracting("fagnManaged").isEqualTo(0L);
        assertThat(result).extracting("fagnUnmanaged").isEqualTo(0L);
        assertThat(result).extracting("smpManaged").isEqualTo(0L);
        assertThat(result).extracting("smpUnmanaged").isEqualTo(0L);

    }

    @Test
    void should_get_done_services_for_last_2_months() {
        // given
        var equip = equipmentService.getEquipmentByName("P-1000");

        dataService.persistDoneService("DI5678", Instant.parse("2024-01-15T00:00:00.00Z"), equip, true);
        dataService.persistDoneService("DI5778", Instant.parse("2024-02-15T00:00:00.00Z"), equip, true);
        dataService.persistDoneService("DI5779", Instant.parse("2024-02-15T00:00:00.00Z"), equip, true);
        dataService.persistDoneService("DI5870", Instant.parse("2024-03-15T00:00:00.00Z"), equip, true);
        dataService.persistDoneService("DI5871", Instant.parse("2024-04-15T00:00:00.00Z"), equip, true);
        dataService.persistDoneService("DI5872", Instant.parse("2024-04-30T00:00:00.00Z"), equip, true);

        // when
        var result = metricsService.aggregateDoneServices(DateInterval.month, Instant.parse("2024-04-20T00:00:00.00Z"), null, 2,
                List.of(), List.of(), List.of());

        //then
        assertThat(result).hasSize(3);
        assertThat(result).extracting("start").containsExactly(
                Instant.parse("2024-02-01T00:00:00Z"),
                Instant.parse("2024-03-01T00:00:00Z"),
                Instant.parse("2024-04-01T00:00:00Z"));
        assertThat(result).extracting("count").containsExactly(2L, 1L, 1L);
    }

    @Test
    void should_not_get_done_services_family_is_null() {
        // given
        var equip = equipmentService.getEquipmentByName("P-1000");

        dataService.persistDoneService("DI5871", Instant.parse("2024-04-15T00:00:00.00Z"), equip, true);
        dataService.persistDoneService("DI5872", Instant.parse("2024-04-30T00:00:00.00Z"), equip, true);

        // when
        List<String> family = new ArrayList<>();
        family.add(null);
        var result = metricsService.aggregateDoneServices(DateInterval.month, Instant.parse("2024-04-20T00:00:00.00Z"), null, 2,
                family, List.of(), List.of());

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void should_not_get_done_services_place_is_null() {
        // given
        var equip = equipmentService.getEquipmentByName("P-1000");

        dataService.persistDoneService("DI5871", Instant.parse("2024-04-15T00:00:00.00Z"), equip, true);
        dataService.persistDoneService("DI5872", Instant.parse("2024-04-30T00:00:00.00Z"), equip, true);

        // when
        List<String> place = new ArrayList<>();
        place.add(null);
        var result = metricsService.aggregateDoneServices(DateInterval.month, Instant.parse("2024-04-20T00:00:00.00Z"), null, 2,
                List.of(), List.of(), place);

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void should_not_get_done_services_entity_is_null() {
        // given
        var equip = equipmentService.getEquipmentByName("P-1000");

        dataService.persistDoneService("DI5871", Instant.parse("2024-04-15T00:00:00.00Z"), equip, true);
        dataService.persistDoneService("DI5872", Instant.parse("2024-04-30T00:00:00.00Z"), equip, true);

        // when
        List<String> entity = new ArrayList<>();
        entity.add(null);
        var result = metricsService.aggregateDoneServices(DateInterval.month, Instant.parse("2024-04-20T00:00:00.00Z"), null, 2,
                List.of(), entity, List.of());

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void should_get_done_services_for_last_2_months_filter_on_armoire() {
        // given
        var equip = equipmentService.getEquipmentByName("P-1000");
        var equipArmoire = equipmentService.getEquipmentByName("A-230");

        dataService.persistDoneService("DI5678", Instant.parse("2024-01-15T00:00:00.00Z"), equip, true);
        dataService.persistDoneService("DI5778", Instant.parse("2024-02-15T00:00:00.00Z"), equipArmoire, true);
        dataService.persistDoneService("DI5779", Instant.parse("2024-02-15T00:00:00.00Z"), equip, true);
        dataService.persistDoneService("DI5870", Instant.parse("2024-03-15T00:00:00.00Z"), equipArmoire, true);
        dataService.persistDoneService("DI5871", Instant.parse("2024-04-15T00:00:00.00Z"), equip, true);

        // when
        var result = metricsService.aggregateDoneServices(DateInterval.month, Instant.parse("2024-04-20T00:00:00.00Z"), null, 2,
                List.of("EP_ARMOIRE"), List.of(), List.of());
        //then

        assertThat(result).hasSize(2);
        assertThat(result).extracting("start").containsExactly(
                Instant.parse("2024-02-01T00:00:00Z"),
                Instant.parse("2024-03-01T00:00:00Z"));
        assertThat(result).extracting("count").containsExactly(1L, 1L);
    }

    @Test
    void should_get_only_curative_services_for_last_2_months() {
        // given
        var equip = equipmentService.getEquipmentByName("P-1000");

        dataService.persistDoneService("DI5678", Instant.parse("2024-01-15T00:00:00.00Z"), equip, true);
        dataService.persistDoneService("DI5778", Instant.parse("2024-02-15T00:00:00.00Z"), equip, false);
        dataService.persistDoneService("DI5779", Instant.parse("2024-02-15T00:00:00.00Z"), equip, true);
        dataService.persistDoneService("DI5870", Instant.parse("2024-03-15T00:00:00.00Z"), equip, false);
        dataService.persistDoneService("DI5871", Instant.parse("2024-04-15T00:00:00.00Z"), equip, true);

        // when
        var result = metricsService.aggregateDoneServices(DateInterval.month, Instant.parse("2024-04-20T00:00:00.00Z"), null, 2,
                List.of(), List.of(), List.of());
        //then

        assertThat(result).hasSize(2);
        assertThat(result).extracting("start").containsExactly(
                Instant.parse("2024-02-01T00:00:00Z"),
                Instant.parse("2024-04-01T00:00:00Z"));
        assertThat(result).extracting("count").containsExactly(1L, 1L);
    }

    @Test
    void should_get_done_services_for_last_2_months_filter_on_chalons_place() {
        // given
        var equipFagniere = equipmentService.getEquipmentByName("P-1000");
        var equipChalons = equipmentService.getEquipmentByName("A-230");

        dataService.persistDoneService("DI5678", Instant.parse("2024-01-15T00:00:00.00Z"), equipFagniere, true);
        dataService.persistDoneService("DI5778", Instant.parse("2024-02-15T00:00:00.00Z"), equipChalons, true);
        dataService.persistDoneService("DI5779", Instant.parse("2024-02-15T00:00:00.00Z"), equipFagniere, true);
        dataService.persistDoneService("DI5870", Instant.parse("2024-03-15T00:00:00.00Z"), equipChalons, true);
        dataService.persistDoneService("DI5871", Instant.parse("2024-04-15T00:00:00.00Z"), equipChalons, true);
        dataService.persistDoneService("DI5872", Instant.parse("2024-04-30T00:00:00.00Z"), equipFagniere, true);

        // when
        var result = metricsService.aggregateDoneServices(DateInterval.month, Instant.parse("2024-05-20T00:00:00.00Z"), null, 3,
                List.of(), List.of(), List.of("CENTRE"));

        //then
        assertThat(result).hasSize(3);
        assertThat(result).extracting("start").containsExactly(
                Instant.parse("2024-02-01T00:00:00Z"),
                Instant.parse("2024-03-01T00:00:00Z"),
                Instant.parse("2024-04-01T00:00:00Z"));
        assertThat(result).extracting("count").containsExactly(1L, 1L, 1L);
    }

    @Test
    void should_get_vp_equipment_from_agglo_with_event_metrics() {
        // when
        var result = metricsService.getVpEquipmentsWithEvent(List.of(), List.of(), List.of(), List.of());

        //then
        assertThat(result).extracting("nbEquipWithEvent_C").isEqualTo(0L);
        assertThat(result).extracting("totalEquipWithEvent_C").isEqualTo(2L);
        assertThat(result).extracting("nbServiceTodoWithEquip_C").isEqualTo(0L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_C").isEqualTo(0L);
    }

    @Test
    void should_get_vp_equipment_from_agglo_with_event_metrics_ignore_deleted_equipments() {
        //given
        saveDeletedVPEquipment();
        var equipmentId = equipmentService.getEquipmentByName("deleted_VP").getId();
        var event = new EventWriteDto(null, "deleted", "desc", Criticality.LOW, "LIMIT", null, "address", equipmentId, "EP",
                null, null, null, null, null, null);
        eventService.saveEvent(event);

        // when
        var result = metricsService.getVpEquipmentsWithEvent(List.of(), List.of(), List.of(), List.of());

        //then
        assertThat(result).extracting("nbEquipWithEvent_C").isEqualTo(0L);
        assertThat(result).extracting("totalEquipWithEvent_C").isEqualTo(2L);
        assertThat(result).extracting("nbServiceTodoWithEquip_C").isEqualTo(0L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_C").isEqualTo(0L);
    }

    @Test
    void should_get_vp_equipment_with_event_detailed_metrics() {
        // when
        var result = metricsService.getVpEquipmentsWithEventDetailed();

        //then
        assertThat(result.getManifestation()).extracting("nbEquipWithEvent_C").isEqualTo(0L);

        assertThat(result.getAnomaly())
                .extracting("nbEquipWithEvent_C").isEqualTo(0L);

        assertThat(result).extracting("totalEquipWithEvent_C").isEqualTo(2L);
        assertThat(result).extracting("nbServiceTodoWithEquip_C").isEqualTo(0L);
        assertThat(result).extracting("nbServiceInProgressWithEquip_C").isEqualTo(0L);

    }

    @Test
    void should_get_done_services_for_last_2_months_filter_on_vp_domain() {
        // given
        var equipFagniere = equipmentService.getEquipmentByName("P-1000");
        var equipChalons = equipmentService.getEquipmentByName("A-230");

        dataService.persistDoneService("DI5678", Instant.parse("2024-01-15T00:00:00.00Z"), equipFagniere, true);
        dataService.persistDoneService("DI5778", Instant.parse("2024-02-15T00:00:00.00Z"), equipChalons, true, true);
        dataService.persistDoneService("DI5779", Instant.parse("2024-02-15T00:00:00.00Z"), equipFagniere, true);
        dataService.persistDoneService("DI5870", Instant.parse("2024-03-15T00:00:00.00Z"), equipChalons, true, true);
        dataService.persistDoneService("DI5871", Instant.parse("2024-04-15T00:00:00.00Z"), equipChalons, true, true);
        dataService.persistDoneService("DI5872", Instant.parse("2024-04-30T00:00:00.00Z"), equipFagniere, true);

        // when
        var result = metricsService.aggregateDoneServices(DateInterval.month, Instant.parse("2024-05-20T00:00:00.00Z"), "VP", 3,
                List.of(), List.of(), List.of());

        //then
        assertThat(result).hasSize(3);
        assertThat(result).extracting("start").containsExactly(
                Instant.parse("2024-02-01T00:00:00Z"),
                Instant.parse("2024-03-01T00:00:00Z"),
                Instant.parse("2024-04-01T00:00:00Z"));
        assertThat(result).extracting("count").containsExactly(1L, 1L, 1L);
    }

    @Test
    void should_get_equipment_vp_anomalies_number_grouped_by_subcategories() {
        // given
        var vpEquipment = equipmentService.getEquipmentByName("camera1");

        eventService.saveEvent(new EventWriteDto(null,
                "new anomaly event",
                "description",
                Criticality.LOW,
                "ANOMALY",
                "TRAFFIC_CONGESTION",
                "address",
                vpEquipment.getId(),
                "VP",
                null,
                null,
                null));

        var creationDate = Instant.parse(LocalDate.now().atStartOfDay() + ":00.000Z");

        // when
        var result = metricsService.getAnomalyEventsBySubCategories("VP", creationDate, null, List.of(), List.of(),
                List.of(), List.of(), null);

        //then
        assertThat(result).extracting("TRAFFIC_CONGESTION").isEqualTo(1L);
        assertThat(result).extracting("WILD_STORAGE").isEqualTo(0L);
        assertThat(result).extracting("UNUSUAL_FLOW").isEqualTo(0L);
    }

    @Test
    void should_get_equipment_vp_anomalies_number_grouped_by_subcategories_ignore_deleted_equipments() {
        // given
        saveDeletedVPEquipment();
        var vpEquipment = equipmentService.getEquipmentByName("deleted_VP");

        eventService.saveEvent(new EventWriteDto(null,
                "new anomaly event",
                "description",
                Criticality.LOW,
                "ANOMALY",
                "TRAFFIC_CONGESTION",
                "address",
                vpEquipment.getId(),
                "VP",
                null,
                null,
                null));

        var creationDate = Instant.parse(LocalDate.now().atStartOfDay() + ":00.000Z");

        // when
        var result = metricsService.getAnomalyEventsBySubCategories("VP", creationDate, null, List.of(), List.of(),
                List.of(), List.of(), null);

        //then
        assertThat(result).extracting("TRAFFIC_CONGESTION").isEqualTo(0L);
        assertThat(result).extracting("WILD_STORAGE").isEqualTo(0L);
        assertThat(result).extracting("UNUSUAL_FLOW").isEqualTo(0L);
    }

    @Test
    void should_get_equipment_vp_anomalies_number_grouped_by_subcategories_for_specific_equipment() {
        // given
        var vpEquipment = equipmentService.getEquipmentByName("camera1");

        eventService.saveEvent(new EventWriteDto(null,
                "new anomaly event",
                "description",
                Criticality.LOW,
                "ANOMALY",
                "TRAFFIC_CONGESTION",
                "address",
                vpEquipment.getId(),
                "VP",
                null,
                null,
                null));

        var creationDate = Instant.parse(LocalDate.now().atStartOfDay() + ":00.000Z");

        // when
        var result = metricsService.getAnomalyEventsBySubCategories("VP", creationDate, null, List.of(), List.of(),
                List.of(), List.of(), "camera1");

        //then
        assertThat(result).extracting("TRAFFIC_CONGESTION").isEqualTo(1L);
        assertThat(result).extracting("WILD_STORAGE").isEqualTo(0L);
        assertThat(result).extracting("UNUSUAL_FLOW").isEqualTo(0L);
    }

    @Test
    void should_get_0_equipment_vp_anomalies_number_grouped_by_subcategories_because_done() {
        // given
        var vpEquipment = equipmentService.getEquipmentByName("camera1");

        var savedEvent = eventService.saveEvent(new EventWriteDto(null,
                "new anomaly event",
                "description",
                Criticality.LOW,
                "ANOMALY",
                "TRAFFIC_CONGESTION",
                "address",
                vpEquipment.getId(),
                "VP",
                null,
                null,
                null));

        var creationDate = Instant.parse(LocalDate.now().atStartOfDay() + ":00.000Z");
        eventService.closeEventById(savedEvent.getId());

        // when
        var result = metricsService.getAnomalyEventsBySubCategories("VP", creationDate, null, List.of(), List.of(),
                List.of(), List.of(), null);

        //then
        assertThat(result).extracting("TRAFFIC_CONGESTION").isEqualTo(0L);
        assertThat(result).extracting("WILD_STORAGE").isEqualTo(0L);
        assertThat(result).extracting("UNUSUAL_FLOW").isEqualTo(0L);
    }

    @Test
    void should_get_all_anomalies_number_grouped_by_subcategories_and_equipment_entities() {
        // given
        var vpEquipment = equipmentService.getEquipmentByName("camera1");
        eventService.saveEvent(new EventWriteDto(null,
                "new anomaly event",
                "description",
                Criticality.LOW,
                "ANOMALY",
                "TRAFFIC_CONGESTION",
                "address",
                vpEquipment.getId(),
                "VP",
                null,
                null,
                null));

        // when
        var creationDate = Instant.parse(LocalDate.now().atStartOfDay().minusDays(3) + ":00.000Z");
        var result = metricsService.getAnomalyEventsGroupedBySubCategoriesAndEntities("VP", creationDate);

        //then
        assertThat(result)
                .filteredOn(r -> r.entity().equals("AGGLO-COMMUN") && r.subCategory().equals("TRAFFIC_CONGESTION"))
                .extracting("count")
                .first()
                .isEqualTo(1L);
    }

    @Test
    void should_get_all_anomalies_number_grouped_by_subcategories_and_equipment_entities_ignore_deleted_equipment() {
        // given
        saveDeletedVPEquipment();
        var vpEquipment = equipmentService.getEquipmentByName("deleted_VP");
        eventService.saveEvent(new EventWriteDto(null,
                "new anomaly event",
                "description",
                Criticality.LOW,
                "ANOMALY",
                "TRAFFIC_CONGESTION",
                "address",
                vpEquipment.getId(),
                "VP",
                null,
                null,
                null));

        // when
        var creationDate = Instant.parse(LocalDate.now().atStartOfDay().minusDays(3) + ":00.000Z");
        var result = metricsService.getAnomalyEventsGroupedBySubCategoriesAndEntities("VP", creationDate);

        //then
        assertThat(result)
                .filteredOn(r -> r.entity().equals("AGGLO-COMMUN") && r.subCategory().equals("TRAFFIC_CONGESTION"))
                .extracting("count")
                .first()
                .isEqualTo(0L);
    }

    @Test
    void should_get_anomaly_event_for_last_2_months() {
        // given
        var vpEquipment = equipmentService.getEquipmentByName("camera1");
        eventService.saveEvent(new EventWriteDto(null,
                "new anomaly event",
                "description",
                Criticality.LOW,
                "ANOMALY",
                "TRAFFIC_CONGESTION",
                "address",
                vpEquipment.getId(),
                "VP",
                null,
                null,
                null));

        var startDate = Instant.parse(LocalDate.now().atStartOfDay().plusDays(10) + ":00.000Z");
        // when
        var result = metricsService.aggregateAnomaliesEvents(DateInterval.month, 2, "VP", startDate, List.of(), List.of(),
                List.of(), List.of());

        //then
        assertThat(result).hasSize(1);
        assertThat(result).extracting("start").containsExactly(
                Instant.parse(LocalDate.now().withDayOfMonth(1) + "T00:00:00.000Z"));
        assertThat(result).extracting("count").containsExactly(1L);
    }

    @Test
    void should_get_anomaly_event_for_last_2_months_for_fagniere_place() {
        // given
        var vpEquipment = equipmentService.getEquipmentByName("camera1");
        eventService.saveEvent(new EventWriteDto(null,
                "new anomaly event",
                "description",
                Criticality.LOW,
                "ANOMALY",
                "TRAFFIC_CONGESTION",
                "address",
                vpEquipment.getId(),
                "VP",
                null,
                null,
                null));

        var startDate = Instant.parse(LocalDate.now().atStartOfDay().plusDays(10) + ":00.000Z");
        // when
        var result = metricsService.aggregateAnomaliesEvents(DateInterval.month, 2, "VP", startDate, List.of("FAGNIERES"),
                List.of(),
                List.of(), List.of());

        //then
        assertThat(result).hasSize(1);
        assertThat(result).extracting("start").containsExactly(
                Instant.parse(LocalDate.now().withDayOfMonth(1) + "T00:00:00.000Z"));
        assertThat(result).extracting("count").containsExactly(1L);
    }

    @Test
    void should_get_anomaly_event_for_last_2_months_for_entity_SMP() {
        // given
        var vpEquipment = equipmentService.getEquipmentByName("camera1");
        eventService.saveEvent(new EventWriteDto(null,
                "new anomaly event",
                "description",
                Criticality.LOW,
                "ANOMALY",
                "TRAFFIC_CONGESTION",
                "address",
                vpEquipment.getId(),
                "VP",
                null,
                null,
                null));

        var startDate = Instant.parse(LocalDate.now().atStartOfDay().plusDays(10) + ":00.000Z");
        // when
        var result = metricsService.aggregateAnomaliesEvents(DateInterval.month, 2, "VP", startDate, List.of(),
                List.of("SAINT-MARTIN-COMMUN"),
                List.of(), List.of());

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void should_get_anomaly_event_for_last_2_months_for_all_domains() {
        // given
        var vpEquipment = equipmentService.getEquipmentByName("camera1");
        eventService.saveEvent(new EventWriteDto(null,
                "new anomaly event",
                "description",
                Criticality.LOW,
                "ANOMALY",
                "TRAFFIC_CONGESTION",
                "address",
                vpEquipment.getId(),
                "VP",
                null,
                null,
                null));

        var startDate = Instant.parse(LocalDate.now().atStartOfDay().plusDays(10) + ":00.000Z");
        // when
        var result = metricsService.aggregateAnomaliesEvents(DateInterval.month, 2, null, startDate, List.of(), List.of(),
                List.of(), List.of());

        //then
        assertThat(result).hasSize(1);
        assertThat(result).extracting("start").containsExactly(
                Instant.parse(LocalDate.now().withDayOfMonth(1) + "T00:00:00.000Z"));
        assertThat(result).extracting("count").containsExactly(1L);
    }

    @Test
    void should_get_anomaly_event_for_last_2_months_with_null_criticality() {
        // given
        var vpEquipment = equipmentService.getEquipmentByName("camera1");
        eventService.saveEvent(new EventWriteDto(null,
                "new anomaly event",
                "description",
                Criticality.LOW,
                "ANOMALY",
                "TRAFFIC_CONGESTION",
                "address",
                vpEquipment.getId(),
                "VP",
                null,
                null,
                null));

        var startDate = Instant.parse(LocalDate.now().atStartOfDay().plusDays(10) + ":00.000Z");
        var criticalities = new ArrayList<String>();
        criticalities.add(null);
        // when
        var result = metricsService.aggregateAnomaliesEvents(DateInterval.month, 2, null, startDate, List.of(), List.of(),
                criticalities, List.of());

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void should_get_anomaly_events_count_for_vp_equipments() {
        // given
        var vpEquipment = equipmentService.getEquipmentByName("camera1");
        eventService.saveEvent(new EventWriteDto(null,
                "new anomaly event",
                "description",
                Criticality.LOW,
                "ANOMALY",
                "TRAFFIC_CONGESTION",
                "address",
                vpEquipment.getId(),
                "VP",
                null,
                null,
                null));

        var startDate = Instant.parse(LocalDate.now().atStartOfDay().minusDays(1) + ":00.000Z");
        // when
        var result = metricsService.getEventsByEquipments("VP", "ANOMALY", 10, startDate, List.of(), List.of(), List.of(),
                List.of());

        //then
        assertThat(result).hasSize(1);
        assertThat(result).extracting("count").containsExactly(1L);
    }

    @Test
    void should_not_get_anomaly_events_count_for_vp_equipments_date_is_too_late() {
        // given
        var vpEquipment = equipmentService.getEquipmentByName("camera1");
        eventService.saveEvent(new EventWriteDto(null,
                "new anomaly event",
                "description",
                Criticality.LOW,
                "ANOMALY",
                "TRAFFIC_CONGESTION",
                "address",
                vpEquipment.getId(),
                "VP",
                null,
                null,
                null));

        var startDate = Instant.parse(LocalDate.now().atStartOfDay().plusDays(1) + ":00.000Z");
        // when
        var result = metricsService.getEventsByEquipments("VP", "ANOMALY", 10, startDate, List.of(), List.of(), List.of(),
                List.of());

        //then
        assertThat(result).isEmpty();
    }

}
