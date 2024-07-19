package com.provoly.event;

import static com.provoly.service.ServiceStatus.ASKED;
import static com.provoly.service.ServiceStatus.IN_PROGRESS;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;

import com.provoly.EnumEntity;
import com.provoly.EquipmentEnrichedProducer;
import com.provoly.action.ActionType;
import com.provoly.action.AskedService;
import com.provoly.equipment.Equipment;
import com.provoly.equipment.EquipmentService;
import com.provoly.event.dto.EventSummaryDto;
import com.provoly.event.dto.EventWriteDto;
import com.provoly.event.dto.EventsSummariesByStatusDto;
import com.provoly.service.Service;
import com.provoly.service.ServiceService;

import org.jboss.logging.Logger;

@ApplicationScoped
public class EventService {

    private final EventDatabaseReader databaseReader;
    private final EventMapper eventMapper;
    private final EquipmentService equipmentService;
    private final ServiceService serviceService;
    private final EquipmentEnrichedProducer equipmentEnrichedProducer;
    private final Logger logger;

    public EventService(EventDatabaseReader databaseReader, EventMapper eventMapper, EquipmentService equipmentService,
            ServiceService serviceService,
            EquipmentEnrichedProducer equipmentEnrichedProducer,
            Logger logger) {
        this.databaseReader = databaseReader;
        this.eventMapper = eventMapper;
        this.equipmentService = equipmentService;
        this.serviceService = serviceService;
        this.equipmentEnrichedProducer = equipmentEnrichedProducer;
        this.logger = logger;
    }

