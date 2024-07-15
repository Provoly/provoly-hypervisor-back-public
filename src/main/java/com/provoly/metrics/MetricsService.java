package com.provoly.metrics;

import static com.provoly.metrics.MetricsDatabaseReader.UNMANAGED;
import static com.provoly.service.ServiceStatus.ASKED;
import static com.provoly.service.ServiceStatus.IN_PROGRESS;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.provoly.equipment.Equipment;
import com.provoly.equipment.EquipmentService;
import com.provoly.equipment.Family;
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
            Collection<String> entities, Collection<String> places) {

        var equipments = getEquipmentForEventsExceptManifestation("EP",
                criticalities,
                categories,
                entities,
                places);
        return buildEpEquimentWithEvent(equipments);

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

        var equipmentsWithEvent = buildEpEquimentWithEvent(equipmentByCategory
                .values()
                .stream()
                .flatMap(Collection::stream)
                .distinct()
                .toList());

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
        var equipments = getEquipmentForEventsExceptManifestation("VP",
                criticalities,
                categories,
                entities,
                places);
        return buildVpEquipmentWithEventsDto(equipments);
    }

    @Transactional
    public VpEquipmentWithEventsDetailedDto getVpEquipmentsWithEventDetailed() {
        var domainEntity = metricsDatabaseReader.getDomainByCode("VP");
        var equipmentByCategory = metricsDatabaseReader.getEquipmentsByEventCategory(domainEntity, List.of(), List.of(),
                List.of(),
                List.of());

        var equipmentsWithEvent = buildVpEquipmentWithEventsDto(
                equipmentByCategory.values().stream().flatMap(Collection::stream).distinct().toList());

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
        Family family = equipmentService.getFamilyByCode(code);
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

        var families = family.stream().map(code -> equipmentService.getFamilyByCode(code).getId()).toList();
        var entities = entity.stream().map(code -> equipmentService.getEquipmentEntity(code).getId()).toList();
        var districts = place.stream().map(code -> equipmentService.getDistrictByCode(code).getId()).toList();
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
    public Map<String, Long> getAnomalyEventsGroupedBySubCategories(String domain, Instant date, String status) {
        logger.infof("""
                Get number of ANOMALY events grouped by sub categories, filter on
                domain : %s,
                date: %s,
                status: %s
                """, domain, date, status);
        var domainEntity = getDomainByCode(domain);
        return metricsDatabaseReader.getAnomalyEventsBySubCategories(domainEntity, date,
                Status.fromString(status));
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
            Instant startDate) {

        startDate = startDate != null ? startDate : Instant.now();
        logger.infof("""
                Aggregate anomaly events in the last %s %s from %s
                filter on
                domain: %s
                """.formatted(buckets, interval, startDate, domain));

        var domainId = domain != null
                ? metricsDatabaseReader.getDomainByCode(domain).getId()
                : null;

        return metricsDatabaseReader.aggregateAnomaliesEvents(
                interval,
                buckets,
                startDate,
                domainId);
    }

    @Transactional
    public Collection<EventsByEquipment> getEventsByEquipments(String domain, String category, int limit, Instant date) {
        logger.debugf("""
                Get Events counts by equipments filter on :
                domain : %s,
                category : %s,
                from date %s
                """, domain, category, date);

        var domainEntity = getDomainByCode(domain);
        var eventCategory = eventService.getCategory(category);

        return metricsDatabaseReader.getEventsByEquipments(
                domainEntity,
                eventCategory,
                limit,
                date);
    }

    private Collection<Equipment> getEquipmentForEventsExceptManifestation(String domain,
            Collection<String> criticalities,
            Collection<String> categories,
            Collection<String> entities,
            Collection<String> places) {
        logger.infof("""
                Get %s equipments with events
                filter on
                criticality : %s,
                category : %s,
                equipment entity : %s
                places: %s
                """.formatted(domain, criticalities, categories, entities, places));
        var domainEntity = metricsDatabaseReader.getDomainByCode(domain);
        var eventCriticalities = criticalities.stream().map(Criticality::fromString).toList();
        var eventCategories = categories.stream().map(eventService::getCategory).toList();
        var districts = places.stream().map(equipmentService::getDistrictByCode).toList();
        var equipmentEntities = entities.stream().map(equipmentService::getEquipmentEntity).toList();

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

    private EpEquipmentWithEventsDto buildEpEquimentWithEvent(Collection<Equipment> equipmentWithUndoneEvents) {
        logger.debug("grouped by familiy and managed/unmanaged");
        var equipments = metricsDatabaseReader.getEpEquipmentByFamily(equipmentWithUndoneEvents);

        logger.debug("Get all equipments grouped by family");
        var totalEquipmentWithEvent = metricsDatabaseReader.getTotalEpEquipmentByFamily();

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

    private VpEquipmentWithEventsDto buildVpEquipmentWithEventsDto(Collection<Equipment> equipmentWithUndoneEvents) {
        var totalEquipments = metricsDatabaseReader.getTotalVpEquipments();

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
