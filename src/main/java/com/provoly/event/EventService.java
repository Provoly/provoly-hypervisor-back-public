package com.provoly.event;

import static com.provoly.service.ServiceStatus.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;

import com.provoly.EnumEntity;
import com.provoly.action.ActionType;
import com.provoly.action.AskedService;
import com.provoly.equipment.Equipment;
import com.provoly.equipment.EquipmentService;
import com.provoly.equipmentenriched.EquipmentEnrichedProducer;
import com.provoly.event.dto.EventSummaryDto;
import com.provoly.event.dto.EventWriteDto;
import com.provoly.event.dto.EventsSummariesByStatusDto;
import com.provoly.notification.NotificationProducer;
import com.provoly.service.Service;
import com.provoly.service.ServiceService;
import com.provoly.user.Role;
import com.provoly.user.UserService;

import org.jboss.logging.Logger;

@ApplicationScoped
public class EventService {

    private final EventDatabaseReader databaseReader;
    private final EventMapper eventMapper;
    private final EquipmentService equipmentService;
    private final ServiceService serviceService;
    private final EquipmentEnrichedProducer equipmentEnrichedProducer;
    private final NotificationProducer notificationProducer;
    private final Logger logger;
    private final UserService userService;
    private final XslxService xslxService;

    public EventService(EventDatabaseReader databaseReader, EventMapper eventMapper, EquipmentService equipmentService,
            ServiceService serviceService,
            EquipmentEnrichedProducer equipmentEnrichedProducer, NotificationProducer notificationProducer,
            Logger logger, UserService userService, XslxService xslxService) {
        this.databaseReader = databaseReader;
        this.eventMapper = eventMapper;
        this.equipmentService = equipmentService;
        this.serviceService = serviceService;
        this.equipmentEnrichedProducer = equipmentEnrichedProducer;
        this.notificationProducer = notificationProducer;
        this.logger = logger;
        this.userService = userService;
        this.xslxService = xslxService;
    }

    @Transactional
    public Map<Status, EventsSummariesByStatusDto> getEventSummariesGroupByStatus(int limit, String criticality) {
        logger.infof("Get events summary with max %s events by status and criticality %s", limit, criticality);

        logger.debugf("Get events summary by status");
        Map<Status, List<EventSummaryDto>> eventSummaries = getEventSummariesGroupByStatus(limit,
                Criticality.fromString(criticality))
                .collect(Collectors.groupingBy(EventSummaryDto::status));

        logger.debugf("Get events count by status");
        Map<Status, Long> counts = databaseReader.getCountEventsByStatus();

        return Map.of(
                Status.DONE,
                new EventsSummariesByStatusDto(counts.getOrDefault(Status.DONE, 0L),
                        eventSummaries.getOrDefault(Status.DONE, List.of())),
                Status.IN_PROGRESS,
                new EventsSummariesByStatusDto(counts.getOrDefault(Status.IN_PROGRESS, 0L),
                        eventSummaries.getOrDefault(Status.IN_PROGRESS, List.of())),
                Status.NEW,
                new EventsSummariesByStatusDto(counts.getOrDefault(Status.NEW, 0L),
                        eventSummaries.getOrDefault(Status.NEW, List.of())));
    }

    private Stream<EventSummaryDto> getEventSummariesGroupByStatus(int limit, Criticality criticality) {
        var events = new ArrayList<>(databaseReader.getEvents(Status.NEW, limit, criticality));
        events.addAll(databaseReader.getEvents(Status.IN_PROGRESS, limit, criticality));
        events.addAll(databaseReader.getEvents(Status.DONE, limit, criticality));

        return events.stream()
                .map(event -> {
                    var allServices = getLastUndoneServiceFromProcedureOrEquipment(event);
                    return eventMapper.mapToEventSummaryDto(event, allServices);
                });
    }

    @Transactional
    public Stream<Event> getEvents(int page,
            int pageSize,
            String sort,
            String order,
            Instant creationDate,
            List<String> criticality,
            List<String> status,
            List<String> category,
            List<String> source,
            List<String> entity,
            List<String> family,
            String name,
            String id,
            String equipment,
            Instant closeDate) {
        logger.infof("""
                Get events with from page %s and size %s with :
                creation date : %s
                criticality : %s,
                status : %s,
                category : %s,
                source: %s,
                equipment entity : %s,
                equipment family : %s,
                closeDate : %s
                with search: %s/%s/%s
                sort by %s
                """.formatted(page, pageSize, creationDate, criticality, status, category, source, entity, family, closeDate,
                name, id, equipment, sort));

        if (oneOfFilterIsEmpty(criticality, status, category, source, entity, family)) {
            return Stream.of();
        }

        var criticalities = criticality.stream()
                .map(Criticality::fromString)
                .toList();

        var statuses = status.stream()
                .map(Status::fromString)
                .toList();

        var entities = entity.stream()
                .map(equipmentService::getEquipmentEntityByCode)
                .toList();

        var families = family.stream()
                .map(equipmentService::getFamilyByCode).toList();

        var categories = category.stream()
                .map(this::getCategory).toList();

        Integer idEvent;
        try {
            idEvent = Integer.parseUnsignedInt(id);
        } catch (NumberFormatException e) {
            idEvent = null;
        }

        return databaseReader.getEvents(page,
                pageSize,
                EventSort.fromName(sort),
                SortOrder.fromString(order),
                creationDate,
                criticalities,
                statuses,
                categories,
                source,
                entities,
                families,
                name,
                idEvent,
                equipment,
                closeDate);
    }

