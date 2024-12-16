package com.provoly.service;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.provoly.action.ActionService;
import com.provoly.action.AskedService;
import com.provoly.equipmentenriched.EquipmentEnrichedProducer;
import com.provoly.service.coswin.CoswinService;

import org.jboss.logging.Logger;

@ApplicationScoped
public class ServiceService {
    private final Logger logger;
    private final ServiceDatabaseReader databaseReader;
    private final ServiceMapper serviceMapper;
    private final EquipmentEnrichedProducer equipmentEnrichedProducer;
    private final CoswinService coswinService;
    private final ActionService actionService;

    public ServiceService(Logger logger, ServiceDatabaseReader databaseReader, ServiceMapper serviceMapper,
            EquipmentEnrichedProducer equipmentEnrichedProducer, CoswinService coswinService, ActionService actionService) {
        this.logger = logger;
        this.databaseReader = databaseReader;
        this.serviceMapper = serviceMapper;
        this.equipmentEnrichedProducer = equipmentEnrichedProducer;
        this.coswinService = coswinService;
        this.actionService = actionService;
    }

    @Transactional
    public void saveOrUpdateServices(Collection<ServiceWriteDto> dtos) {
        logger.infof("Save or update %s services", dtos.size());
        dtos
                .forEach(dto -> {
                    checkStartAndEndDatePresence(dto);
                    checkCloseDatePresence(dto);
                    databaseReader.getServiceWithExternalId(dto.id())
                            .ifPresentOrElse(
                                    service -> {
                                        logger.infof("Service with external id %s already exists, update it", dto.id());

                                        var previousEquipment = service.getEquipment();
                                        serviceMapper.updateService(dto, service);

                                        if (service.getEquipment() != null) {
                                            equipmentEnrichedProducer.updateFor(service.getEquipment());
                                        }

                                        if (previousEquipment != null &&
                                                !service.getEquipment().getId().equals(previousEquipment.getId())) {
                                            logger.infof(
                                                    "Equipment for service %s is updated, update service number to previous equipment",
                                                    service.getId());
                                            equipmentEnrichedProducer.updateFor(previousEquipment);
                                        }
                                    },
                                    () -> {
                                        logger.infof("Service with external id %s not exists, create it", dto.id());
                                        var service = new Service(UUID.randomUUID(), dto.id());
                                        serviceMapper.updateService(dto, service);
                                        databaseReader.saveService(service);
                                        if (dto.equipment() != null) {
                                            equipmentEnrichedProducer.updateFor(service.getEquipment());
                                        }
                                    });
                });

    }

    private void checkStartAndEndDatePresence(ServiceWriteDto dto) {
        if (dto.startDate() == null || dto.endDate() == null) {
            throw new IllegalArgumentException(
                    "Cannot save or update service with status %s without startDate or endDate"
                            .formatted(dto.status()));
        }
    }

    private void checkCloseDatePresence(ServiceWriteDto dto) {
        if (dto.status() == ServiceStatus.DONE && dto.closeDate() == null) {
            throw new IllegalArgumentException(
                    "Cannot save or update service with status %s without closeDate".formatted(dto.status()));
        }
    }

    @Transactional
    public Stream<Service> getServices() {
        return databaseReader.getAllServices();
    }

    @Transactional
    public Service getServiceByExternalId(String externalId) {
        return databaseReader.getServiceWithExternalId(externalId)
                .orElse(null);
    }

    @Transactional
    public Map<String, String> createExternalService(UUID actionId, ExternalServiceWriteDto dto) throws IOException {
        logger.infof("Create external service for action %s", actionId);

        var action = actionService.getActionById(actionId);
        if (!(action instanceof AskedService)) {
            throw new IllegalArgumentException("Action %s is not an asked service".formatted(actionId));
        }

        if (((AskedService) action).getServiceExternalId() != null) {
            throw new IllegalArgumentException(
                    "Action %s is already linked to an external service : %s".formatted(actionId,
                            ((AskedService) action).getServiceExternalId()));
        }
        var serviceCode = coswinService.sendExternalService(dto);
        logger.infof("External service created with code %s", serviceCode);
        ((AskedService) action).setServiceExternalId(serviceCode);
        return Map.of("id", serviceCode);
    }
}
