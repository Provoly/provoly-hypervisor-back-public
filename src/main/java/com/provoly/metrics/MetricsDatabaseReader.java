package com.provoly.metrics;

import static java.util.stream.Collectors.groupingBy;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;

import com.provoly.DatabaseReader;
import com.provoly.equipment.*;
import com.provoly.event.*;
import com.provoly.service.Service;
import com.provoly.service.ServiceStatus;

import org.jboss.logging.Logger;

@ApplicationScoped
public class MetricsDatabaseReader extends DatabaseReader {
    public static final String UNMANAGED = "unmanaged";
    public static final String MANAGED = "managed";
    private static final long EP_ID = 1;

    private final Logger logger;
    private final EquipmentService equipmentService;

    public MetricsDatabaseReader(EntityManager em, Logger logger, EquipmentService equipmentService) {
        super(em);
        this.logger = logger;
        this.equipmentService = equipmentService;
    }

    private record QueryResult(String code, int managed, long count) {
    }

    public Collection<Equipment> getEquipmentsWithUnDoneEvents(Collection<String> entities,
            Collection<Criticality> criticalities,
            Collection<Category> categories) {

        return equipmentService
                .getEquipments(entities)
                .stream()
                .filter(equipment -> equipment.getDomain().getId() == EP_ID)
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

    public Map<String, Long> getTotalEquipmentGroupedByFamilyAndManaged() {
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
                .where(builder.equal(domain.get(Domain_.id), EP_ID))
                .groupBy(family.get(Family_.code), managed);

        var result = em.createQuery(query)
                .getResultList();

        return gatherUnmanagedEquipments(result);
    }

    public Map<String, Map<ServiceStatus, Long>> getEquipmentServicesByStatus(Collection<Equipment> equipments) {
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

    public Map<String, Long> getEquipmentsGroupByEntityAndManaged(Family family, Domain domain) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<QueryResult> criteriaQuery = builder.createQuery(QueryResult.class);
        Root<Equipment> equipment = criteriaQuery.from(Equipment.class);
        var entity = equipment.join(Equipment_.entity, JoinType.LEFT);
        var managed = getManagedPath(builder, equipment.get(Equipment_.attributes));

        var query = criteriaQuery.multiselect(
                entity.get(EquipmentEntity_.name),
                managed,
                builder.count(equipment))
                .where(builder.and(
                        builder.equal(equipment.get(Equipment_.family), family),
                        builder.equal(equipment.get(Equipment_.domain), domain)))
                .groupBy(entity.get(EquipmentEntity_.name), managed);

        return em.createQuery(query)
                .getResultStream()
                .collect(Collectors.toMap(
                        r -> "%s_%s".formatted(r.code(), r.managed() == 1 ? MANAGED : UNMANAGED),
                        QueryResult::count));

    }

    public List<AggregateServiceDto> aggregateDoneServices(DateInterval interval,
            int buckets,
            Instant date,
            Collection<Long> families,
            Collection<Long> entities) {

        return em.createNativeQuery("""
                select date_trunc(:interval, close_date, 'UTC') start, coalesce(count(*),0) from {h-schema}service
                left join {h-schema}equipment on service.equipment_id = equipment.id
                where status = 'DONE'
                and close_date < cast (:reference_date as timestamptz)
                and close_date > date_trunc(:interval, cast (:reference_date as timestamptz) - cast (:result as interval))
                and equipment.family_id in :families_id
                and equipment.equipment_entity_id in :entities_id
                group by start
                order by 1;
                """, Tuple.class)
                .setParameter("interval", interval.name())
                .setParameter("reference_date", date)
                .setParameter("result", "%s %s".formatted(buckets, interval))
                .setParameter("families_id", families)
                .setParameter("entities_id", entities)
                .getResultStream()
                .map(res -> new AggregateServiceDto(
                        Instant.parse(((Tuple) res).get(0).toString()),
                        Long.parseLong(((Tuple) res).get(1).toString())))
                .toList();
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
        return event -> event.getCategory() != Category.MANIFESTATION;
    }

    private Predicate<Event> isOneOfCriticality(Collection<Criticality> criticalities) {
        return event -> (criticalities.isEmpty()) || (criticalities.contains(event.getCriticality()));
    }

    private Predicate<Event> isOneOfCategory(Collection<Category> categories) {
        return event -> (categories.isEmpty()) || (categories.contains(event.getCategory()));
    }
}