    private boolean oneOfFilterIsEmpty(List<String> criticality, List<String> status, List<String> category,
            List<String> source, List<String> entity, List<String> type) {
        return criticality.stream().anyMatch(String::isEmpty) ||
                status.stream().anyMatch(String::isEmpty) ||
                category.stream().anyMatch(String::isEmpty) ||
                source.stream().anyMatch(String::isEmpty) ||
                entity.stream().anyMatch(String::isEmpty) ||
                type.stream().anyMatch(String::isEmpty);
    }

    @Transactional
    public Category getCategory(String category) {
        logger.debugf("Get category with code %s", category);
        return category != null ? databaseReader.getCategoryByCode(category) : null;
    }

    @Transactional
    public Category getCategoryOrNull(String category) {
        logger.debugf("Get category with code %s", category);
        return category == null || category.isEmpty()
                ? null
                : databaseReader.getCategoryByCode(category);
    }

    @Transactional
    public Event getEventDetails(Integer id) {
        logger.infof("Get event details with id  %s".formatted(id));
        return databaseReader.getEventById(id);
    }

    @Transactional
    public Event getEventDetailsBySourceAndExternalId(String source, String id) {
        logger.infof("Get event details with source %s and external id  %s".formatted(source, id));
        return databaseReader.getEventDetailsBySourceAndExternalId(source, id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Event with source %s and external id %s not found".formatted(source, id)));
    }

    @Transactional
    public Event saveEvent(EventWriteDto eventDto) {
        logger.infof("Create %s event with name %s".formatted(eventDto.getCategory(), eventDto.getName()));
        checkDatesCoherence(eventDto);
        checkSubCategoryCoherence(eventDto);
        Event event;

        if (eventDto.getCreator() != null) {
            logger.debugf("Save an internal event");
            event = new Event(eventDto.getCreator());
        } else {
            logger.debugf("Save an external event");
            event = new Event(eventDto.getExternalId(), eventDto.getExternalSourceRef());
        }

        eventMapper.updateEvent(eventDto, event);
        databaseReader.saveEvent(event);

        enrichEquipmentFromUpdatedEvent(event.getId(), eventDto.getEquipmentId(), null);
        notificationProducer.sendNotificationFor(event);

        logger.debugf("Event %s is created".formatted(event.getId()));
        return event;
    }

    @Transactional
    public void updateEvent(Integer id, EventWriteDto eventDto) {
        logger.infof("Update %s event with name %s".formatted(eventDto.getCategory(), eventDto.getName()));
        checkDatesCoherence(eventDto);
        checkSubCategoryCoherence(eventDto);

        Event eventToUpdate = databaseReader.getEventById(id);
        var previousEquipmentId = eventToUpdate.getEquipment() != null ? eventToUpdate.getEquipment().getId() : null;

        if (!eventToUpdate.isExternal() && !Objects.equals(eventDto.getCreator(), eventToUpdate.getCreator())) {
            throw new ForbiddenException("Creator can't be updated");
        }

        if (eventToUpdate.isExternal() && externalPropertiesAreUpdated(eventDto, eventToUpdate)) {
            throw new ForbiddenException("External id and source reference can't be updated");
        }

        if (!userService.hasRole(Role.STR_EVENT_WRITE)
                && propertiesAreUpdated(eventDto, eventToUpdate)) {
            throw new io.quarkus.security.ForbiddenException("Missing permission to update event.");
        }

        eventMapper.updateEvent(eventDto, eventToUpdate);
        logger.debugf("Event %s is updated".formatted(id));
        enrichEquipmentFromUpdatedEvent(id, eventDto.getEquipmentId(), previousEquipmentId);
    }

    @Transactional
    public void closeEvent(Event event) {
        logger.infof("Close event %s", event.getId());
        if (event.getStatus() != Status.DONE) {
            event.setStatus(Status.DONE);
            event.setCloseDate(Instant.now());
        }
        equipmentEnrichedProducer.updateFor(event);
    }

    @Transactional
    public ByteArrayInputStream exportEvents() throws IOException {
        var events = databaseReader.getAllEvents();
        logger.infof("Export %s events", events.size());
        return xslxService.generateExcelWithEvents(eventMapper.mapToExportEventDto(events));
    }

    private boolean externalPropertiesAreUpdated(EventWriteDto eventDto, Event eventToUpdate) {
        return !Objects.equals(eventToUpdate.getExternalSourceRef(), eventDto.getExternalSourceRef())
                || (eventDto.getExternalId() != null && !eventDto.getExternalId().equals(eventToUpdate.getExternalId()));
    }

    private boolean propertiesAreUpdated(EventWriteDto eventDto, Event eventToUpdate) {
        var subCode = eventToUpdate.getSubCategory() == null ? null : eventToUpdate.getSubCategory().getCode();
        return !eventDto.getDescription().equals(eventToUpdate.getDescription())
                || (eventDto.getAddress() != null && !eventDto.getAddress().equals(eventToUpdate.getAddress()))
                || !eventDto.getCriticality().equals(eventToUpdate.getCriticality())
                || !Objects.equals(eventDto.getDomain(),
                        eventToUpdate.getDomain() == null ? null : eventToUpdate.getDomain().getCode())
                || !eventDto.getName().equals(eventToUpdate.getName())
                || !eventDto.getCategory().equals(eventToUpdate.getCategory().getCode())
                || !Objects.equals(eventDto.getSubCategory(), subCode);
    }

    private void enrichEquipmentFromUpdatedEvent(Integer eventId, UUID currentEquipmentId, UUID previousEquipmentId) {
        Event createdOrUpdatedEvent = databaseReader.getEventById(eventId);
        equipmentEnrichedProducer.updateFor(createdOrUpdatedEvent);

        if (previousEquipmentId != null && currentEquipmentId != null &&
                !currentEquipmentId.equals(previousEquipmentId)) {
            Equipment equipment = equipmentService.getEquipmentById(previousEquipmentId);
            equipmentEnrichedProducer.updateFor(equipment);
        }
    }

    private void checkDatesCoherence(EventWriteDto e) { // FIXME: il faut eviter les reference au categories propres à chalons dans le code
        if (e.getStartDate() != null && e.getEndDate() != null && e.getEndDate().isBefore(e.getStartDate())) {
            throw new IllegalArgumentException("End date is invalid: it must be after start date");
        }
        logger.debugf("Check if event %s has manifestation dates".formatted(e.getId()));
        if (e.getCategory().equals("MANIFESTATION") && (e.getStartDate() == null || e.getEndDate() == null)) {
            throw new IllegalArgumentException(
                    "Properties 'startDate' and 'endDate' are required for 'MANIFESTATION' category");
        }
    }

    private void checkSubCategoryCoherence(EventWriteDto eventDto) {
        var category = getCategory(eventDto.getCategory());
        var subCategories = databaseReader.getSubCategories(category).map(EnumEntity::getCode).toList();

        if (!subCategories.isEmpty()) {
            logger.debugf("Category %s has subcategories", category);
            if (eventDto.getSubCategory() == null || !subCategories.contains(eventDto.getSubCategory())) {
                throw new ForbiddenException("Subcategory is required. Valid subcategories are %s for category %s"
                        .formatted(subCategories, category.getCode()));
            }
        } else {
            logger.debugf("Category %s hasn't subcategories", category);
            if (eventDto.getSubCategory() != null) {
                throw new ForbiddenException(
                        "Invalid subcategory. No subcategories are available for category %s"
                                .formatted(eventDto.getCategory()));
            }
        }
    }

    private List<Service> getLastUndoneServiceFromProcedureOrEquipment(Event event) {

        Stream<Service> equipmentServices = event.getEquipment() == null
                ? Stream.of()
                : event.getEquipment()
                        .getServices()
                        .stream();

        return equipmentServices
                .filter(service -> service.getStatus() == ASKED || service.getStatus() == IN_PROGRESS
                        || service.getStatus() == NEW)
                .sorted(compareByStatusThenLastDate()).distinct()
                .toList();
    }

    private Stream<Service> getProcedureServices(Event event) {
        return event.getProcedure() == null
                ? Stream.of()
                : event
                        .getProcedure()
                        .getActions()
                        .stream()
                        .filter(action -> action.getType().equals(ActionType.ASKED_SERVICE.name())
                                && ((AskedService) action).getServiceExternalId() != null)
                        .map(action -> serviceService.getServiceByExternalId(((AskedService) action).getServiceExternalId()))
                        .filter(Objects::nonNull);
    }

    private Comparator<Service> compareByStatusThenLastDate() {
        Comparator<Service> compareByStatusThenReversedDate = Comparator
                .comparing(service -> service.getStatus().getPriority(), Comparator.reverseOrder());
        compareByStatusThenReversedDate.thenComparing(Service::getLastModificationDate, Comparator.reverseOrder());
        return compareByStatusThenReversedDate;
    }

}
