package com.provoly.metrics;

import static com.provoly.metrics.MetricsDatabaseReader.UNMANAGED;
import static com.provoly.service.ServiceStatus.ASKED;
import static com.provoly.service.ServiceStatus.IN_PROGRESS;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.provoly.equipment.*;
import com.provoly.event.Criticality;
import com.provoly.event.Domain;
import com.provoly.event.EventService;
import com.provoly.event.Status;
import com.provoly.service.ServiceStatus;

import org.jboss.logging.Logger;

@ApplicationScoped
public class MetricsService {
    public static final String ARMOIRE_CODE = "EP_ARMOIRE";
    public static final String FOYER_LUMINEUX_CODE = "EP_FOYER_LUMINEUX";
    public static final String MANIFESTATION = "MANIFESTATION";
    public static final String LIMIT = "LIMIT";
    public static final String OUTOFORDER = "OUTOFORDER";
    public static final String ANOMALY = "ANOMALY";

    private final Logger logger;
    private final MetricsDatabaseReader metricsDatabaseReader;
    private final EquipmentService equipmentService;
    private final EventService eventService;

    public MetricsService(Logger logger, MetricsDatabaseReader metricsDatabaseReader, EquipmentService equipmentService,
            EventService eventService) {
        this.logger = logger;
        this.metricsDatabaseReader = metricsDatabaseReader;
        this.equipmentService = equipmentService;
        this.eventService = eventService;
    }

    @Transactional
    public EpEquipmentWithEventsDto getEpEquipmentsWithEvent(
            Collection<String> criticalities,
            Collection<String> categories,
            Collection<String> entities,
            Collection<String> places) {

        var districts = places.stream().map(equipmentService::getDistrictByCodeOrNull).toList();
        var entitiesEntity = entities.stream().map(equipmentService::getEquipmentEntityOrNull).toList();

        var equipments = getEquipmentForEventsExceptManifestation("EP",
                criticalities,
                categories,
                entitiesEntity,
                districts);
        return buildEpEquimentWithEvent(equipments, entitiesEntity, districts);

    }

