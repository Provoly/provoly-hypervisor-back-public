package com.provoly.service;

import java.util.Collection;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.provoly.EquipmentEnrichedProducer;

import org.jboss.logging.Logger;

@ApplicationScoped
public class ServiceService {
    private final Logger logger;
    private final ServiceDatabaseReader databaseReader;
    private final ServiceMapper serviceMapper;
    private final EquipmentEnrichedProducer equipmentEnrichedProducer;

    public ServiceService(Logger logger, ServiceDatabaseReader databaseReader, ServiceMapper serviceMapper,
            EquipmentEnrichedProducer equipmentEnrichedProducer) {
        this.logger = logger;
        this.databaseReader = databaseReader;
        this.serviceMapper = serviceMapper;
        this.equipmentEnrichedProducer = equipmentEnrichedProducer;
    }

    @Transactional
    public void saveOrUpdateServices(Collection<ServiceWriteDto> dtos) {
        logger.infof("Save or update %s services", dtos.size());
        dtos
                .forEach(dto -> {
                    checkStartAndCloseDatePresence(dto);
                    checkCloseDatePresence(dto);
                    databaseReader.getServiceWithExternalId(dto.id())
                            .ifPresentOrElse(
                                    service -> {
                                        logger.infof("Service with external id %s already exists, update it", dto.id());

                                        var previousEquipment = service.getEquipment();

                                        serviceMapper.updateService(dto, service);
                                        equipmentEnrichedProducer.updateFor(service.getEquipment());

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
                                        Service service = new Service(UUID.randomUUID());
                                        serviceMapper.updateService(dto, service);
                                        databaseReader.saveService(service);
                                        equipmentEnrichedProducer.updateFor(service.getEquipment());
                                    });
                });

    }

    private void checkStartAndCloseDatePresence(ServiceWriteDto dto) {
        if (dto.status().getPriority() > 1 && (dto.startDate() == null || dto.endDate() == null)) {
            throw new IllegalArgumentException(
                    "Cannot save or update service with status %s without startDate or endDate"
                            .formatted(dto.status()));
        }
    }

    private void checkCloseDatePresence(ServiceWriteDto dto) {
        if (dto.status().getPriority() == 4 && dto.closeDate() == null) {
            throw new IllegalArgumentException(
                    "Cannot save or update service with status %s without closeDate".formatted(dto.status()));
        }
    }

    @Transactional
    public Collection<Service> getServices() {
        return databaseReader.getAllServices();
    }
}
