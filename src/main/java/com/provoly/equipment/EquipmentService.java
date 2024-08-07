package com.provoly.equipment;

import java.util.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.provoly.EquipmentEnrichedProducer;

import org.jboss.logging.Logger;

@ApplicationScoped
public class EquipmentService {
    private final Logger logger;
    private final EquipmentDatabaseReader databaseReader;
    private final EquipmentMapper equipmentMapper;
    private final EquipmentEnrichedProducer equipmentEnrichedProducer;

    public EquipmentService(EquipmentDatabaseReader databaseReader, EquipmentMapper equipmentMapper, Logger logger,
            EquipmentEnrichedProducer equipmentEnrichedProducer) {
        this.databaseReader = databaseReader;
        this.equipmentMapper = equipmentMapper;
        this.logger = logger;
        this.equipmentEnrichedProducer = equipmentEnrichedProducer;
    }

    @Transactional
    public void saveOrUpdateEquipments(Collection<EquipmentWriteDto> dtos) {
        logger.infof("Save or update %s equipments", dtos.size());
        dtos.stream()
                .sorted(Comparator.comparing(EquipmentWriteDto::level))
                .forEach(dto -> databaseReader.getEquipmentWithExternalId(dto.id())
                        .ifPresentOrElse(
                                equipment -> {
                                    logger.debugf("Equipment with external id %s already exists, update it", dto.id());
                                    equipmentMapper.updateEquipment(dto, equipment);
                                    equipmentEnrichedProducer.updateFor(equipment);
                                },
                                () -> {
                                    logger.debugf("Equipment with external id %s not exists, create it", dto.id());
                                    Equipment equipment = new Equipment(UUID.randomUUID());
                                    equipmentMapper.updateEquipment(dto, equipment);
                                    databaseReader.saveEquipment(equipment);
                                    equipmentEnrichedProducer.updateFor(equipment);
                                }));
    }

    @Transactional
    public Optional<Equipment> getOptionalEquipmentById(UUID id) {
        return databaseReader.getEquipmentById(id);
    }

    @Transactional
    public Equipment getEquipmentById(UUID id) {
        return databaseReader.getEquipmentById(id)
                .orElseThrow(() -> new NoSuchElementException("Equipment with id %s not found".formatted(id)));
    }

    @Transactional
    public Equipment getEquipmentByIdOrNull(UUID id) {
        if (id == null) {
            logger.debugf("Equipment id is null, return null");
            return null;
        }
        return getOptionalEquipmentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Equipment with id %s invalid".formatted(id)));
    }

    @Transactional
    public Collection<Equipment> getEquipments(Collection<String> entities) {
        logger.debugf("Get all equipments with entities %s", entities);
        var equipmentEntities = entities.stream().map(this::getEquipmentEntity).toList();
        return databaseReader.getEquipmentsByEntities(equipmentEntities);
    }

    @Transactional
    public Equipment getEquipmentByName(String name) {
        logger.debugf("Get all equipments with name %s", name);
        return databaseReader.getEquipmentByName(name);
    }

    @Transactional
    public Collection<String> getEquipmentEntitiesName() {
        logger.debugf("Get all equipments entities names");
        return databaseReader.getEquipmentEntities()
                .stream()
                .map(EquipmentEntity::getName)
                .toList();
    }

    @Transactional
    public EquipmentEntity getEquipmentEntity(String entity) {
        logger.debugf("Get equipment entity %s", entity);
        return databaseReader.getEquipmentEntityByCode(entity)
                .orElseThrow(() -> new IllegalArgumentException("Entity code %s invalid".formatted(entity)));
    }

    @Transactional
    public EquipmentEntity getEquipmentEntityOrNull(String code) {
        logger.debugf("Get equipment entity %s", code);
        return code == null || code.isEmpty()
                ? null
                : databaseReader.getEquipmentEntityByCode(code)
                        .orElseThrow(() -> new IllegalArgumentException("Entity code %s invalid".formatted(code)));
    }

    @Transactional
    public Family getFamilyByCode(String code) {
        logger.debugf("Get equipment family by code  %s", code);
        return databaseReader.getFamilyByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Family code %s invalid".formatted(code)));
    }

    @Transactional
    public Family getFamilyByCodeOrNull(String code) {
        logger.debugf("Get equipment family by code  %s", code);
        return code == null || code.isEmpty()
                ? null
                : databaseReader.getFamilyByCode(code)
                        .orElseThrow(() -> new IllegalArgumentException("Family code %s invalid".formatted(code)));
    }

    @Transactional
    public District getDistrictByCode(String code) {
        logger.debugf("Get district by code  %s", code);
        return databaseReader.getDistrictByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("District code %s invalid".formatted(code)));
    }

    @Transactional
    public District getDistrictByCodeOrNull(String code) {
        logger.debugf("Get district by code  %s", code);
        return code == null || code.isEmpty()
                ? null
                : databaseReader.getDistrictByCode(code)
                        .orElseThrow(() -> new IllegalArgumentException("District code %s invalid".formatted(code)));
    }

    @Transactional
    public Equipment getEquipmentByIdExternalId(String source, String id) {
        return databaseReader.getEquipmentWithExternalId(Map.of(source.toUpperCase(), id))
                .orElseThrow(
                        () -> new NoSuchElementException("Equipment with source %s and id %s not found".formatted(source, id)));
    }
}
