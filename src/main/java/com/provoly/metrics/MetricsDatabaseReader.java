package com.provoly.metrics;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.transaction.Transactional;

import com.provoly.DatabaseReader;
import com.provoly.EnumEntity;
import com.provoly.equipment.*;
import com.provoly.event.*;
import com.provoly.service.Service;
import com.provoly.service.ServiceStatus;

import org.hibernate.query.TypedParameterValue;
import org.hibernate.type.StandardBasicTypes;
import org.jboss.logging.Logger;

@ApplicationScoped
public class MetricsDatabaseReader extends DatabaseReader {
    public static final String ANOMALY_CATEGORY = "ANOMALY";
    public static final String UNMANAGED = "unmanaged";
    public static final String MANAGED = "managed";

    private final Logger logger;

    public MetricsDatabaseReader(EntityManager em, Logger logger) {
        super(em);
        this.logger = logger;
    }

    private record ManagedResult(String code, int managed, long count) {
    }

    private record EquipmentResult(Category category, Equipment equipment) {
    }

    public Map<String, List<Equipment>> getEquipmentsByEventCategory(
            Domain domainEntity,
            Collection<EquipmentEntity> entities,
            Collection<String> criticalities,
            Collection<Category> categories,
            Collection<District> districts) {

        logger.debug("Get equipment linked with an undone event grouped by category");

        var builder = em.getCriteriaBuilder();
        CriteriaQuery<EquipmentResult> criteriaQuery = builder.createQuery(EquipmentResult.class);
        Root<Event> event = criteriaQuery.from(Event.class);
        var category = event.join(Event_.category, JoinType.LEFT);
        var equipment = event.join(Event_.equipment, JoinType.LEFT);

        var predicates = new ArrayList<Predicate>();
        predicates.add(builder.notEqual(event.get(Event_.status), Status.DONE));
        predicates.add(builder.isFalse(equipment.get(Equipment_.deleted)));

        filterOnDomain(domainEntity, predicates, builder, equipment);
        filterOnCriticality(criticalities, predicates, event);
        filterOn(entities, predicates, builder, equipment, Equipment_.entity);
        filterOn(districts, predicates, builder, equipment, Equipment_.district);

        if (!categories.isEmpty()) {
            logger.debugf("filter on categories %s", categories);
            if (categories.stream().noneMatch(Objects::nonNull)) {
                logger.debugf("filter on null  %s");
                predicates.add(builder.isNull(event.get(Event_.category)));
            } else {
                var subcategory = categories
                        .stream()
                        .map(c -> getSubCategories(c).toList())
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
                subcategory.addAll(categories);
                logger.debugf("filter on  %s", subcategory);
                predicates.add(event.get(Event_.category).in(subcategory));
            }
        }

        var query = criteriaQuery
                .multiselect(
                        category,
                        equipment)
                .distinct(true)
                .where(getPredicatesAsArray(predicates));

        return em.createQuery(query)
                .getResultStream()
                .collect(groupingBy(this::getCategoryCode, mapping(EquipmentResult::equipment, Collectors.toList())));
    }

    public Map<String, Long> getEpEquipmentByFamily(Collection<Equipment> equipments) {
        var groupedByManagedAndCode = equipments
                .stream()
                .collect(groupingBy(equipment -> equipment.getAttributes().get(MANAGED),
                        groupingBy(equipment -> equipment.getFamily().getCode(), Collectors.counting())));

        var result = groupedByManagedAndCode.getOrDefault(1, new HashMap<>());

        logger.debugf("merge unmanaged equipments");
        long mergedUnmanagedEquipments = groupedByManagedAndCode.getOrDefault(0, Map.of())
                .values()
                .stream()
                .mapToLong(v -> v)
                .sum();

        result.put(UNMANAGED, mergedUnmanagedEquipments);
        return result;
    }

    public Map<String, Long> getEpEquipmentsByEntity(Family family, Domain domain) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<ManagedResult> criteriaQuery = builder.createQuery(ManagedResult.class);
        Root<Equipment> equipment = criteriaQuery.from(Equipment.class);
        var entity = equipment.join(Equipment_.entity, JoinType.LEFT);
        var managed = getManagedPath(builder, equipment.get(Equipment_.attributes));

        var familyPredicate = family == null ? builder.isNull(equipment.get(Equipment_.family))
                : builder.equal(equipment.get(Equipment_.family), family);

