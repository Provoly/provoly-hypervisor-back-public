package com.provoly.metrics;

import static com.provoly.metrics.MetricsDatabaseReader.UNMANAGED;
import static com.provoly.service.ServiceStatus.ASKED;
import static com.provoly.service.ServiceStatus.IN_PROGRESS;

import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.provoly.equipment.EquipmentService;
import com.provoly.equipment.Family;
import com.provoly.event.*;
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
    public EquipmentWithEventsDto getEquipmentsWithEventMetrics(List<String> criticalities, List<String> categories,
            List<String> entities) {

        logger.infof("""
                filter on
                criticality : %s,
                category : %s,
                equipment entity : %s
                """.formatted(criticalities, categories, entities));

        logger.debug("Get all equipment linked with at least one undone event which is not a Manifestation");
        var equipmentWithUndoneEvents = metricsDatabaseReader.getEquipmentsWithUnDoneEvents(entities, criticalities,
                categories);

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
        logger.infof("Get all equipment by entity for EP domain and family", code);
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

    private Long getServicesForEquipmentAndStatus(Map<String, Map<ServiceStatus, Long>> servicesByEquipments, String code,
            ServiceStatus status) {
        return servicesByEquipments.getOrDefault(code, Map.of(status, 0L)).getOrDefault(status, 0L);
    }
}
