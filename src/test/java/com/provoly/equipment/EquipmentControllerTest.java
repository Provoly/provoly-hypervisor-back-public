package com.provoly.equipment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;

import org.junit.jupiter.api.Test;

@QuarkusTest
public class EquipmentControllerTest {
    @Inject
    EquipmentController equipmentController;

    @Test
    @TestSecurity(user = "reader")
    void should_throw_forbidden_when_null_required_property() {
        // Given
        var equipment = new EquipmentWriteDto(null, 0, null, null, null, null, null, null, null);

        // When
        assertThatThrownBy(() -> equipmentController.saveOrUpdateEquipments(List.of(equipment)))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("must not be null");
    }
}
