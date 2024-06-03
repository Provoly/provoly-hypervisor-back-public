package com.provoly.equipment;

import java.util.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import com.provoly.DatabaseReader;

import org.jboss.logging.Logger;

@ApplicationScoped
public class EquipmentDatabaseReader extends DatabaseReader {
    private Logger logger;

    public EquipmentDatabaseReader(EntityManager em, Logger logger) {
        super(em);
        this.logger = logger;
    }

    public void saveEquipment(Equipment equipment) {
        em.persist(equipment);
    }

    public Optional<Equipment> getEquipmentById(UUID id) {
        return Optional.ofNullable(em.find(Equipment.class, id));
    }

    public Equipment getEquipmentByName(String name) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Equipment> criteriaQuery = builder.createQuery(Equipment.class);
        Root<Equipment> root = criteriaQuery.from(Equipment.class);

        var query = criteriaQuery.select(root)
                .where(builder.equal(root.get(Equipment_.name), name));

        return em.createQuery(query)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Equipment with name %s not found".formatted(name)));
    }

    public Collection<Equipment> getEquipmentsByEntities(List<EquipmentEntity> entities) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Equipment> criteriaQuery = builder.createQuery(Equipment.class);
        Root<Equipment> root = criteriaQuery.from(Equipment.class);

        List<Predicate> predicates = new ArrayList<>();

        if (!entities.isEmpty()) {
            logger.debugf("filter on equipment entities %s", entities);
            predicates.add(root.get(Equipment_.entity).in(entities));
        }

        var query = criteriaQuery.select(root)
                .where(builder.and(getPredicatesAsArray(predicates)));

        return em.createQuery(query)
                .getResultList();
    }

    public Collection<EquipmentEntity> getEquipmentEntities() {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<EquipmentEntity> criteriaQuery = builder.createQuery(EquipmentEntity.class);
        Root<EquipmentEntity> root = criteriaQuery.from(EquipmentEntity.class);

        return em.createQuery(criteriaQuery.select(root))
                .getResultList();
    }

    public Optional<EquipmentEntity> getEquipmentEntityByName(String name) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<EquipmentEntity> criteriaQuery = builder.createQuery(EquipmentEntity.class);
        Root<EquipmentEntity> root = criteriaQuery.from(EquipmentEntity.class);

        var query = criteriaQuery.select(root)
                .where(builder.equal(root.get(EquipmentEntity_.name), name));

        return em.createQuery(query)
                .getResultStream()
                .findFirst();
    }

    public Optional<Family> getFamilyByCode(String code) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Family> criteriaQuery = builder.createQuery(Family.class);
        Root<Family> root = criteriaQuery.from(Family.class);

        var query = criteriaQuery.select(root)
                .where(builder.equal(root.get(Family_.code), code));

        return em.createQuery(query)
                .getResultStream()
                .findFirst();
    }

    public Optional<Family> getFamilyByName(String name) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Family> criteriaQuery = builder.createQuery(Family.class);
        Root<Family> root = criteriaQuery.from(Family.class);

        var query = criteriaQuery.select(root)
                .where(builder.equal(root.get(Family_.name), name));

        return em.createQuery(query)
                .getResultStream()
                .findFirst();
    }

    public Collection<Family> getFamilies() {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Family> criteriaQuery = builder.createQuery(Family.class);
        Root<Family> root = criteriaQuery.from(Family.class);

        return em.createQuery(criteriaQuery.select(root))
                .getResultList();
    }

    public Optional<Equipment> getEquipmentWithExternalId(String id) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Equipment> criteriaQuery = builder.createQuery(Equipment.class);
        Root<Equipment> root = criteriaQuery.from(Equipment.class);

        var query = criteriaQuery.select(root)
                .where(builder.equal(root.get(Equipment_.externalId), id));

        return em.createQuery(query)
                .getResultStream()
                .findFirst();
    }

    public Optional<Equipment> getEquipmentWithCode(String code) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Equipment> criteriaQuery = builder.createQuery(Equipment.class);
        Root<Equipment> root = criteriaQuery.from(Equipment.class);

        var query = criteriaQuery.select(root)
                .where(builder.equal(root.get(Equipment_.code), code));

        return em.createQuery(query)
                .getResultStream()
                .findFirst();
    }
}
