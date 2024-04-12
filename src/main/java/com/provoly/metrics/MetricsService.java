package com.provoly.metrics;

import static com.provoly.metrics.MetricsDatabaseReader.UNMANAGED;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.provoly.equipment.EquipmentService;
import com.provoly.equipment.Family;
import com.provoly.event.Domain;
import com.provoly.event.Status;

import org.jboss.logging.Logger;

@ApplicationScoped
public class MetricsService {
    public static final String ARMOIRE_CODE = "EP_A";
    public static final String FOYER_LUMINEUX_CODE = "EP_FL";
    private Logger logger;
    private MetricsDatabaseReader metricsDatabaseReader;
    private EquipmentService equipmentService;

    public MetricsService(Logger logger, MetricsDatabaseReader metricsDatabaseReader, EquipmentService equipmentService) {
        this.logger = logger;
        this.metricsDatabaseReader = metricsDatabaseReader;
        this.equipmentService = equipmentService;
    }

    @Transactional
    public EquipmentWithEventsDto getEquipmentsWithEventMetrics() {
        logger.debug("Get all equipment linked with at least one undone event which is not a Manifestation");
        var equipmentWithUndoneEvents = metricsDatabaseReader.getEquipmentsWithUnDoneEvent();

        logger.debug("Get all equipments grouped by family");
        var totalEquipmentWithEvent = metricsDatabaseReader.getEquipmentGroupedByFamilyAndManaged();

        logger.debug("Get services equipments grouped by family and service status");
        var servicesByEquipments = metricsDatabaseReader.getEquipmentServicesByStatus();

        return new EquipmentWithEventsDto(
                equipmentWithUndoneEvents.getOrDefault(ARMOIRE_CODE, 0L),
                totalEquipmentWithEvent.getOrDefault(ARMOIRE_CODE, 0L),
                getServicesForEquipmentAndStatus(servicesByEquipments, ARMOIRE_CODE, Status.NEW),
                getServicesForEquipmentAndStatus(servicesByEquipments, ARMOIRE_CODE, Status.IN_PROGRESS),

                equipmentWithUndoneEvents.getOrDefault(FOYER_LUMINEUX_CODE, 0L),
                totalEquipmentWithEvent.getOrDefault(FOYER_LUMINEUX_CODE, 0L),
                getServicesForEquipmentAndStatus(servicesByEquipments, FOYER_LUMINEUX_CODE, Status.NEW),
                getServicesForEquipmentAndStatus(servicesByEquipments, FOYER_LUMINEUX_CODE, Status.IN_PROGRESS),

                equipmentWithUndoneEvents.getOrDefault(UNMANAGED, 0L),
                totalEquipmentWithEvent.getOrDefault(UNMANAGED, 0L),
                getServicesForEquipmentAndStatus(servicesByEquipments, UNMANAGED, Status.NEW),
                getServicesForEquipmentAndStatus(servicesByEquipments, UNMANAGED, Status.IN_PROGRESS));
    }

    @Transactional
    public EquipmentByEntityDto getEquipmentByEntity(String code) {
        logger.infof("Get all equipment by entity for EP domain and family", code);
        Family family = equipmentService.getFamilyByCode(code);
        Domain domain = metricsDatabaseReader.getDomainByName("EP").get();

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

    private Long getServicesForEquipmentAndStatus(Map<String, Map<Status, Long>> servicesByEquipments, String code,
            Status status) {
        return servicesByEquipments.getOrDefault(code, Map.of(status, 0L)).getOrDefault(status, 0L);
    }
}
