package com.provoly.event;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.provoly.EquipmentEnrichedProducer;
import com.provoly.action.Action;
import com.provoly.action.ActionType;
import com.provoly.action.Service;
import com.provoly.equipment.Equipment;
import com.provoly.equipment.EquipmentService;
import com.provoly.event.dto.*;

import org.jboss.logging.Logger;

@ApplicationScoped
public class EventService {

    private final EventDatabaseReader databaseReader;
    private final EventMapper eventMapper;
    private final EquipmentService equipmentService;
    private final EquipmentEnrichedProducer equipmentEnrichedProducer;
    private final Logger logger;

    public EventService(EventDatabaseReader databaseReader, EventMapper eventMapper, EquipmentService equipmentService,
            EquipmentEnrichedProducer equipmentEnrichedProducer,
            Logger logger) {
        this.databaseReader = databaseReader;
        this.eventMapper = eventMapper;
        this.equipmentService = equipmentService;
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
                new EventsSummariesByStatusDto(counts.get(Status.DONE), eventSummaries.getOrDefault(Status.DONE, List.of())),
                Status.IN_PROGRESS,
                new EventsSummariesByStatusDto(counts.get(Status.IN_PROGRESS),
                        eventSummaries.getOrDefault(Status.IN_PROGRESS, List.of())),
                Status.NEW,
                new EventsSummariesByStatusDto(counts.get(Status.NEW), eventSummaries.getOrDefault(Status.NEW, List.of())));
    }

    private List<EventSummaryDto> getEventSummariesGroupByStatus(int limit, Criticality criticality) {
        var events = new ArrayList<>(databaseReader.getEvents(Status.NEW, limit, criticality));
        events.addAll(databaseReader.getEvents(Status.IN_PROGRESS, limit, criticality));
        events.addAll(databaseReader.getEvents(Status.DONE, limit, criticality));

        return events.stream()
                .map(event -> {
                    var allServices = getServicesFromProcedureAndEquipment(event);
                    return eventMapper.mapToEventSummaryDto(event, allServices.size(),
                            getLastInProgressOrNewService(allServices));
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

        var operatorCategories = category.stream()
                .filter(OperatorCategory::isOperatorCategory)
                .map(OperatorCategory::valueOf).toList();

        var alertCategories = category.stream()
                .filter(AlertCategory::isAlertCategory)
                .map(AlertCategory::valueOf).toList();

        var reportCategories = category.stream()
                .filter(ReportCategory::isReportCategory)
                .map(ReportCategory::valueOf).toList();

        return databaseReader.getEvents(page,
                pageSize,
                Sort.fromName(sort),
                SortOrder.fromString(order),
                creationDate,
                criticalities,
                statuses,
                operatorCategories,
                alertCategories,
                reportCategories,
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
    public Event getEventDetails(UUID id) {
        logger.infof("Get event details with id  %s".formatted(id));
        return databaseReader.getEventById(id);
    }

    @Transactional
    public ResponseCode saveOrUpdateEvent(EventWriteDto eventDto) {
        boolean isNewEvent = true;

        UUID previousEquipmentId = null;
        logger.infof("Update %s event with id %s", eventDto.getType(), eventDto.getId());

        if (databaseReader.isEventWithIdExists(eventDto.getId())) {
            Event eventToUpdate = databaseReader.getEventById(eventDto.getId());
            previousEquipmentId = eventToUpdate.getEquipment().getId();

            if (!eventDto.getName().equals(eventToUpdate.getName())) {
                checkIsNameAlreadyExists(eventDto.getName());
            }

            switch (eventDto) {
                case OperatorEventWriteDto dto -> updateOperatorEvent(dto);
                case ReportEventWriteDto dto -> updateReportEvent(dto);
                case AlertEventWriteDto dto -> {
                    logger.errorf("It's not possible to update event %s of type Alert.".formatted(dto.getId()));
                    throw new IllegalArgumentException(
                            "It's not possible to update event %s of type Alert.".formatted(dto.getId()));
                }
                default -> throw new IllegalStateException("Unexpected value: " + eventDto);
            }
            isNewEvent = false;

        } else {
            logger.infof("Event %s of type %s not exists, create it".formatted(eventDto.getId(), eventDto.getType()));
            checkIsNameAlreadyExists(eventDto.getName());
            switch (eventDto) {
                case OperatorEventWriteDto dto -> saveOperatorEvent(dto);
                case ReportEventWriteDto dto -> saveReportEvent(dto);
                case AlertEventWriteDto dto -> saveAlertEvent(dto);
                default -> throw new IllegalStateException("Unexpected value: " + eventDto);
            }
        }

        Event createdOrUpdatedEvent = databaseReader.getEventById(eventDto.getId());
        equipmentEnrichedProducer.updateFor(createdOrUpdatedEvent); // FIXME: https://github.com/Provoly/provoly-hypervisor-back/issues/71

        if (previousEquipmentId != null && eventDto.getEquipmentId() != null &&
                !eventDto.getEquipmentId().equals(previousEquipmentId)) {
            Equipment equipment = equipmentService.getEquipmentById(previousEquipmentId);
            equipmentEnrichedProducer.updateFor(equipment);
        }

        return isNewEvent ? ResponseCode.CREATED : ResponseCode.UPDATED;
    }

    @Transactional
    public void closeEventById(UUID id) {
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

    private void saveAlertEvent(AlertEventWriteDto dto) {
        if (dto.getEquipmentId() == null) {
            throw new IllegalArgumentException("Alert event must reference an equipment");
        }

        EventAlert event = new EventAlert(dto.getId());
        eventMapper.saveAlertEvent(dto, event);
        databaseReader.saveEvent(event);
    }

    private void saveOperatorEvent(OperatorEventWriteDto dto) {
        checkManifestationCategory(dto);
        EventOperator event = new EventOperator(dto.getId());
        eventMapper.updateOperatorEvent(dto, event);
        databaseReader.saveEvent(event);
        logger.debugf("Event %s successfully created".formatted(dto.getId()));
    }

    private void saveReportEvent(ReportEventWriteDto dto) {
        EventReport event = new EventReport(dto.getId());
        eventMapper.updateReportEvent(dto, event);
        databaseReader.saveEvent(event);
        logger.debugf("Event %s successfully created".formatted(dto.getId()));
    }

    private void updateOperatorEvent(OperatorEventWriteDto dto) {
        checkManifestationCategory(dto);
        EventOperator eventEntity = (EventOperator) databaseReader.getEventById(dto.getId());
        eventMapper.updateOperatorEvent(dto, eventEntity);
        logger.debugf("Event %s successfully updated".formatted(dto.getId()));
    }

    private void updateReportEvent(ReportEventWriteDto dto) {
        EventReport eventEntity = (EventReport) databaseReader.getEventById(dto.getId());
        if (!eventEntity.getExternalSourceRef().equals(dto.getExternalSourceRef())) {
            throw new IllegalArgumentException("It's not possible to update externalSourceRef value");
        }
        eventMapper.updateReportEvent(dto, eventEntity);
        logger.debugf("Event %s successfully updated".formatted(dto.getId()));
    }

    private void checkManifestationCategory(OperatorEventWriteDto e) {
        logger.debugf("Check if event %s has manifestation dates".formatted(e.getId()));
        if (e.getCategory() == OperatorCategory.MANIFESTATION) {
            if (e.getStartDate() == null || e.getEndDate() == null) {
                throw new IllegalArgumentException(
                        "Properties 'startDate' and 'endDate' are required for 'MANIFESTATION' category");
            }
            if (e.getEndDate().isBefore(e.getStartDate())) {
                throw new IllegalArgumentException("End date is invalid: it must be after start date");
            }
        }
    }

    private void checkIsNameAlreadyExists(String name) {
        if (databaseReader.isEventWithNameExists(name)) {
            throw new IllegalArgumentException("Event with name '%s' already exists".formatted(name));
        }
    }

    private List<Service> getServicesFromProcedureAndEquipment(Event event) {
        var procedureServices = getProcedureServices(event);

        procedureServices.addAll(event.getEquipment() == null
                ? List.of()
                : event.getEquipment().getServices());

        return procedureServices.stream()
                .distinct()
                .toList();
    }

    private String getLastInProgressOrNewService(List<Service> services) {
        return services.stream()
                .filter(service -> service.getStatus() == Status.IN_PROGRESS || service.getStatus() == Status.NEW)
                .min(compareByStatusThenLastDate())
                .map(Service::getName)
                .orElse(null);
    }

    private ArrayList<Service> getProcedureServices(Event event) {
        return event.getProcedure() == null
                ? new ArrayList<>()
                : new ArrayList<>(event
                        .getProcedure()
                        .getActions()
                        .stream()
                        .filter(action -> action.getType() == ActionType.SERVICE)
                        .map(Service.class::cast)
                        .toList());
    }

    private Comparator<Service> compareByStatusThenLastDate() {
        Comparator<Service> compareByStatusThenReversedDate = Comparator
                .comparing(service -> service.getStatus().getPriority(), Comparator.reverseOrder());
        compareByStatusThenReversedDate.thenComparing(Action::getLastModificationDate, Comparator.reverseOrder());
        return compareByStatusThenReversedDate;
    }

}
