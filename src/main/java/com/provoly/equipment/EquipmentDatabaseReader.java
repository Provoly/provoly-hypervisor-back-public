package com.provoly.equipment;

import java.util.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;

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

    public Collection<Equipment> getEquipments(List<EquipmentEntity> entities, List<Family> families, String search, int page,
            int pageSize) {
        var cb = em.getCriteriaBuilder();
        CriteriaQuery<Equipment> criteriaQuery = cb.createQuery(Equipment.class);
        Root<Equipment> root = criteriaQuery.from(Equipment.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isFalse(root.get(Equipment_.deleted)));

        if (!entities.isEmpty()) {
            logger.debugf("filter on equipment entities %s", entities);
            predicates.add(root.get(Equipment_.entity).in(entities));
        }
        if (!families.isEmpty()) {
            logger.debugf("filter on equipment families %s", families);
            predicates.add(root.get(Equipment_.family).in(families));
        }

        if (search != null) {
            logger.debugf("filter on equipment that contains '%s' in code or family name".formatted(search));
            var family = root.join(Equipment_.family, JoinType.LEFT);
            search = stripAccentAndAddPercents(search);

            var filters = cb.or(
                    cb.like(unaccent(cb, root.get(Equipment_.name)), search),
                    cb.like(unaccent(cb, family.get(Family_.name)), search));
            predicates.add(filters);
        }

        var query = criteriaQuery.select(root)
                .where(cb.and(getPredicatesAsArray(predicates)));

        return em.createQuery(query)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public Optional<EquipmentEntity> getEquipmentEntityByCode(String name) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<EquipmentEntity> criteriaQuery = builder.createQuery(EquipmentEntity.class);
        Root<EquipmentEntity> root = criteriaQuery.from(EquipmentEntity.class);

        var query = criteriaQuery.select(root)
                .where(builder.equal(root.get(EquipmentEntity_.code), name));

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

    public Optional<City> getCityByCode(String code) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<City> criteriaQuery = builder.createQuery(City.class);
        Root<City> root = criteriaQuery.from(City.class);

        var query = criteriaQuery.select(root)
                .where(builder.equal(root.get(City_.code), code));

        return em.createQuery(query)
                .getResultStream()
                .findFirst();
    }

    public Optional<District> getDistrictByCode(String code) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<District> criteriaQuery = builder.createQuery(District.class);
        Root<District> root = criteriaQuery.from(District.class);

        var query = criteriaQuery.select(root)
                .where(builder.equal(root.get(District_.code), code));

        return em.createQuery(query)
                .getResultStream()
                .findFirst();
    }

    public Optional<Equipment> getEquipmentWithExternalId(Map<String, String> id) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Equipment> criteriaQuery = builder.createQuery(Equipment.class);
        Root<Equipment> root = criteriaQuery.from(Equipment.class);
        MapJoin<Equipment, String, String> externalId = root.joinMap("externalId");

        var predicate = id.entrySet().stream()
                .map(item -> builder.and(
                        builder.equal(externalId.key(), item.getKey()),
                        builder.equal(externalId.value(), item.getValue())))
                .toList();

        var query = criteriaQuery.select(root)
                .where(builder.or(getPredicatesAsArray(predicate)));

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
