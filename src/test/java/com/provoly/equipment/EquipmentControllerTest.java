package com.provoly.equipment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;

import com.provoly.TestDataService;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;

import org.junit.jupiter.api.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EquipmentControllerTest {
    @Inject
    EquipmentController equipmentController;

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
    @TestSecurity(user = "reader", roles = { "equipment_write" })
    void should_throw_forbidden_when_null_required_property() {
        // Given
        var equipment = new EquipmentWriteDto("id", 0, null, null, null, null, null, "CH", "address", null, null, false, null);

        // When
        assertThatThrownBy(() -> equipmentController.saveOrUpdateEquipments(List.of(equipment)))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("must not be null");
    }
}
