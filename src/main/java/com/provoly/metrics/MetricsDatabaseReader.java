package com.provoly.metrics;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;

import com.provoly.DatabaseReader;
import com.provoly.action.Action;
import com.provoly.equipment.*;
import com.provoly.event.*;

@ApplicationScoped
public class MetricsDatabaseReader extends DatabaseReader {
    public static final String UNMANAGED = "unmanaged";
    public static final String MANAGED = "managed";

    private final EquipmentService equipmentService;

    public MetricsDatabaseReader(EntityManager em, EquipmentService equipmentService) {
        super(em);
        this.equipmentService = equipmentService;
    }

    private record QueryResult(String code, boolean managed, long count) {
    }

    public List<Equipment> getEquipmentsWithUnDoneEvents(List<String> entities,
            List<String> criticalities,
            List<String> categories) {

        var eventCriticalities = criticalities.stream().map(Criticality::fromString).toList();
        var eventCategories = categories.stream().map(Category::fromString).toList();
        return equipmentService
                .getEquipments(entities)
                .stream()
                .filter(equipment -> equipment.getDomain().getName().equals("EP"))
                .filter(equipment -> matchEvents(equipment.getEvents(), eventCriticalities, eventCategories))
                .toList();

    }

    private boolean matchEvents(List<Event> events, List<Criticality> criticalities, List<Category> categories) {
        return events
                .stream()
                .anyMatch(event -> isUnDone()
                        .and(isNotManifestation())
                        .and(isOneOfCategory(categories))
                        .and(isOneOfCriticality(criticalities)).test(event));
    }

    public Map<String, Long> equipmentsCountGroupedByManaged(List<Equipment> equipments) {
        var groupedByManagedAndCode = equipments
                .stream()
                .collect(Collectors.groupingBy(equipment -> equipment.getAttributes().get(MANAGED),
                        Collectors.groupingBy(equipment -> equipment.getFamily().getCode(), Collectors.counting())));

        var result = groupedByManagedAndCode.getOrDefault(true, new HashMap<>());
        long mergedUnmanagedEquipments = groupedByManagedAndCode.getOrDefault(false, Map.of()).values().stream()
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

    public Map<String, Map<Status, Long>> getEquipmentServicesByStatus(List<Equipment> equipments) {
        var equipmentServices = equipments.stream()
                .map(Equipment::getServices)
                .flatMap(Collection::stream)
                .filter(service -> service.getStatus() != Status.DONE)
                .collect(Collectors.groupingBy(service -> service.getEquipment().getAttributes().get(MANAGED),
                        Collectors.groupingBy(service -> service.getEquipment().getFamily().getCode(),
                                Collectors.groupingBy(Action::getStatus, Collectors.counting()))));

        var result = equipmentServices.getOrDefault(true, new HashMap<>());

        var mergedUnmanagedEquipments = equipmentServices.getOrDefault(false, Map.of()).values()
                .stream()
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingLong(Map.Entry::getValue)));

        result.put(UNMANAGED, mergedUnmanagedEquipments);
        return result;
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

    private Predicate<Event> isUnDone() {
        return event -> event.getStatus() != Status.DONE;
    }

    private Predicate<Event> isNotManifestation() {
        return event -> event.getCategory() != Category.MANIFESTATION;
    }

    private Predicate<Event> isOneOfCriticality(List<Criticality> criticalities) {
        return event -> (criticalities.isEmpty()) || (criticalities.contains(event.getCriticality()));
    }

    private Predicate<Event> isOneOfCategory(List<Category> categories) {
        return event -> (categories.isEmpty()) || (categories.contains(event.getCategory()));
    }
}