        var query = criteriaQuery.multiselect(
                entity.get(EquipmentEntity_.code),
                managed,
                builder.count(equipment))
                .where(builder.and(
                        builder.isFalse(equipment.get(Equipment_.deleted)),
                        familyPredicate,
                        builder.isFalse(equipment.get(Equipment_.deleted)),
                        builder.equal(equipment.get(Equipment_.domain), domain)))
                .groupBy(entity.get(EquipmentEntity_.code), managed);

        return em.createQuery(query)
                .getResultStream()
                .collect(Collectors.toMap(
                        r -> "%s_%s".formatted(r.code(), r.managed() == 1 ? MANAGED : UNMANAGED),
                        ManagedResult::count));

    }

    public Map<String, Long> getTotalEpEquipmentByFamily() {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<ManagedResult> criteriaQuery = builder.createQuery(ManagedResult.class);
        Root<Equipment> equipment = criteriaQuery.from(Equipment.class);
        var family = equipment.join(Equipment_.family, JoinType.LEFT);
        var domain = equipment.join(Equipment_.domain, JoinType.LEFT);
        var managed = getManagedPath(builder, equipment.get(Equipment_.attributes));

        var query = criteriaQuery
                .multiselect(
                        family.get(Family_.code),
                        managed,
                        builder.count(equipment))
                .where(builder.and(
                        builder.equal(domain.get(Domain_.code), "EP"),
                        builder.isFalse(equipment.get(Equipment_.deleted))))
                .groupBy(family.get(Family_.code), managed);

        var result = em.createQuery(query)
                .getResultList();

        return gatherUnmanagedEquipments(result);
    }

    public Long getTotalVpEquipments() {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
        Root<Equipment> equipment = criteriaQuery.from(Equipment.class);
        var domain = equipment.join(Equipment_.domain, JoinType.LEFT);

        var query = criteriaQuery
                .select(builder.count(equipment))
                .where(builder.and(
                        builder.equal(domain.get(Domain_.code), "VP"),
                        builder.isFalse(equipment.get(Equipment_.deleted))));

        return em.createQuery(query).getSingleResult();
    }

    public Map<String, Map<ServiceStatus, Long>> getEpServicesByStatus(Collection<Equipment> equipments) {
        var equipmentServices = equipments.stream()
                .map(Equipment::getServices)
                .flatMap(Collection::stream)
                .filter(service -> service.getStatus() == ServiceStatus.ASKED
                        || service.getStatus() == ServiceStatus.IN_PROGRESS)
                .collect(groupingBy(service -> service.getEquipment().getAttributes().get(MANAGED),
                        groupingBy(service -> service.getEquipment().getFamily().getCode(),
                                groupingBy(Service::getStatus, Collectors.counting()))));

        var result = equipmentServices.getOrDefault(1, new HashMap<>());

        var mergedUnmanagedEquipments = equipmentServices.getOrDefault(0, Map.of()).values()
                .stream()
                .flatMap(m -> m.entrySet().stream())
                .collect(groupingBy(Map.Entry::getKey, Collectors.summingLong(Map.Entry::getValue)));

        result.put(UNMANAGED, mergedUnmanagedEquipments);
        return result;
    }

    public Map<ServiceStatus, Long> getVpServicesByStatus(Collection<Equipment> equipments) {
        return equipments.stream()
                .map(Equipment::getServices)
                .flatMap(Collection::stream)
                .filter(service -> service.getStatus() == ServiceStatus.ASKED
                        || service.getStatus() == ServiceStatus.IN_PROGRESS)
                .collect(groupingBy(Service::getStatus, Collectors.counting()));
    }

    public Collection<EventsByEquipment> getEventsByEquipments(Domain domainEntity,
            Category eventCategory,
            int limit,
            Instant date, Collection<District> districts,
            Collection<EquipmentEntity> entities,
            Collection<String> criticalities,
            Collection<Family> families) {

        var builder = em.getCriteriaBuilder();
        CriteriaQuery<EventsByEquipment> criteriaQuery = builder.createQuery(EventsByEquipment.class);
        Root<Event> event = criteriaQuery.from(Event.class);
        var equipment = event.join(Event_.equipment, JoinType.LEFT);
        var city = equipment.join(Equipment_.city, JoinType.LEFT);

        var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
        predicates.add(builder.isFalse(equipment.get(Equipment_.deleted)));

        if (eventCategory != null) {
            var subcategory = getSubCategories(eventCategory).toList();
            logger.debugf("filter on categories %s", subcategory);
            predicates.add(event.get(Event_.category).in(subcategory));
        }

        filterOnDomain(domainEntity, predicates, builder, equipment);
        filterOn(entities, predicates, builder, equipment, Equipment_.entity);
        filterOn(districts, predicates, builder, equipment, Equipment_.district);
        filterOn(families, predicates, builder, equipment, Equipment_.family);
        filterOnCriticality(criticalities, predicates, event);

        if (date != null) {
            logger.debugf("Creation date is greater than %s", date);
            predicates.add(builder.greaterThanOrEqualTo(event.get(Event_.creationDate), date));
        }

        var query = criteriaQuery
                .multiselect(
                        equipment.get(Equipment_.code),
                        equipment.get(Equipment_.address),
                        city.get(City_.code),
                        builder.count(event))
                .where(builder.and(getPredicatesAsArray(predicates)))
                .groupBy(equipment.get(Equipment_.code),
                        equipment.get(Equipment_.address),
                        city.get(City_.code))
                .orderBy(builder.desc(builder.count(event)));

        return em.createQuery(query)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<AggregateServiceDto> aggregateDoneServices(DateInterval interval,
            int buckets,
            Instant date,
            Long domainId,
            Collection<Long> families,
            Collection<Long> entities,
            Collection<Long> districts) {

        return em
                .createNativeQuery(
                        """
                                select date_trunc(:interval, close_date, 'UTC') start, count(*) from {h-schema}service
                                left join {h-schema}equipment on service.equipment_id = equipment.id
                                where status = 'DONE'
                                and category_id = :category
                                and close_date < cast (:reference_date as timestamptz)
                                and close_date > date_trunc(:interval, cast (:reference_date as timestamptz) - cast (:interval_number as interval))
                                and (:no_family_list or equipment.family_id in :families_id or (:is_family_null and equipment.family_id is null))
                                and (:no_entity_list or equipment.equipment_entity_id in :entities_id or (:is_entity_null and equipment.equipment_entity_id is null))
                                and (:no_district_list or equipment.district_id in :districts_id or (:is_district_null and equipment.district_id is null) )
                                and (service.domain_id = :domain_id or :domain_id is null)
                                group by start
                                order by 1;
                                """,
                        Tuple.class)
                .setParameter("category", 2) // Curative cateogory
                .setParameter("interval", interval.name())
                .setParameter("reference_date", date)
                .setParameter("interval_number", "%s %s".formatted(buckets, interval))
                .setParameter("entities_id", entities.stream().noneMatch(Objects::nonNull) ? List.of() : entities)
                .setParameter("families_id", families.stream().noneMatch(Objects::nonNull) ? List.of() : families)
                .setParameter("districts_id", districts.stream().noneMatch(Objects::nonNull) ? List.of() : districts)
                .setParameter("is_family_null", families.stream().noneMatch(Objects::nonNull))
                .setParameter("no_family_list", families.isEmpty())
                .setParameter("is_entity_null", entities.stream().noneMatch(Objects::nonNull))
                .setParameter("no_entity_list", entities.isEmpty())
                .setParameter("is_district_null", districts.stream().noneMatch(Objects::nonNull))
                .setParameter("no_district_list", districts.isEmpty())
                .setParameter("domain_id", new TypedParameterValue(StandardBasicTypes.LONG, domainId))
                .getResultStream()
                .map(res -> new AggregateServiceDto(
                        Instant.parse(((Tuple) res).get(0).toString()),
                        Long.parseLong(((Tuple) res).get(1).toString())))
                .toList();
    }

    @Transactional
    public Map<String, Long> getAnomalyEventsBySubCategories(Domain domain,
            Instant date,
            Status status,
            List<EquipmentEntity> entities,
            List<District> districts,
            List<String> criticalities,
            List<Family> families,
            String equipmentName) {

        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = builder.createQuery(Tuple.class);
        Root<Event> event = criteriaQuery.from(Event.class);
        var equipment = event.join(Event_.equipment, JoinType.LEFT);
        var category = event.join(Event_.category, JoinType.LEFT);
        var subcategory = getAnomalySubCategoriesCode();

        var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
        predicates.add(builder.isFalse(equipment.get(Equipment_.deleted)));

        filterOnDomain(domain, predicates, builder, equipment);
        filterOn(entities, predicates, builder, equipment, Equipment_.entity);
        filterOn(districts, predicates, builder, equipment, Equipment_.district);
        filterOn(families, predicates, builder, equipment, Equipment_.family);
        filterOnCriticality(criticalities, predicates, event);

        if (date != null) {
            logger.debugf("filter on creation date %s", date);
            predicates.add(builder.greaterThan(event.get(Event_.creationDate), date));
            predicates.add(builder.notEqual(event.get(Event_.status), Status.DONE));
        }

        if (status != null) {
            logger.debugf("filter on status %s", status);
            predicates.add(builder.equal(event.get(Event_.status), status));
        }

        if (equipmentName != null) {
            logger.debugf("filter on equipment name  %s", equipmentName);
            predicates.add(builder.equal(equipment.get(Equipment_.name), equipmentName));
        }

        var query = criteriaQuery.multiselect(
                category.get(Category_.code),
                builder.count(event))
                .where(builder.and(getPredicatesAsArray(predicates)))
                .groupBy(category.get(Category_.code));

        var res = em.createQuery(query)
                .getResultStream()
                .collect(Collectors.toMap(k -> k.get(0).toString(), k -> Long.parseLong(k.get(1).toString())));

        return subcategory
                .stream()
                .collect(Collectors.toMap(code -> code, code -> res.getOrDefault(code, 0L)));
    }

    @Transactional
    public Collection<AnomalyQueryResult> getAnomalyEventsBySubCategoriesAndEntities(Domain domain,
            Instant startDate) {
        var subcategoryCodes = getAnomalySubCategoriesCode();

        var builder = em.getCriteriaBuilder();
        CriteriaQuery<AnomalyQueryResult> criteriaQuery = builder.createQuery(AnomalyQueryResult.class);
        Root<Event> event = criteriaQuery.from(Event.class);
        var equipment = event.join(Event_.equipment, JoinType.LEFT);
        var equipmentEntity = equipment.join(Equipment_.entity, JoinType.LEFT);
        var category = event.join(Event_.category, JoinType.LEFT);

        var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
        predicates.add(category.get(Category_.code).in(subcategoryCodes));
        predicates.add(builder.isFalse(equipment.get(Equipment_.deleted)));

        filterOnDomain(domain, predicates, builder, equipment);

        if (startDate != null) {
            logger.debugf("from creation date %s", startDate);
            predicates.add(builder.greaterThanOrEqualTo(event.get(Event_.creationDate), startDate));
        }

        var query = criteriaQuery.multiselect(
                equipmentEntity.get(EquipmentEntity_.code),
                category.get(Category_.code),
                builder.count(event))
                .where(builder.and(getPredicatesAsArray(predicates)))
                .groupBy(equipmentEntity.get(EquipmentEntity_.code), category.get(Category_.code));

        var results = em.createQuery(query)
                .getResultList();

        completeAnomaliesCountWithZeros(results, subcategoryCodes);
        return results;
    }

    public Collection<AggregateServiceDto> aggregateAnomaliesEvents(DateInterval interval,
            int buckets,
            Instant startDate,
            Long domainId,
            Collection<Long> districts,
            Collection<Long> entities,
            Collection<String> criticalities,
            Collection<Long> families) {
        return em
                .createNativeQuery(
                        """
                                select date_trunc(:interval, creation_date, 'UTC') start, category.code, count(*) from {h-schema}event
                                left join {h-schema}equipment on event.equipment_id = equipment.id
                                left join {h-schema}category on event.category_id = category.id
                                where category.code in :categories
                                and creation_date < cast (:reference_date as timestamptz)
                                and creation_date > date_trunc(:interval, cast (:reference_date as timestamptz) - cast (:interval_number as interval))
                                and (equipment.domain_id = :domain_id or :domain_id is null)
                                and (:no_family_list or equipment.family_id in :families_id or (:is_family_null and equipment.family_id is null))
                                and (:no_entity_list or equipment.equipment_entity_id in :entities_id or (:is_entity_null and equipment.equipment_entity_id is null))
                                and (:no_district_list or equipment.district_id in :districts_id or (:is_district_null and equipment.district_id is null) )
                                and (:criticalities is null or event.criticality in :criticalities )
                                group by start, category.code
                                order by 1;
                                """,
                        Tuple.class)
                .setParameter("categories", getAnomalySubCategoriesCode())
                .setParameter("interval", interval.name())
                .setParameter("reference_date", startDate)
                .setParameter("interval_number", "%s %s".formatted(buckets, interval))
                .setParameter("domain_id", new TypedParameterValue(StandardBasicTypes.LONG, domainId))
                .setParameter("entities_id", entities.stream().noneMatch(Objects::nonNull) ? List.of() : entities)
                .setParameter("families_id", families.stream().noneMatch(Objects::nonNull) ? List.of() : families)
                .setParameter("districts_id", districts.stream().noneMatch(Objects::nonNull) ? List.of() : districts)
                .setParameter("is_family_null", families.stream().noneMatch(Objects::nonNull))
                .setParameter("no_family_list", families.isEmpty())
                .setParameter("is_entity_null", entities.stream().noneMatch(Objects::nonNull))
                .setParameter("no_entity_list", entities.isEmpty())
                .setParameter("is_district_null", districts.stream().noneMatch(Objects::nonNull))
                .setParameter("no_district_list", districts.isEmpty())
                .setParameter("criticalities", criticalities)
                .getResultStream()
                .map(res -> new AggregateAnomalyDto(
                        Instant.parse(((Tuple) res).get(0).toString()),
                        ((Tuple) res).get(1).toString(),
                        Long.parseLong(((Tuple) res).get(2).toString())))
                .toList();
    }

    private String getCategoryCode(EquipmentResult r) {
        return r.category().getParent() != null
                ? r.category().getParent().getCode()
                : r.category().getCode();
    }

    private List<String> getAnomalySubCategoriesCode() {
        return getSubCategories(getCategoryByCode(ANOMALY_CATEGORY))
                .map(EnumEntity::getCode)
                .toList();
    }

    private Stream<Category> getSubCategories(Category category) {
        logger.debugf("Get sub categories for %s", category);

        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Category> criteriaQuery = builder.createQuery(Category.class);
        Root<Category> root = criteriaQuery.from(Category.class);

        var query = criteriaQuery.select(root)
                .where(builder.equal(root.get(Category_.parent), category));

        return em.createQuery(query)
                .getResultStream();
    }

    private void filterOnDomain(Domain domain, ArrayList<jakarta.persistence.criteria.Predicate> predicates,
            CriteriaBuilder builder, Join<Event, Equipment> equipment) {
        if (domain != null) {
            logger.debugf("filter on domain %s", domain);
            predicates.add(builder.equal(equipment.get(Equipment_.domain), domain));
        }
    }

    private void filterOnCriticality(Collection<String> criticalities, ArrayList<Predicate> predicates, Root<Event> event) {
        if (!criticalities.isEmpty()) {
            logger.debugf("filter on event criticalities  %s", criticalities);
            predicates.add(event.get(Event_.criticality).in(criticalities));
        }
    }

    private void filterOn(Collection<? extends EnumEntity> entities,
            ArrayList<Predicate> predicates,
            CriteriaBuilder builder,
            Join<Event, Equipment> equipment,
            SingularAttribute<Equipment, ? extends EnumEntity> attribute) {
        if (!entities.isEmpty()) {
            if (entities.stream().noneMatch(Objects::nonNull)) {
                logger.debugf("filter on null  %s");
                predicates.add(builder.isNull(equipment.get(attribute)));
            } else {
                logger.debugf("filter on  %s", entities);
                predicates.add(equipment.get(attribute).in(entities));
            }
        }
    }

    private void completeAnomaliesCountWithZeros(List<AnomalyQueryResult> results, List<String> subCode) {
        getEquipmentEntities().stream().map(EnumEntity::getCode).forEach(
                entity -> {
                    if (results.stream()
                            .noneMatch(r -> r.entity().equals(entity))) {
                        subCode.forEach(sc -> results.add(new AnomalyQueryResult(entity, sc, 0L)));
                    } else {
                        subCode.forEach(sc -> {
                            if (results.stream()
                                    .filter(r -> r.entity().equals(entity))
                                    .noneMatch(r -> r.subCategory().equals(sc))) {
                                results.add(new AnomalyQueryResult(entity, sc, 0L));
                            }
                        });
                    }
                });
    }

    private Map<String, Long> gatherUnmanagedEquipments(List<ManagedResult> result) {
        logger.debug("Gathering unmanaged equipments");
        var equipmentWithEventsTotal = result.stream()
                .filter(managedResult -> managedResult.managed == 1)
                .collect(Collectors.toMap(ManagedResult::code, ManagedResult::count));

        equipmentWithEventsTotal.put(UNMANAGED, result.stream()
                .filter(managedResult -> managedResult.managed == 0)
                .mapToLong(ManagedResult::count)
                .sum());
        return equipmentWithEventsTotal;
    }

    private Expression<Integer> getManagedPath(CriteriaBuilder builder, Path<Map<String, Object>> attributes) {
        return builder.function("jsonb_extract_path_text", Integer.class,
                attributes, builder.literal(MANAGED));
    }

}
