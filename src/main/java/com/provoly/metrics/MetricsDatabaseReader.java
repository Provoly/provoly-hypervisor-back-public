package com.provoly.metrics;

import static java.util.stream.Collectors.groupingBy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
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
    public static final String UNMANAGED = "unmanaged";
    public static final String MANAGED = "managed";

    private final Logger logger;
    private final EquipmentService equipmentService;

    public MetricsDatabaseReader(EntityManager em, Logger logger, EquipmentService equipmentService) {
        super(em);
        this.logger = logger;
        this.equipmentService = equipmentService;
    }

    private record QueryResult(String code, int managed, long count) {
    }

    public Collection<Equipment> getEquipmentsWithUnDoneEvents(
            Long domainId,
            Collection<String> entities,
            Collection<Criticality> criticalities,
            Collection<Category> categories,
            Collection<District> districts) {

        return equipmentService
                .getEquipments(entities)
                .stream()
                .filter(equipment -> equipment.getDomain().getId().equals(domainId))
                .filter(equipment -> districts.isEmpty() || districts.contains(equipment.getDistrict()))
                .filter(equipment -> matchEvents(equipment.getEvents(), criticalities, categories))
                .toList();

    }

    private boolean matchEvents(Collection<Event> events, Collection<Criticality> criticalities,
            Collection<Category> categories) {
        return events
                .stream()
                .anyMatch(event -> isUnDone()
                        .and(isNotManifestation())
                        .and(isOneOfCategory(categories))
                        .and(isOneOfCriticality(criticalities)).test(event));
    }

    public Map<String, Long> equipmentsCountGroupedByManaged(Collection<Equipment> equipments) {
        var groupedByManagedAndCode = equipments
                .stream()
                .collect(groupingBy(equipment -> equipment.getAttributes().get(MANAGED),
                        groupingBy(equipment -> equipment.getFamily().getCode(), Collectors.counting())));

        var result = groupedByManagedAndCode.getOrDefault(1, new HashMap<>());
        long mergedUnmanagedEquipments = groupedByManagedAndCode.getOrDefault(0, Map.of()).values().stream()
                .mapToLong(v -> v).sum();
        result.put(UNMANAGED, mergedUnmanagedEquipments);
        return result;
    }

    public Map<String, Long> getTotalEpEquipmentGroupedByFamilyAndManaged() {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<QueryResult> criteriaQuery = builder.createQuery(QueryResult.class);
        Root<Equipment> equipment = criteriaQuery.from(Equipment.class);
        var family = equipment.join(Equipment_.family, JoinType.LEFT);
        var domain = equipment.join(Equipment_.domain, JoinType.LEFT);
        var managed = getManagedPath(builder, equipment.get(Equipment_.attributes));

        var query = criteriaQuery
                .multiselect(
                        family.get(Family_.code),
                        managed,
                        builder.count(equipment))
                .where(builder.equal(domain.get(Domain_.code), "EP"))
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
                .where(builder.equal(domain.get(Domain_.code), "VP"));

        return em.createQuery(query).getSingleResult();
    }

    public Map<String, Map<ServiceStatus, Long>> getEpEquipmentServicesByStatus(Collection<Equipment> equipments) {
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

    public Map<ServiceStatus, Long> getVpEquipmentServicesByStatus(Collection<Equipment> equipments) {
        return equipments.stream()
                .map(Equipment::getServices)
                .flatMap(Collection::stream)
                .filter(service -> service.getStatus() == ServiceStatus.ASKED
                        || service.getStatus() == ServiceStatus.IN_PROGRESS)
                .collect(groupingBy(Service::getStatus, Collectors.counting()));
    }

    public Map<String, Long> getEquipmentsGroupByEntityAndManaged(Family family, Domain domain) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<QueryResult> criteriaQuery = builder.createQuery(QueryResult.class);
        Root<Equipment> equipment = criteriaQuery.from(Equipment.class);
        var entity = equipment.join(Equipment_.entity, JoinType.LEFT);
        var managed = getManagedPath(builder, equipment.get(Equipment_.attributes));

        var query = criteriaQuery.multiselect(
                entity.get(EquipmentEntity_.code),
                managed,
                builder.count(equipment))
                .where(builder.and(
                        builder.isFalse(equipment.get(Equipment_.deleted)),
                        builder.equal(equipment.get(Equipment_.family), family),
                        builder.equal(equipment.get(Equipment_.domain), domain)))
                .groupBy(entity.get(EquipmentEntity_.code), managed);

        return em.createQuery(query)
                .getResultStream()
                .collect(Collectors.toMap(
                        r -> "%s_%s".formatted(r.code(), r.managed() == 1 ? MANAGED : UNMANAGED),
                        QueryResult::count));

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
                                and (:families_id is null or equipment.family_id in :families_id)
                                and (:entities_id is null or equipment.equipment_entity_id in :entities_id)
                                and (:districts_id is null or equipment.district_id in :districts_id )
                                and (service.domain_id = :domain_id or :domain_id is null)
                                group by start
                                order by 1;
                                """,
                        Tuple.class)
                .setParameter("category", 2) // Curative cateogory
                .setParameter("interval", interval.name())
                .setParameter("reference_date", date)
                .setParameter("interval_number", "%s %s".formatted(buckets, interval))
                .setParameter("families_id", families)
                .setParameter("entities_id", entities)
                .setParameter("districts_id", districts)
                .setParameter("domain_id", new TypedParameterValue(StandardBasicTypes.LONG, domainId))
                .getResultStream()
                .map(res -> new AggregateServiceDto(
                        Instant.parse(((Tuple) res).get(0).toString()),
                        Long.parseLong(((Tuple) res).get(1).toString())))
                .toList();
    }

    @Transactional
    public Map<String, Long> getAnomalyEventsGroupedBySubCategories(Domain domain,
            Category anomalyCategory,
            Instant date,
            Status status) {

        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = builder.createQuery(Tuple.class);
        Root<Event> event = criteriaQuery.from(Event.class);
        var equipment = event.join(Event_.equipment, JoinType.LEFT);
        var category = event.join(Event_.category, JoinType.LEFT);
        var subcategory = getSubCategories(anomalyCategory).toList();

        var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();

        if (domain != null) {
            logger.debugf("filter on domain %s", domain);
            predicates.add(builder.equal(equipment.get(Equipment_.domain), domain));
        }

        if (date != null) {
            logger.debugf("filter on creation date %s", date);
            predicates.add(builder.between(event.get(Event_.creationDate),
                    date,
                    date.plus(1, ChronoUnit.DAYS)));
            predicates.add(builder.notEqual(event.get(Event_.status), Status.DONE));
        }

        if (status != null) {
            logger.debugf("filter on status %s", status);
            predicates.add(builder.equal(event.get(Event_.status), status));
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
                .collect(Collectors.toMap(EnumEntity::getCode, v -> res.getOrDefault(v.getCode(), 0L)));
    }

    @Transactional
    public Collection<AnomalyQueryResult> getAnomalyEventsGroupedBySubCategoriesAndEntities(Domain domain,
            Category anomalyCategory,
            Instant startDate) {

        var builder = em.getCriteriaBuilder();
        CriteriaQuery<AnomalyQueryResult> criteriaQuery = builder.createQuery(AnomalyQueryResult.class);
        Root<Event> event = criteriaQuery.from(Event.class);
        var equipment = event.join(Event_.equipment, JoinType.LEFT);
        var equipmentEntity = equipment.join(Equipment_.entity, JoinType.LEFT);
        var category = event.join(Event_.category, JoinType.LEFT);
        var subcategory = getSubCategories(anomalyCategory).toList();
        var subCode = subcategory.stream().map(EnumEntity::getCode).toList();

        var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();

        if (domain != null) {
            logger.debugf("filter on domain %s", domain);
            predicates.add(builder.equal(equipment.get(Equipment_.domain), domain));
        }

        if (startDate != null) {
            logger.debugf("from creation date %s", startDate);
            predicates.add(builder.greaterThanOrEqualTo(event.get(Event_.creationDate), startDate));
            predicates.add(builder.notEqual(event.get(Event_.status), Status.DONE));
        }

        var query = criteriaQuery.multiselect(
                equipmentEntity.get(EquipmentEntity_.code),
                category.get(Category_.code),
                builder.count(event))
                .where(builder.and(getPredicatesAsArray(predicates)))
                .groupBy(equipmentEntity.get(EquipmentEntity_.code), category.get(Category_.code));

        var results = em.createQuery(query)
                .getResultList();

        completeAnomaliesCountWithZeros(results, subCode);
        return results;
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

    private Map<String, Long> gatherUnmanagedEquipments(List<QueryResult> result) {
        var equipmentWithEventsTotal = result.stream()
                .filter(queryResult -> queryResult.managed == 1)
                .collect(Collectors.toMap(QueryResult::code, QueryResult::count));

        equipmentWithEventsTotal.put(UNMANAGED, result.stream()
                .filter(queryResult -> queryResult.managed == 0)
                .mapToLong(QueryResult::count)
                .sum());
        return equipmentWithEventsTotal;
    }

    private Expression<Integer> getManagedPath(CriteriaBuilder builder, Path<Map<String, Object>> attributes) {
        return builder.function("jsonb_extract_path_text", Integer.class,
                attributes, builder.literal(MANAGED));
    }

    private Predicate<Event> isUnDone() {
        return event -> event.getStatus() != Status.DONE;
    }

    private Predicate<Event> isNotManifestation() {
        return event -> !event.getCategory().getCode().equals("MANIFESTATION");
    }

    private Predicate<Event> isOneOfCriticality(Collection<Criticality> criticalities) {
        return event -> (criticalities.isEmpty()) || (criticalities.contains(event.getCriticality()));
    }

    private Predicate<Event> isOneOfCategory(Collection<Category> categories) {
        return event -> (categories.isEmpty()) || (categories.contains(event.getCategory()));
    }

    public Stream<Category> getSubCategories(Category category) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Category> criteriaQuery = builder.createQuery(Category.class);
        Root<Category> root = criteriaQuery.from(Category.class);

        var query = criteriaQuery.select(root)
                .where(builder.equal(root.get(Category_.parent), category));

        return em.createQuery(query)
                .getResultStream();
    }

}
