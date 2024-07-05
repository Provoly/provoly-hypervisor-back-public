package com.provoly.metrics;

import static com.provoly.metrics.MetricsDatabaseReader.UNMANAGED;
import static com.provoly.service.ServiceStatus.ASKED;
import static com.provoly.service.ServiceStatus.IN_PROGRESS;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
    public static final String CAMERA_CODE = "VP_CAM";
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
    public EpEquipmentWithEventsDto getEpEquipmentsWithEventMetrics(
            Collection<String> criticalities,
            Collection<String> categories,
            Collection<String> entities, Collection<String> places) {

        var equipmentWithUndoneEvents = getEquipmentWithUndoneEvents("EP", criticalities, categories, entities, places);

        logger.debugf("grouped by Managed/ unmanaged");
        var equipments = metricsDatabaseReader.equipmentsCountGroupedByManaged(equipmentWithUndoneEvents);

        logger.debug("Get all equipments grouped by family");
        var totalEquipmentWithEvent = metricsDatabaseReader.getTotalEpEquipmentGroupedByFamilyAndManaged();

        logger.debug("Get services equipments grouped by family and service status");
        var servicesByEquipments = metricsDatabaseReader.getEpEquipmentServicesByStatus(equipmentWithUndoneEvents);

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

    @Transactional
    public VpEquipmentWithEventsDto getVpEquipmentsWithEventMetrics(
            List<String> criticalities,
            List<String> categories,
            List<String> entities,
            List<String> places) {
        var equipmentWithUndoneEvents = getEquipmentWithUndoneEvents("VP", criticalities, categories, entities, places);
        var totalEquipments = metricsDatabaseReader.getTotalVpEquipments();

        logger.debug("Get services equipments grouped by family and service status");
        var servicesByEquipments = metricsDatabaseReader.getVpEquipmentServicesByStatus(equipmentWithUndoneEvents);

        return new VpEquipmentWithEventsDto(
                equipmentWithUndoneEvents.size(),
                totalEquipments,
                servicesByEquipments.getOrDefault(ASKED, 0L),
                servicesByEquipments.getOrDefault(IN_PROGRESS, 0L));
    }

    @Transactional
    public EquipmentByEntityDto getTotalEquipmentsByEntity(String code) {
        logger.infof("Get all equipments by entity for EP domain and family %s", code);
        Family family = equipmentService.getFamilyByCode(code);
        Domain domain = metricsDatabaseReader.getDomainByCode("EP");

        var result = metricsDatabaseReader.getEquipmentsGroupByEntityAndManaged(family, domain);
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

    private Collection<Equipment> getEquipmentWithUndoneEvents(String domain,
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
        var domainId = metricsDatabaseReader.getDomainByCode(domain).getId();
        var eventCriticalities = criticalities.stream().map(Criticality::fromString).toList();
        var eventCategories = categories.stream().map(eventService::getCategory).toList();
        var districts = places.stream().map(equipmentService::getDistrictByCode).toList();

        logger.debugf("Get all %s equipments linked with at least one undone event which is not a Manifestation", domain);
        return metricsDatabaseReader.getEquipmentsWithUnDoneEvents(domainId, entities,
                eventCriticalities,
                eventCategories, districts);
    }

    private Long getServicesForEquipmentAndStatus(Map<String, Map<ServiceStatus, Long>> servicesByEquipments, String code,
            ServiceStatus status) {
        return servicesByEquipments.getOrDefault(code, Map.of(status, 0L)).getOrDefault(status, 0L);
    }

    public Map<String, Long> getAnomalyEventsGroupedBySubCategories(String domain, Instant date, String status) {
        var domainEntity = domain == null ? null : metricsDatabaseReader.getDomainByCode(domain);
        var anomalyCategory = metricsDatabaseReader.getCategoryByCode("ANOMALY");
        return metricsDatabaseReader.getAnomalyEventsGroupedBySubCategories(domainEntity, anomalyCategory, date,
                Status.fromString(status));
    }
}