    @Transactional
    public EpEquipmentWithEventsDetailedDto getEpEquipmentWithEventDetailed() {
        var domainEntity = metricsDatabaseReader.getDomainByCode("EP");

        logger.debug("Get equipments with events grouped by event category");
        var equipmentByCategory = metricsDatabaseReader.getEquipmentsByEventCategory(domainEntity,
                List.of(),
                List.of(),
                List.of(),
                List.of());

        List<Equipment> equipments = equipmentByCategory
                .values()
                .stream()
                .flatMap(Collection::stream)
                .distinct()
                .toList();

        var equipmentsWithEvent = buildEpEquimentWithEvent(equipments, List.of(), List.of());

        var equipmentsByFamily = equipmentByCategory.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        item -> metricsDatabaseReader.getEpEquipmentByFamily(item.getValue())));

        return new EpEquipmentWithEventsDetailedDto(equipmentsWithEvent,
                buildEpEquipmentByCategory(MANIFESTATION, equipmentsByFamily),
                buildEpEquipmentByCategory(LIMIT, equipmentsByFamily),
                buildEpEquipmentByCategory(OUTOFORDER, equipmentsByFamily),
                buildEpEquipmentByCategory(ANOMALY, equipmentsByFamily));

    }

    @Transactional
    public VpEquipmentWithEventsDto getVpEquipmentsWithEvent(
            List<String> criticalities,
            List<String> categories,
            List<String> entities,
            List<String> places) {

        var districts = places.stream().map(equipmentService::getDistrictByCodeOrNull).toList();
        var entitiesEntity = entities.stream().map(equipmentService::getEquipmentEntityOrNull).toList();

        var equipments = getEquipmentForEventsExceptManifestation("VP",
                criticalities,
                categories,
                entitiesEntity,
                districts);
        return buildVpEquipmentWithEventsDto(equipments, districts, entitiesEntity);
    }

    @Transactional
    public VpEquipmentWithEventsDetailedDto getVpEquipmentsWithEventDetailed() {
        var domainEntity = metricsDatabaseReader.getDomainByCode("VP");
        var equipmentByCategory = metricsDatabaseReader.getEquipmentsByEventCategory(domainEntity, List.of(), List.of(),
                List.of(),
                List.of());

        List<Equipment> equipments = equipmentByCategory.values().stream().flatMap(Collection::stream).distinct().toList();
        var equipmentsWithEvent = buildVpEquipmentWithEventsDto(equipments, List.of(), List.of());

        return new VpEquipmentWithEventsDetailedDto(
                equipmentsWithEvent,
                new VpEquipmentByCategoryDto(equipmentByCategory.getOrDefault(MANIFESTATION, List.of()).size()),
                new VpEquipmentByCategoryDto(equipmentByCategory.getOrDefault(LIMIT, List.of()).size()),
                new VpEquipmentByCategoryDto(equipmentByCategory.getOrDefault(OUTOFORDER, List.of()).size()),
                new VpEquipmentByCategoryDto(equipmentByCategory.getOrDefault(ANOMALY, List.of()).size()));
    }

    @Transactional
    public EquipmentByEntityDto getTotalEpEquipmentsByEntity(String code) {
        logger.infof("Get all equipments by entity for EP domain and family %s", code);
        Family family = equipmentService.getFamilyByCodeOrNull(code);
        Domain domain = getDomainByCode("EP");

        var result = metricsDatabaseReader.getEpEquipmentsByEntity(family, domain);
        return new EquipmentByEntityDto(
                result.getOrDefault("AGGLO-COMMUN_managed", 0L),
                result.getOrDefault("AGGLO-COMMUN_unmanaged", 0L),
                result.getOrDefault("CHALONS-COMMUN_managed", 0L),
                result.getOrDefault("CHALONS-COMMUN_unmanaged", 0L),
                result.getOrDefault("FAGNIERES-COMMUN_managed", 0L),
                result.getOrDefault("FAGNIERES-COMMUN_unmanaged", 0L),
                result.getOrDefault("SAINT-MARTIN-COMMUN_managed", 0L),
                result.getOrDefault("SAINT-MARTIN-COMMUN_unmanaged", 0L));

    }

    @Transactional
    public List<AggregateServiceDto> aggregateDoneServices(DateInterval interval,
            Instant date,
            String domain,
            int nbBuckets,
            Collection<String> family,
            Collection<String> entity,
            Collection<String> place) {

        date = date != null ? date : Instant.now();
        logger.infof("""
                Aggregate done services in the last %s %s from %s
                filter on
                category : curative
                domain: %s
                family : %s,
                equipment entity : %s
                place: %s
                """.formatted(nbBuckets, interval, date, domain, family, entity, place));

        var families = family.stream()
                .map(code -> code == null || code.isEmpty() ? null : equipmentService.getFamilyByCodeOrNull(code).getId())
                .toList();
        var entities = entity.stream()
                .map(code -> code == null || code.isEmpty() ? null : equipmentService.getEquipmentEntityOrNull(code).getId())
                .toList();
        var districts = place.stream()
                .map(code -> code == null || code.isEmpty() ? null : equipmentService.getDistrictByCodeOrNull(code).getId())
                .toList();
        var domainId = domain != null
                ? metricsDatabaseReader.getDomainByCode(domain).getId()
                : null;

        return metricsDatabaseReader.aggregateDoneServices(
                interval,
                nbBuckets,
                date,
                domainId,
                families,
                entities,
                districts);
    }

    @Transactional
    public Map<String, Long> getAnomalyEventsBySubCategories(String domain,
            Instant date,
            String status,
            List<String> place,
            List<String> entity,
            List<String> criticality,
            List<String> family,
            String name) {
        logger.infof("""
                Get number of ANOMALY events grouped by sub categories, filter on
                domain : %s,
                date: %s,
                status: %s,
                places: %s,
                entities: %s,
                criticalities: %s,
                family: %s
                equipment: %s
                """, domain, date, status, place, entity, criticality, family, name);

        var domainEntity = getDomainByCode(domain);
        var districts = place.stream().map(equipmentService::getDistrictByCodeOrNull).toList();
        var entities = entity.stream().map(equipmentService::getEquipmentEntityOrNull).toList();
        var families = family.stream().map(equipmentService::getFamilyByCodeOrNull).toList();

        List<String> criticalities = getCriticalityList(criticality);

        var equipmentName = name != null ? equipmentService.getEquipmentByName(name).getName() : null;

        return metricsDatabaseReader.getAnomalyEventsBySubCategories(domainEntity,
                date,
                Status.fromString(status),
                entities,
                districts,
                criticalities,
                families,
                equipmentName);
    }

    @Transactional
    public Collection<AnomalyQueryResult> getAnomalyEventsGroupedBySubCategoriesAndEntities(String domain, Instant startDate) {
        logger.infof("""
                Get number of ANOMALY events grouped by equipment entities and sub categories, filter on
                domain : %s,
                startDate: %s
                """, domain, startDate);
        var domainEntity = getDomainByCode(domain);

        return metricsDatabaseReader.getAnomalyEventsBySubCategoriesAndEntities(domainEntity, startDate);
    }

    @Transactional
    public Collection<AggregateServiceDto> aggregateAnomaliesEvents(DateInterval interval,
            int buckets,
            String domain,
            Instant startDate,
            List<String> place,
            List<String> entity,
            List<String> criticality,
            List<String> family) {

        startDate = startDate != null ? startDate : Instant.now();
        logger.infof("""
                Aggregate anomaly events in the last %s %s from %s
                filter on
                domain: %s,
                places : %s,
                entities: %s,
                criticalities: %s
                """.formatted(buckets, interval, startDate, domain, place, entity, criticality));

        var domainId = domain != null
                ? metricsDatabaseReader.getDomainByCode(domain).getId()
                : null;

        var families = family.stream()
                .map(code -> code == null || code.isEmpty() ? null : equipmentService.getFamilyByCodeOrNull(code).getId())
                .toList();
        var districts = place.stream()
                .map(code -> code == null || code.isEmpty() ? null : equipmentService.getDistrictByCode(code).getId()).toList();
        var entities = entity.stream()
                .map(code -> code == null || code.isEmpty() ? null : equipmentService.getEquipmentEntity(code).getId())
                .toList();
        var criticalities = getCriticalityList(criticality);

        return metricsDatabaseReader.aggregateAnomaliesEvents(
                interval,
                buckets,
                startDate,
                domainId,
                districts,
                entities,
                criticalities,
                families);
    }

    @Transactional
    public Collection<EventsByEquipment> getEventsByEquipments(String domain,
            String category,
            int limit,
            Instant date,
            List<String> place,
            List<String> entity,
            List<String> criticality,
            List<String> family) {
        logger.debugf("""
                Get Events counts by equipments filter on :
                domain : %s,
                category : %s,
                from date %s
                place: %s,
                entity: %s,
                criticality: %s,
                family: %s
                """, domain, category, date, place, entity, criticality, family);

        var domainEntity = getDomainByCode(domain);
        var eventCategory = eventService.getCategoryOrNull(category);

        var districts = place.stream().map(equipmentService::getDistrictByCodeOrNull).toList();
        var entities = entity.stream().map(equipmentService::getEquipmentEntityOrNull).toList();
        var families = family.stream().map(equipmentService::getFamilyByCodeOrNull).toList();
        var criticalities = getCriticalityList(criticality);

        return metricsDatabaseReader.getEventsByEquipments(
                domainEntity,
                eventCategory,
                limit,
                date,
                districts,
                entities,
                criticalities,
                families);
    }

    private Collection<Equipment> getEquipmentForEventsExceptManifestation(String domain,
            Collection<String> criticalities,
            Collection<String> categories,
            Collection<EquipmentEntity> equipmentEntities,
            Collection<District> districts) {
        logger.infof("""
                Get %s equipments with events
                filter on
                criticality : %s,
                category : %s,
                equipment entity : %s
                places: %s
                """.formatted(domain, criticalities, categories, equipmentEntities, districts));
        var domainEntity = metricsDatabaseReader.getDomainByCode(domain);
        var eventCriticalities = getCriticalityList(criticalities);
        var eventCategories = categories.stream().map(eventService::getCategoryOrNull).toList();

        logger.debugf("Get all %s equipments linked with at least one undone event which is not a Manifestation", domain);
        return metricsDatabaseReader.getEquipmentsByEventCategory(
                domainEntity,
                equipmentEntities,
                eventCriticalities,
                eventCategories,
                districts)
                .entrySet()
                .stream()
                .filter(item -> !item.getKey().equals(MANIFESTATION))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .distinct()
                .toList();
    }

    private EpEquipmentWithEventsDto buildEpEquimentWithEvent(Collection<Equipment> equipmentWithUndoneEvents,
            List<EquipmentEntity> entitiesEntity, List<District> districts) {
        logger.debug("grouped by familiy and managed/unmanaged");
        var equipments = metricsDatabaseReader.getEpEquipmentByFamily(equipmentWithUndoneEvents);

        logger.debug("Get all equipments grouped by family");
        var totalEquipmentWithEvent = metricsDatabaseReader.getTotalEpEquipmentByFamily(entitiesEntity, districts);

        logger.debug("Get ep services equipments grouped by family and service status");
        var servicesByEquipments = metricsDatabaseReader.getEpServicesByStatus(equipmentWithUndoneEvents);

        return new EpEquipmentWithEventsDto(
                equipments.getOrDefault(ARMOIRE_CODE, 0L),
                totalEquipmentWithEvent.getOrDefault(ARMOIRE_CODE, 0L),
                getServicesForEquipmentAndStatus(servicesByEquipments, ARMOIRE_CODE, ASKED),
                getServicesForEquipmentAndStatus(servicesByEquipments, ARMOIRE_CODE, IN_PROGRESS),

                equipments.getOrDefault(FOYER_LUMINEUX_CODE, 0L),
                totalEquipmentWithEvent.getOrDefault(FOYER_LUMINEUX_CODE, 0L),
                getServicesForEquipmentAndStatus(servicesByEquipments, FOYER_LUMINEUX_CODE, ASKED),
                getServicesForEquipmentAndStatus(servicesByEquipments, FOYER_LUMINEUX_CODE, IN_PROGRESS),

                equipments.getOrDefault(UNMANAGED, 0L),
                totalEquipmentWithEvent.getOrDefault(UNMANAGED, 0L),
                getServicesForEquipmentAndStatus(servicesByEquipments, UNMANAGED, ASKED),
                getServicesForEquipmentAndStatus(servicesByEquipments, UNMANAGED, IN_PROGRESS));
    }

    private VpEquipmentWithEventsDto buildVpEquipmentWithEventsDto(Collection<Equipment> equipmentWithUndoneEvents,
            List<District> districts, List<EquipmentEntity> entitiesEntity) {
        var totalEquipments = metricsDatabaseReader.getTotalVpEquipments(districts, entitiesEntity);

        logger.debug("Get vp services equipments grouped by family and service status");
        var servicesByEquipments = metricsDatabaseReader.getVpServicesByStatus(equipmentWithUndoneEvents);

        return new VpEquipmentWithEventsDto(
                equipmentWithUndoneEvents.size(),
                totalEquipments,
                servicesByEquipments.getOrDefault(ASKED, 0L),
                servicesByEquipments.getOrDefault(IN_PROGRESS, 0L));
    }

    private Domain getDomainByCode(String code) {
        return code == null ? null : metricsDatabaseReader.getDomainByCode(code);
    }

    private List<String> getCriticalityList(Collection<String> criticality) {
        if (criticality.isEmpty()) {
            return List.of();
        }

        return criticality.stream().noneMatch(Objects::nonNull)
                ? List.of("null")
                : criticality.stream().map(c -> Criticality.fromString(c).name()).toList();
    }

    private Long getServicesForEquipmentAndStatus(Map<String, Map<ServiceStatus, Long>> servicesByEquipments, String code,
            ServiceStatus status) {
        return servicesByEquipments.getOrDefault(code, Map.of(status, 0L)).getOrDefault(status, 0L);
    }

    private EpEquipmentByCategoryDto buildEpEquipmentByCategory(String category,
            Map<String, Map<String, Long>> equipmentsByCategory) {
        return new EpEquipmentByCategoryDto(
                equipmentsByCategory.getOrDefault(category, Map.of()).getOrDefault(ARMOIRE_CODE, 0L),
                equipmentsByCategory.getOrDefault(category, Map.of()).getOrDefault(FOYER_LUMINEUX_CODE, 0L),
                equipmentsByCategory.getOrDefault(category, Map.of()).getOrDefault(UNMANAGED, 0L));
    }

}
