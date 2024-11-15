package com.provoly.service;

import static com.provoly.service.ServiceStatus.ASKED;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

import jakarta.inject.Inject;

import com.provoly.TestDataService;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ServiceControllerTest {
    @Inject
    ServiceController serviceController;

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
    @TestSecurity(user = "reader", roles = { "service_write" })
    void should_throw_when_invalid_equipment() {
        // Given
        var service = new ServiceWriteDto("technical_id", null, "", "invalid", Instant.now(), Instant.now(), Instant.now(),
                Instant.now(), null, "EP", ASKED, "PREV");

        // When
        assertThatThrownBy(() -> serviceController.saveOrUpdateServices(List.of(service)))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @TestSecurity(user = "reader", roles = { "service_write" })
    void should_throw_when_invalid_category() {
        // Given
        var service = new ServiceWriteDto("technical_id", null, "", "A-230", Instant.now(), Instant.now(), Instant.now(),
                Instant.now(), null, "EP", ASKED, "invalid");

        // When
        assertThatThrownBy(() -> serviceController.saveOrUpdateServices(List.of(service)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

}
