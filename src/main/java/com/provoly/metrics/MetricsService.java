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

import com.provoly.EnumEntity;
import com.provoly.equipment.EquipmentService;
import com.provoly.equipment.Family;
import com.provoly.event.Category;
import com.provoly.event.Criticality;
import com.provoly.event.Domain;
import com.provoly.service.ServiceStatus;

import org.jboss.logging.Logger;

@ApplicationScoped
public class MetricsService {
    public static final String ARMOIRE_CODE = "EP_ARMOIRE";
    public static final String FOYER_LUMINEUX_CODE = "EP_FOYER_LUMINEUX";
    private Logger logger;
    private MetricsDatabaseReader metricsDatabaseReader;
    private EquipmentService equipmentService;

    public MetricsService(Logger logger, MetricsDatabaseReader metricsDatabaseReader, EquipmentService equipmentService) {
        this.logger = logger;
        this.metricsDatabaseReader = metricsDatabaseReader;
        this.equipmentService = equipmentService;
    }

    @Transactional
    public EquipmentWithEventsDto getEquipmentsWithEventMetrics(Collection<String> criticalities, Collection<String> categories,
            Collection<String> entities) {

        logger.infof("""
                Get equipments with events
                filter on
                criticality : %s,
                category : %s,
                equipment entity : %s
                """.formatted(criticalities, categories, entities));

        var eventCriticalities = criticalities.stream().map(Criticality::fromString).toList();
        var eventCategories = categories.stream().map(Category::fromString).toList();

        logger.debug("Get all equipment linked with at least one undone event which is not a Manifestation");
        var equipmentWithUndoneEvents = metricsDatabaseReader.getEquipmentsWithUnDoneEvents(entities, eventCriticalities,
                eventCategories);

        logger.debugf("grouped by Managed/ unmanaged");
        var equipments = metricsDatabaseReader.equipmentsCountGroupedByManaged(equipmentWithUndoneEvents);

        logger.debug("Get all equipments grouped by family");
        var totalEquipmentWithEvent = metricsDatabaseReader.getTotalEquipmentGroupedByFamilyAndManaged();

        logger.debug("Get services equipments grouped by family and service status");
        var servicesByEquipments = metricsDatabaseReader.getEquipmentServicesByStatus(equipmentWithUndoneEvents);

        return new EquipmentWithEventsDto(
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
    public EquipmentByEntityDto getTotalEquipmentsByEntity(String code) {
        logger.infof("Get all equipments by entity for EP domain and family %s", code);
        Family family = equipmentService.getFamilyByCode(code);
        Domain domain = metricsDatabaseReader.getDomainByCode("EP").get();

        var result = metricsDatabaseReader.getEquipmentsGroupByEntityAndManaged(family, domain);
        return new EquipmentByEntityDto(
                result.getOrDefault("AGGLO_EP_managed", 0L) + result.getOrDefault("AGGLO_COMMUN_managed", 0L),
                result.getOrDefault("AGGLO_EP_unmanaged", 0L) + result.getOrDefault("AGGLO_COMMUN_unmanaged", 0L),
                result.getOrDefault("CHALONS_EP_managed", 0L) + result.getOrDefault("CHALONS_COMMUN_managed", 0L),
                result.getOrDefault("CHALONS_EP_unmanaged", 0L) + result.getOrDefault("CHALONS_COMMUN_unmanaged", 0L),
                result.getOrDefault("FAGNIERES_COMMUN_managed", 0L),
                result.getOrDefault("FAGNIERES_COMMUN_unmanaged", 0L),
                result.getOrDefault("SAINT_MARTIN_COMMUN_managed", 0L),
                result.getOrDefault("SAINT_MARTIN_COMMUN_unmanaged", 0L));

    }

    @Transactional
    public List<AggregateServiceDto> aggregateDoneServices(DateInterval interval,
            Instant date,
            int nbBuckets,
            Collection<String> family,
            Collection<String> entity) {

        date = date != null ? date : Instant.now();
        logger.infof("""
                Aggregate done services in the last %s %s from %s
                filter on
                family : %s,
                equipment entity : %s
                """.formatted(nbBuckets, interval, date, family, entity));

        var families = family.isEmpty() ? equipmentService.getFamilies().stream().map(EnumEntity::getId).toList()
                : family.stream().map(code -> equipmentService.getFamilyByCode(code).getId()).toList();
        var entities = entity.isEmpty() ? equipmentService.getEquipmentEntities().stream().map(EnumEntity::getId).toList()
                : entity.stream().map(code -> equipmentService.getEquipmentEntity(code).getId()).toList();

        return metricsDatabaseReader.aggregateDoneServices(
                interval,
                nbBuckets,
                date,
                families,
                entities);
    }

    private Long getServicesForEquipmentAndStatus(Map<String, Map<ServiceStatus, Long>> servicesByEquipments, String code,
            ServiceStatus status) {
        return servicesByEquipments.getOrDefault(code, Map.of(status, 0L)).getOrDefault(status, 0L);
    }
}
