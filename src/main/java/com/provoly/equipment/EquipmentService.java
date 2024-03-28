package com.provoly.equipment;

import java.util.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.provoly.DatabaseReader;

import org.jboss.logging.Logger;

@ApplicationScoped
public class EquipmentService {
    private DatabaseReader databaseReader;
    private Logger logger;

    public EquipmentService(DatabaseReader databaseReader, Logger logger) {
        this.databaseReader = databaseReader;
        this.logger = logger;
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
    public Collection<Equipment> getEquipments(String entity) {
        logger.debugf("Get all equipments with entity %s", entity);
        return databaseReader.getEquipments(getEquipmentEntity(entity));
    }

    @Transactional
    public Equipment getEquipmentByName(String name) {
        logger.debugf("Get all equipments with name %s", name);
        return databaseReader.getEquipmentByName(name);
    }

    @Transactional
    public List<String> getEquipmentEntitiesName() {
        logger.debugf("Get all equipments entities");
        return databaseReader.getEquipmentEntities()
                .stream()
                .map(EquipmentEntity::getName)
                .toList();
    }

    @Transactional
    public EquipmentEntity getEquipmentEntity(String entity) {
        logger.debugf("Get equipment entity %s", entity);
        return entity != null ? databaseReader.getEquipmentEntityByName(entity)
                .orElseThrow(() -> new IllegalArgumentException("Entity %s invalid".formatted(entity)))
                : null;
    }

    @Transactional
    public Family getFamiliyByCode(String code) {
        logger.debugf("Get equipment family by code  %s", code);
        return databaseReader.getFamilyByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Code %s invalid".formatted(code)));
    }
}
