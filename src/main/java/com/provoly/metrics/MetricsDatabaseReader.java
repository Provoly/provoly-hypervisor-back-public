package com.provoly.metrics;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;

import com.provoly.DatabaseReader;
import com.provoly.action.Service;
import com.provoly.action.Service_;
import com.provoly.equipment.*;
import com.provoly.event.Domain;
import com.provoly.event.Domain_;
import com.provoly.event.Status;

@ApplicationScoped
public class MetricsDatabaseReader extends DatabaseReader {
    public static final String UNMANAGED = "unmanaged";
    public static final String MANAGED = "managed";

    public MetricsDatabaseReader(EntityManager em) {
        super(em);
    }

    private record QueryResult(String code, boolean managed, long count) {
    }

    private record ServiceWithEquipments(String code, Status status, Boolean managed, long count) {
    }

    public Map<String, Long> getEquipmentsWithUnDoneEvent() {
        List<Tuple> result = em.createNativeQuery("""
                select family.code as code, (attributes->>'managed')::::boolean as managed, count(distinct family.code) as count
                from {h-schema}equipment eqt
                join {h-schema}event on event.equipment_id = eqt.id
                join {h-schema}family on eqt.family_id = family.id
                where eqt.domain_id = 1 -- domain "EP"
                and exists (
                    select 1 from {h-schema}all_event evt
                    where eqt.id = evt.equipment_id
                    and evt.status != 'DONE'
                    and evt.category != 'MANIFESTATION')
                group by family.code, managed;
                """, Tuple.class)
                .getResultList();

        var queryResults = result
                .stream()
                .map(t -> new QueryResult(t.get("code").toString(), (boolean) t.get(MANAGED), (long) t.get("count")))
                .toList();
        return gatherUnmanagedEquipments(queryResults);
    }

    public Map<String, Long> getEquipmentGroupedByFamilyAndManaged() {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<QueryResult> criteriaQuery = builder.createQuery(QueryResult.class);
        Root<Equipment> equipment = criteriaQuery.from(Equipment.class);
        var family = equipment.join(Equipment_.family, JoinType.LEFT);
        var domain = equipment.join(Equipment_.domain, JoinType.LEFT);

        final Expression<Boolean> managed = getManagedPath(builder, equipment.get(Equipment_.attributes));

        var query = criteriaQuery
                .multiselect(
                        family.get(Family_.code),
                        managed,
                        builder.count(equipment))
                .where(builder.equal(domain.get(Domain_.name), "EP"))
                .groupBy(family.get(Family_.code), managed);

        var result = em.createQuery(query)
                .getResultList();

        return gatherUnmanagedEquipments(result);
    }

    public Map<String, Map<Status, Long>> getEquipmentServicesByStatus() {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<ServiceWithEquipments> criteriaQuery = builder.createQuery(ServiceWithEquipments.class);
        Root<Service> service = criteriaQuery.from(Service.class);
        var equipment = service.join(Service_.equipment, JoinType.LEFT);
        var family = equipment.join(Equipment_.family, JoinType.LEFT);
        var domain = equipment.join(Equipment_.domain, JoinType.LEFT);

        Expression<Boolean> managed = getManagedPath(builder, equipment.get(Equipment_.attributes));

        var query = criteriaQuery
                .multiselect(
                        family.get(Family_.code),
                        service.get(Service_.status),
                        managed,
                        builder.count(service))
                .where(builder.and(
                        builder.equal(domain.get(Domain_.name), "EP"),
                        service.get(Service_.status).in(List.of(Status.NEW, Status.IN_PROGRESS))))
                .groupBy(family.get(Family_.code), service.get(Service_.status), managed);

        var result = em.createQuery(query)
                .getResultList();

        var linkedServicesByStatus = getServiceCountByManagedEquipAndStatus(result);

        linkedServicesByStatus.put(UNMANAGED,
                result.stream()
                        .filter(r -> !r.managed())
                        .collect(Collectors.groupingBy(ServiceWithEquipments::status,
                                Collectors.summingLong(ServiceWithEquipments::count))));

        return linkedServicesByStatus;
    }

    private Map<String, Map<Status, Long>> getServiceCountByManagedEquipAndStatus(List<ServiceWithEquipments> result) {
        Map<String, Map<Status, Long>> linkedServicesByStatus = new HashMap<>();

        result.stream()
                .filter(ServiceWithEquipments::managed)
                .forEach(s -> {
                    if (linkedServicesByStatus.containsKey(s.code())) {
                        linkedServicesByStatus.get(s.code()).put(s.status, s.count());
                    } else {
                        linkedServicesByStatus.put(s.code(), new EnumMap<>(Map.of(s.status(), s.count())));
                    }
                });
        return linkedServicesByStatus;
    }

    public Map<String, Long> getEquipmentsGroupByEntityAndManaged(Family family, Domain domain) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<QueryResult> criteriaQuery = builder.createQuery(QueryResult.class);
        Root<Equipment> equipment = criteriaQuery.from(Equipment.class);
        var entity = equipment.join(Equipment_.entity, JoinType.LEFT);
        Expression<Boolean> managed = getManagedPath(builder, equipment.get(Equipment_.attributes));

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
                        r -> "%s_%s".formatted(r.code(), r.managed() ? MANAGED : UNMANAGED),
                        QueryResult::count));

    }

    private Map<String, Long> gatherUnmanagedEquipments(List<QueryResult> result) {
        var equipmentWithEventsTotal = result.stream()
                .filter(QueryResult::managed)
                .collect(Collectors.toMap(QueryResult::code, QueryResult::count));

        equipmentWithEventsTotal.put(UNMANAGED, result.stream()
                .filter(r -> !r.managed())
                .mapToLong(QueryResult::count)
                .sum());
        return equipmentWithEventsTotal;
    }

    private Expression<Boolean> getManagedPath(CriteriaBuilder builder, Path<Map<String, Object>> attributes) {
        return builder.function("jsonb_extract_path_text", Boolean.class,
                attributes, builder.literal(MANAGED));
    }
}
