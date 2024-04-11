package com.provoly.metrics;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

import com.provoly.DatabaseReader;
import com.provoly.action.Service;
import com.provoly.action.Service_;
import com.provoly.equipment.Equipment;
import com.provoly.equipment.Equipment_;
import com.provoly.equipment.Family_;
import com.provoly.event.Domain_;
import com.provoly.event.Status;

@ApplicationScoped
public class MetricsDatabaseReader extends DatabaseReader {
    public static final String UNMANAGED = "unmanaged";
    public static final String MANAGED = "managed";

    public MetricsDatabaseReader(EntityManager em) {
        super(em);
    }

    private record EquipmentWithEvents(String code, Boolean managed, long count) {
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

        var tupleToEquipmentWithEvents = result.stream()
                .map(t -> new EquipmentWithEvents(t.get("code").toString(), (boolean) t.get(MANAGED), (long) t.get("count")))
                .toList();
        return separateUnmanagedFromManagedEquipments(tupleToEquipmentWithEvents);
    }

    public Map<String, Long> getEquipmentGroupedByFamilyAndManaged() {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<EquipmentWithEvents> criteriaQuery = builder.createQuery(EquipmentWithEvents.class);
        Root<Equipment> root = criteriaQuery.from(Equipment.class);
        var family = root.join(Equipment_.family, JoinType.LEFT);
        var domain = root.join(Equipment_.domain, JoinType.LEFT);

        final Expression<Boolean> managed = builder.function("jsonb_extract_path_text", Boolean.class,
                root.get(Equipment_.attributes), builder.literal(MANAGED));

        var query = criteriaQuery
                .multiselect(
                        family.get(Family_.code),
                        managed,
                        builder.count(root))
                .where(builder.equal(domain.get(Domain_.name), "EP"))
                .groupBy(family.get(Family_.code), managed);

        var result = em.createQuery(query)
                .getResultList();

        return separateUnmanagedFromManagedEquipments(result);
    }

    public Map<String, Map<Status, Long>> getEquipmentServicesByStatus() {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<ServiceWithEquipments> criteriaQuery = builder.createQuery(ServiceWithEquipments.class);
        Root<Service> root = criteriaQuery.from(Service.class);
        var equipment = root.join(Service_.equipment, JoinType.LEFT);
        var family = equipment.join(Equipment_.family, JoinType.LEFT);
        var domain = equipment.join(Equipment_.domain, JoinType.LEFT);

        final Expression<Boolean> managed = builder.function("jsonb_extract_path_text", Boolean.class,
                equipment.get(Equipment_.attributes), builder.literal(MANAGED));

        var query = criteriaQuery
                .multiselect(
                        family.get(Family_.code),
                        root.get(Service_.status),
                        managed,
                        builder.count(root))
                .where(builder.and(
                        builder.equal(domain.get(Domain_.name), "EP"),
                        root.get(Service_.status).in(List.of(Status.NEW, Status.IN_PROGRESS))))
                .groupBy(family.get(Family_.code), root.get(Service_.status), managed);

        var result = em.createQuery(query)
                .getResultList();

        if (result.isEmpty()) {
            return Map.of();
        }

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

        linkedServicesByStatus.put(UNMANAGED, result.stream()
                .filter(r -> !r.managed())
                .collect(Collectors.groupingBy(ServiceWithEquipments::status,
                        Collectors.summingLong(ServiceWithEquipments::count))));

        return linkedServicesByStatus;
    }

    private Map<String, Long> separateUnmanagedFromManagedEquipments(List<EquipmentWithEvents> result) {
        if (result.isEmpty()) {
            return Map.of();
        }
        var equipmentWithEventsTotal = result.stream()
                .filter(EquipmentWithEvents::managed)
                .collect(Collectors.toMap(EquipmentWithEvents::code, EquipmentWithEvents::count));

        equipmentWithEventsTotal.put(UNMANAGED, result.stream()
                .filter(r -> !r.managed())
                .mapToLong(EquipmentWithEvents::count)
                .sum());
        return equipmentWithEventsTotal;
    }
}
