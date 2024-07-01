package com.provoly.equipment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.*;

import jakarta.inject.Inject;

import com.provoly.TestDataService;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EquipmentServiceTest {

    @Inject
    EquipmentService equipmentService;

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
    void should_get_equipment_by_id() {
        // given
        var equip = equipmentService.getEquipmentByName("A-230");
        // when
        var equipment = equipmentService.getEquipmentById(equip.getId());

        //then
        assertThat(equipment).isInstanceOf(Equipment.class);
    }

    @Test
    void should_get_equipment_by_name() {
        // when
        var equipment = equipmentService.getEquipmentByName("A-230");

        //then
        assertThat(equipment).isInstanceOf(Equipment.class);
    }

    @Test
    void should_throw_exception_when_get_equipment_id_not_exists() {
        // when
        assertThatThrownBy(() -> equipmentService.getEquipmentById(UUID.randomUUID()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void should_throw_exception_when_get_equipment_name_not_exists() {
        // when
        assertThatThrownBy(() -> equipmentService.getEquipmentByName("toto"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void should_get_equipments_with_good_entity() {
        // when
        var result = equipmentService.getEquipments(List.of("FAGNIERES_COMMUN"));

        // then
        assertThat(result).extracting("entity").extracting("name").containsExactly("FAGNIERES_COMMUN");
    }

    @Test
    void should_throw_exception_when_get_equipment_entity_not_exists() {
        // when
        assertThatThrownBy(() -> equipmentService.getEquipments(List.of("invalid_entity")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("entity invalid");
    }

    @Test
    void should_throw_exception_when_save_equipments_parent_not_exists() {
        // given
        var equipment = new EquipmentWriteDto("id", 0, "name", "code", "EP", "Armoire", "FAGNIERES_COMMUN", "CH", "address",
                "CH_C", "invalid_code", false, null);

        // when
        assertThatThrownBy(() -> equipmentService.saveOrUpdateEquipments(List.of(equipment)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("parent");
    }

    @Test
    void should_throw_exception_and_abort_save_equipments_when_at_least_one_error() {
        // given
        var equipment1 = new EquipmentWriteDto("new_technical_id", 0, "name", "code", "EP", "Armoire", "FAGNIERES_COMMUN", "CH",
                "address", "CH_C", null, false, null);
        var equipment2 = new EquipmentWriteDto("new_technical_id1", 0, "name1", "code1", "invalid domain", "Armoire",
                "FAGNIERES_COMMUN", "CH", "address", "CH_C", null, false, null);

        var actualEquipmentSize = equipmentService.getEquipments(List.of()).size();

        // when
        assertThatThrownBy(() -> equipmentService.saveOrUpdateEquipments(List.of(equipment1, equipment2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("domain");
        var sameEquipmentSize = equipmentService.getEquipments(List.of()).size();

        // then
        assertThat(actualEquipmentSize).isEqualTo(sameEquipmentSize);
    }

    @Test
    void should_update_equipment_by_adding_attributes() {
        // given
        var equipment1 = new EquipmentWriteDto("P-1000", 0, "P-1000", "P-1000", "EP", "Foyer Lumineux", "FAGNIERES_COMMUN",
                "CH", "address", "CH_C", null, false,
                Map.of("activeEnergy", 30));

        // when
        equipmentService.saveOrUpdateEquipments(List.of(equipment1));
        var updatedEquipment = equipmentService.getEquipmentByName("P-1000");

        // then
        assertThat(updatedEquipment.getAttributes()).containsEntry("activeEnergy", 30);
    }

    @Test
    void should_update_equipment_attributes_with_null_value() {
        // given
        var equipment = new EquipmentWriteDto("P-1000", 0, "P-1000", "P-1000", "EP", "Foyer Lumineux", "FAGNIERES_COMMUN", "CH",
                "address", "CH_C", null, false,
                new HashMap<>(Map.of("activeEnergy", 30)));
        equipmentService.saveOrUpdateEquipments(List.of(equipment));

        // when
        equipment.attributes().put("activeEnergy", null);
        equipmentService.saveOrUpdateEquipments(List.of(equipment));
        var updatedEquipment = equipmentService.getEquipmentByName("P-1000");

        // then
        assertThat(updatedEquipment.getAttributes()).containsEntry("activeEnergy", null);
    }

    @Test
    void should_update_equipment_only_filled_in_attributes() {
        // given
        var equipment = new EquipmentWriteDto("P-1000", 0, "P-1000", "P-1000", "EP", "Foyer Lumineux", "FAGNIERES_COMMUN", "CH",
                "address", "CH_C", null, false,
                new HashMap<>(Map.of("activeEnergy", 30)));
        equipmentService.saveOrUpdateEquipments(List.of(equipment));

        // when
        equipment.attributes().put("activePower", "20");
        equipmentService.saveOrUpdateEquipments(List.of(equipment));
        var updatedEquipment = equipmentService.getEquipmentByName("P-1000");

        // then
        assertThat(updatedEquipment.getAttributes()).containsEntry("activeEnergy", 30);
        assertThat(updatedEquipment.getAttributes()).containsEntry("activePower", "20");
    }

    @Test
    void should_get_equipment_with_sorted_events() {
        // when
        var equipment = equipmentService.getEquipmentByName("C-762");

        // then
        assertThat(equipment.getEvents()).extracting("name").containsExactly("report1", "report3");
    }

    @Test
    void should_not_retrieve_equipment_deleted() {
        // given
        var equipment = new EquipmentWriteDto("P-1001", 0, "P-1001", "P-1001", "EP", "Foyer Lumineux", "FAGNIERES_COMMUN",
                "CH", "address", "CH_C", null, true,
                Map.of("activeEnergy", 30));

        equipmentService.saveOrUpdateEquipments(List.of(equipment));

        // when
        var equipments = equipmentService.getEquipments(List.of("FAGNIERES_COMMUN"));

        // then
        assertThat(equipments).isNotEmpty();
        assertThat(equipments).extracting("deleted").doesNotContain(true);
    }

}
