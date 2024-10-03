package com.provoly.event;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;

import com.provoly.TestDataService;
import com.provoly.equipment.EquipmentService;
import com.provoly.event.dto.EventWriteDto;

import io.quarkus.test.junit.QuarkusTest;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class EventServiceTest {

    @Inject
    EventService eventService;

    @Inject
    TestDataService dataService;

    @Inject
    EquipmentService equipmentService;

    @BeforeEach
    public void init() {
        dataService.init();
    }

    @AfterEach
    public void clean() {
        dataService.clean();
    }

    @Test
    void should_throw_exception_create_operator_event_with_missing_dates() {
        // given
        var event = dataService.buildEvent("tutu", "MANIFESTATION", Criticality.HIGH, false);

        // then
        assertThatThrownBy(() -> eventService.saveEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Properties 'startDate' and 'endDate' are required for 'MANIFESTATION' category");
    }

    @Test
    void should_throw_exception_create_operator_event_with_invalid_dates() {
        // given
        var event = dataService.buildEvent("tutu", "MANIFESTATION", Criticality.HIGH, true);

        // then
        assertThatThrownBy(() -> eventService.saveEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End date is invalid: it must be after start date");
    }

    @Test
    @Transactional
    void should_throw_exception_update_external_event() {
        // given
        var eventAlert = eventService
                .getEvents(1, 1, null, null, null, List.of(), List.of(), List.of("LIMIT"), List.of(),
                        List.of(), null, null, null)
                .toList()
                .getFirst();

        var event = dataService.buildExternalEvent("tutu", "OUTOFORDER", Criticality.HIGH, false,
                eventAlert.getEquipment().getId());

        // then
        assertThatThrownBy(() -> eventService.updateEvent(eventAlert.getId(), event))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void should_throw_exception_create_event_with_invalid_domain() {
        // given
        var event = new EventWriteDto(null,
                "new event",
                "desc",
                Criticality.HIGH,
                "LIMIT",
                null,
                null,
                null,
                "invalid_domain",
                null,
                null,
                null);

        // then
        assertThatThrownBy(() -> eventService.saveEvent(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid");
    }

    @Test
    void should_throw_exception_create_event_with_invalid_sub_category() {
        // given
        var event = new EventWriteDto(null,
                "new event",
                "desc",
                Criticality.HIGH,
                "ANOMALY",
                "TOTO",
                null,
                null,
                null,
                null,
                null,
                null);

        // then
        assertThatThrownBy(() -> eventService.saveEvent(event))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Subcategory is required");
    }

    @Test
    void should_throw_exception_create_event_category_without_kwnown_sub_category() {
        // given
        var event = new EventWriteDto(null,
                "new event",
                "desc",
                Criticality.HIGH,
                "MANIFESTATION",
                "TOTO",
                null,
                null,
                null,
                Instant.now(),
                Instant.now(),
                null);

        // then
        assertThatThrownBy(() -> eventService.saveEvent(event))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("No subcategories are avalaible for");
    }

    @Test
    @Transactional
    void should_close_event() {
        // given
        var eventIdInProgress = eventService
                .getEvents(1, 1, null, null, null, List.of(), List.of(Status.IN_PROGRESS.name()), List.of(), List.of(),
                        List.of(), null, null, null)
                .toList()
                .getFirst()
                .getId();

        // when
        eventService.closeEventById(eventIdInProgress);

        // then
        var event = eventService.getEventDetails(eventIdInProgress);
        assertThat(event.getCloseDate()).isNotNull();
        assertThat(event.getStatus()).isEqualTo(Status.DONE);

    }

    @Test
    @Transactional
    void should_not_close_already_closed_event() {
        // given
        var eventIdDone = eventService
                .getEvents(1, 1, null, null, null, List.of(), List.of(Status.DONE.name()), List.of(), List.of(), List.of(),
                        null, null, null)
                .toList()
                .getFirst()
                .getId();
        var oldEvent = eventService.getEventDetails(eventIdDone);

        // when
        eventService.closeEventById(eventIdDone);

        // then
        var event = eventService.getEventDetails(eventIdDone);
        assertThat(event.getCloseDate()).isEqualTo(oldEvent.getCloseDate());
    }

    @Test
    void should_return_empty_summary_event_and_count_0_when_no_event() {
        // given
        clean();

        // when
        var result = eventService.getEventSummariesGroupByStatus(10, null);

        // then
        assertThat(result.get(Status.NEW)).extracting("count").isEqualTo(0L);
        assertThat(result.get(Status.IN_PROGRESS)).extracting("count").isEqualTo(0L);
        assertThat(result.get(Status.DONE)).extracting("count").isEqualTo(0L);

        init();
    }

    @Test
    @Transactional
    void should_export_events() throws IOException {
        // given
        var events = eventService.getEvents(1, 100, null, null, null, List.of(), List.of(), List.of(), List.of(), List.of(),
                null, null, null).toList();

        // when
        var result = eventService.exportEvents();

        File tempFile = File.createTempFile("events", ".xslx", null);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(result.readAllBytes());
        }

        FileInputStream file = new FileInputStream(tempFile);
        Workbook book = new XSSFWorkbook(file);

        // then
        assertThat(book.getSheetAt(0).getLastRowNum()).isEqualTo(events.size());
        tempFile.deleteOnExit();
    }

    @Test
    @Transactional
    void should_throw_exception_update_external_source_event() {
        // given
        var event = dataService.getEvent1();
        var eventUpdate = new EventWriteDto(event.getId(), event.getName(), event.getDescription(), event.getCriticality(),
                "MANIFESTATION", null, event.getAddress(), null, null, Instant.now(), Instant.now(), null, "source", null,
                null);

        // then
        assertThatThrownBy(() -> eventService.updateEvent(event.getId(), eventUpdate))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining(
                        "External source reference can't be updated");
    }

    @Test
    @Transactional
    void should_throw_exception_update_external_source_event_2() {
        // given
        var event = dataService.getExternalEvent();
        var eventUpdate = new EventWriteDto(event.getId(), event.getName(), event.getDescription(), event.getCriticality(),
                "OUTOFORDER", null, event.getAddress(), null, null, null, null, null, null, null, null);

        // then
        assertThatThrownBy(() -> eventService.updateEvent(event.getId(), eventUpdate))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining(
                        "External source reference can't be updated");
    }

    @Test
    @Transactional
    void should_update_event_with_same_external_id_and_source_when_save_event() {
        // given
        var equipment = equipmentService.getEquipmentByName("A-230").getId();
        var event = new EventWriteDto(null,
                "new event",
                "desc",
                Criticality.HIGH,
                "MANIFESTATION",
                null,
                null,
                equipment,
                null,
                Instant.now(),
                Instant.now(),
                Instant.now(),
                "external_source",
                "external_id",
                null);
        eventService.saveEvent(event);

        // when
        eventService.saveEvent(event);

        //then
        var events = eventService.getEvents(1, 10, null, null, null, List.of(), List.of(), List.of(), List.of(), List.of(),
                "new event", null, null).toList();
        assertThat(events.size()).isEqualTo(1);
    }

    @Test
    @Transactional
    void should_save_event_with_same_external_id_but_different_source_when_save_event() {
        // given
        var equipment = equipmentService.getEquipmentByName("A-230").getId();
        var event = new EventWriteDto(null,
                "new event",
                "desc",
                Criticality.HIGH,
                "MANIFESTATION",
                null,
                null,
                equipment,
                null,
                Instant.now(),
                Instant.now(),
                Instant.now(),
                "external_source",
                "external_id",
                null);
        eventService.saveEvent(event);

        // when
        var eventSameExternalId = new EventWriteDto(null,
                "new event",
                "desc",
                Criticality.HIGH,
                "MANIFESTATION",
                null,
                null,
                equipment,
                null,
                Instant.now(),
                Instant.now(),
                Instant.now(),
                "external_source_2",
                "external_id",
                null);
        eventService.saveEvent(eventSameExternalId);

        //then
        var events = eventService.getEvents(1, 10, null, null, null, List.of(), List.of(), List.of(), List.of(), List.of(),
                "new event", null, null).toList();
        assertThat(events.size()).isEqualTo(2);
    }
}