    @Transactional
    public Map<Status, EventsSummariesByStatusDto> getEventSummariesGroupByStatus(int limit, String criticality) {
        logger.infof("Get events summary with max %s events by status and criticality %s", limit, criticality);

        logger.debugf("Get events summary by status");
        Map<Status, List<EventSummaryDto>> eventSummaries = getEventSummariesGroupByStatus(limit,
                Criticality.fromString(criticality))
                .stream()
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

    private List<EventSummaryDto> getEventSummariesGroupByStatus(int limit, Criticality criticality) {
        var events = new ArrayList<>(databaseReader.getEvents(Status.NEW, limit, criticality));
        events.addAll(databaseReader.getEvents(Status.IN_PROGRESS, limit, criticality));
        events.addAll(databaseReader.getEvents(Status.DONE, limit, criticality));

        return events.stream()
                .map(event -> {
                    var allServices = getServicesFromProcedureAndEquipment(event);
                    return eventMapper.mapToEventSummaryDto(event, allServices.size(),
                            getLastInProgressOrAskedService(allServices));
                })
                .toList();
    }

    @Transactional
    public Collection<Event> getEvents(int page,
            int pageSize,
            String sort,
            String order,
            Instant creationDate,
            List<String> criticality,
            List<String> status,
            List<String> category,
            List<String> entity,
            List<String> family) {
        logger.infof("""
                Get events with from page %s and size %s with :
                creation date : %s
                criticality : %s,
                status : %s,
                category : %s,
                equipment entity : %s,
                equipment family : %s
                sort by %s
                """.formatted(page, pageSize, creationDate, criticality, status, category, entity, family, sort));

        if (oneOfFilterIsEmpty(criticality, status, category, entity, family)) {
            return List.of();
        }

        var criticalities = criticality.stream()
                .map(Criticality::fromString)
                .toList();

        var statuses = status.stream()
                .map(Status::fromString)
                .toList();

        var entities = entity.stream()
                .map(equipmentService::getEquipmentEntity)
                .toList();

        var families = family.stream()
                .map(equipmentService::getFamilyByCode).toList();

        var categories = category.stream()
                .map(this::getCategory).toList();

        return databaseReader.getEvents(page,
                pageSize,
                EventSort.fromName(sort),
                SortOrder.fromString(order),
                creationDate,
                criticalities,
                statuses,
                categories,
                entities,
                families);
    }

    private boolean oneOfFilterIsEmpty(List<String> criticality, List<String> status, List<String> category,
            List<String> entity, List<String> type) {
        return criticality.stream().anyMatch(String::isEmpty) ||
                status.stream().anyMatch(String::isEmpty) ||
                category.stream().anyMatch(String::isEmpty) ||
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
    public Event saveEvent(EventWriteDto eventDto) {
        logger.infof("Create %s event with name %s".formatted(eventDto.getCategory(), eventDto.getName()));
        checkNameAlreadyExists(eventDto.getName());
        checkManifestationCategory(eventDto);
        checkSubCategoryCoherence(eventDto);

        if ((eventDto.getExternalSourceRef() != null && !eventDto.getExternalSourceRef().isBlank())
                && eventDto.getEquipmentId() == null) {
            throw new ForbiddenException("Events with external source must provide an equipment.");
        }

        Event event = new Event();
        eventMapper.updateEvent(eventDto, event);
        databaseReader.saveEvent(event);
        enrichEquipmentFromUpdatedEvent(event.getId(), eventDto.getEquipmentId(), null);
        logger.debugf("Event %s is created".formatted(event.getId()));
        return event;
    }

    @Transactional
    public void updateEvent(Integer id, EventWriteDto eventDto) {
        logger.infof("Update %s event with name %s".formatted(eventDto.getCategory(), eventDto.getName()));
        checkManifestationCategory(eventDto);
        checkSubCategoryCoherence(eventDto);

        Event eventToUpdate = databaseReader.getEventById(id);
        var previousEquipmentId = eventToUpdate.getEquipment() != null ? eventToUpdate.getEquipment().getId() : null;

        if (eventDto.getExternalSourceRef() != null) {
            throw new ForbiddenException("Event %s has an external source and can't be updated.".formatted(eventDto.getId()));
        }

        if (!eventDto.getName().equals(eventToUpdate.getName())) {
            checkNameAlreadyExists(eventDto.getName());
        }

        eventMapper.updateEvent(eventDto, eventToUpdate);
        logger.debugf("Event %s is updated".formatted(id));
        enrichEquipmentFromUpdatedEvent(id, eventDto.getEquipmentId(), previousEquipmentId);
    }

    @Transactional
    public void closeEventById(Integer id) {
        var event = databaseReader.getEventById(id);
        logger.debugf("Close event with id %s and set close date".formatted(id));
        closeEvent(event);
    }

    public void closeEvent(Event event) {
        if (event.getStatus() != Status.DONE) {
            event.setStatus(Status.DONE);
            event.setCloseDate(Instant.now());
        }
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

    private void checkManifestationCategory(EventWriteDto e) { // FIXME: il faut eviter les reference au categories propres à chalons dans le code
        logger.debugf("Check if event %s has manifestation dates".formatted(e.getId()));
        if (e.getCategory().equals("MANIFESTATION")) {
            if (e.getStartDate() == null || e.getEndDate() == null) {
                throw new IllegalArgumentException(
                        "Properties 'startDate' and 'endDate' are required for 'MANIFESTATION' category");
            }
            if (e.getEndDate().isBefore(e.getStartDate())) {
                throw new IllegalArgumentException("End date is invalid: it must be after start date");
            }
        }
    }

    private void checkSubCategoryCoherence(EventWriteDto eventDto) {
        var category = getCategory(eventDto.getCategory());
        var subCategories = databaseReader.getSubCategories(category).map(EnumEntity::getCode).toList();

        if (!subCategories.isEmpty()) {
            logger.debugf("Category %s has subcategories", category);
            if (eventDto.getSubCategory() == null || !subCategories.contains(eventDto.getSubCategory())) {
                throw new ForbiddenException("Subcategory is required. Valid subcategories are %s for category %s"
                        .formatted(subCategories, eventDto.getName()));
            }
        } else {
            logger.debugf("Category %s has'nt subcategories", category);
            if (eventDto.getSubCategory() != null) {
                throw new ForbiddenException(
                        "No subcategories are avalaible for category %s".formatted(eventDto.getCategory()));
            }
        }
    }

    private void checkNameAlreadyExists(String name) {
        if (databaseReader.isEventWithNameExists(name)) {
            throw new IllegalArgumentException("Event with name '%s' already exists".formatted(name));
        }
    }

    private Collection<Service> getServicesFromProcedureAndEquipment(Event event) {
        var procedureServices = getProcedureServices(event);

        procedureServices.addAll(event.getEquipment() == null
                ? List.of()
                : event.getEquipment().getServices());

        return procedureServices.stream()
                .distinct()
                .toList();
    }

    private String getLastInProgressOrAskedService(Collection<Service> services) {
        return services.stream()
                .filter(service -> service.getStatus() == ASKED || service.getStatus() == IN_PROGRESS)
                .min(compareByStatusThenLastDate())
                .map(Service::getExternalId)
                .orElse(null);
    }

    private ArrayList<Service> getProcedureServices(Event event) {
        return event.getProcedure() == null
                ? new ArrayList<>()
                : new ArrayList<>(event
                        .getProcedure()
                        .getActions()
                        .stream()
                        .filter(action -> action.getType().equals(ActionType.ASKED_SERVICE.name())
                                && ((AskedService) action).getServiceExternalId() != null)
                        .map(action -> serviceService.getServiceByExternalId(((AskedService) action).getServiceExternalId()))
                        .toList());
    }

    private Comparator<Service> compareByStatusThenLastDate() {
        Comparator<Service> compareByStatusThenReversedDate = Comparator
                .comparing(service -> service.getStatus().getPriority(), Comparator.reverseOrder());
        compareByStatusThenReversedDate.thenComparing(Service::getLastModificationDate, Comparator.reverseOrder());
        return compareByStatusThenReversedDate;
    }

}
