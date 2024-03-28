package com.provoly.equipment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;
import java.util.UUID;

import jakarta.inject.Inject;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Test;

@QuarkusTest
public class EquipmentServiceTest {

    @Inject
    EquipmentService equipmentService;

    @Test
    void should_get_equipment_by_id() {
        // when
        var equipment = equipmentService.getEquipmentById(UUID.fromString("0c728960-d5fd-49ea-8fe8-f72cb9cfcccd"));

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
    void should_throw_exception_when_equipment_id_not_exists() {
        // when
        assertThatThrownBy(() -> equipmentService.getEquipmentById(UUID.randomUUID()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void should_throw_exception_when_equipment_name_not_exists() {
        // when
        assertThatThrownBy(() -> equipmentService.getEquipmentByName("toto"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void should_return_equipments_with_good_entity() {
        // when
        var result = equipmentService.getEquipments("FAGNIERES_COMMUN");

        // then
        assertThat(result).extracting("entity").extracting("name").containsExactly("FAGNIERES_COMMUN");
    }

    @Test
    void should_throw_exception_when_equipment_entity_not_exists() {
        // when
        assertThatThrownBy(() -> equipmentService.getEquipments("invalid_entity"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("entity invalid");
    }
}
