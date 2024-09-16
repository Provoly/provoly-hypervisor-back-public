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
import jakarta.ws.rs.ForbiddenException;

import com.provoly.TestDataService;
import com.provoly.event.dto.EventWriteDto;

import io.quarkus.test.junit.QuarkusTest;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventServiceTest {

    @Inject
    EventService eventService;

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
    void should_throw_exception_create_external_event_missing_equipment_id() {
        // given
        var event = dataService.buildExternalEvent("alert", "LIMIT", Criticality.HIGH, false, null);

        // then
        assertThatThrownBy(() -> eventService.saveEvent(event))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("must provide an equipment.");
    }

    @Test
    void should_throw_exception_update_external_event() {
        // given
        var eventAlert = eventService
                .getEvents(1, 1, null, null, null, List.of(), List.of(), List.of("LIMIT"), List.of(),
                        List.of(), null)
                .stream()
                .toList()
                .getFirst();

        var event = dataService.buildExternalEvent("tutu", "OUTOFORDER", Criticality.HIGH, false,
                eventAlert.getEquipment().getId());

        // then
        assertThatThrownBy(() -> eventService.updateEvent(eventAlert.getId(), event))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining(
                        "It's only possible to update description, address or criticality of events with external reference");
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
    void should_throw_exception_create_event_external_source_without_equipment() {
        // given
        var event = new EventWriteDto(null,
                "new event",
                "desc",
                Criticality.HIGH,
                "LIMIT",
                null,
                null,
                null,
                null,
                Instant.now(),
                Instant.now(),
                "source");

        // then
        assertThatThrownBy(() -> eventService.saveEvent(event))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Events with external source must provide an equipment");
    }

    @Test
    void should_close_event() {
        // given
        var eventIdInProgress = eventService
                .getEvents(1, 1, null, null, null, List.of(), List.of(Status.IN_PROGRESS.name()), List.of(), List.of(),
                        List.of(), null)
                .stream()
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
    void should_not_close_already_closed_event() {
        // given
        var eventIdDone = eventService
                .getEvents(1, 1, null, null, null, List.of(), List.of(Status.DONE.name()), List.of(), List.of(), List.of(),
                        null)
                .stream()
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
    void should_export_events() throws IOException {
        // given
        var events = eventService.getEvents(1, 100, null, null, null, List.of(), List.of(), List.of(), List.of(), List.of(),
                null);

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

}
