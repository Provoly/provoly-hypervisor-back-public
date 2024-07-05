package com.provoly.event;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;

import com.provoly.DatabaseReader;
import com.provoly.equipment.EquipmentEntity;
import com.provoly.equipment.Equipment_;
import com.provoly.equipment.Family;
import com.provoly.procedure.Procedure_;

import org.jboss.logging.Logger;

@ApplicationScoped
public class EventDatabaseReader extends DatabaseReader {
    private Logger logger;

    protected EventDatabaseReader(EntityManager em, Logger logger) {
        super(em);
        this.logger = logger;
    }

    public Collection<Event> getEvents(int page,
            int pageSize,
            EventSort sort,
            SortOrder order,
            Instant creationDate,
            List<Criticality> criticalities,
            List<Status> status,
            List<Category> categories,
            List<EquipmentEntity> entities,
            List<Family> families) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Event> criteriaQuery = builder.createQuery(Event.class);
        Root<Event> root = criteriaQuery.from(Event.class);

        List<Predicate> predicates = new ArrayList<>();

        if (creationDate != null) {
            logger.debugf("filter on creation date %s", creationDate);
            predicates.add(builder.between(root.get(Event_.creationDate),
                    creationDate,
                    creationDate.plus(1, ChronoUnit.DAYS)));
        }

        if (!criticalities.isEmpty()) {
            logger.debugf("filter on criticalities %s", criticalities);
            predicates.add(root.get(Event_.criticality).in(criticalities));
        }

        if (!status.isEmpty()) {
            logger.debugf("filter on status %s", status);
            predicates.add(root.get(Event_.status).in(status));
        }

        if (!entities.isEmpty()) {
            logger.debugf("filter on equipment entity %s", entities);
            var equipment = root.join(Event_.equipment, JoinType.LEFT);
            predicates.add(equipment.get(Equipment_.entity).in(entities));
        }

        if (!families.isEmpty()) {
            logger.debugf("filter on equipment families %s", families);
            var equipment = root.join(Event_.equipment, JoinType.LEFT);
            predicates.add(equipment.get(Equipment_.family).in(families));
        }

        if (!categories.isEmpty()) {
            logger.debugf("filter on categories %s", categories);
            categories = categories.stream()
                    .map(this::getCategoryOrSubCategories)
                    .flatMap(Collection::stream)
                    .toList();
            predicates.add(root.get(Event_.category).in(categories));
        }

        var filters = getPredicatesAsArray(predicates);

        List<Order> orders = buildEventOrders(sort, order, builder, root);

        var query = criteriaQuery.select(root)
                .where(builder.and(filters))
                .orderBy(orders);

        return em.createQuery(query)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public List<Event> getEvents(Status status, int limit, Criticality criticality) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Event> criteriaQuery = builder.createQuery(Event.class);
        Root<Event> root = criteriaQuery.from(Event.class);

        List<Predicate> predicatesList = new ArrayList<>();
        predicatesList.add(builder.equal(root.get(Event_.status), status));

        if (criticality != null) {
            predicatesList.add(builder.equal(root.get(Event_.criticality), criticality));
        }

        var query = criteriaQuery.select(root)
                .where(builder.and(getPredicatesAsArray(predicatesList)))
                .orderBy(builder.desc(root.get(Event_.lastModificationDate)));

        return em.createQuery(query)
                .setMaxResults(limit)
                .getResultList();
    }

    public Map<Status, Long> getCountEventsByStatus() {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> criteriaQuery = builder.createQuery(Object[].class);
        Root<Event> root = criteriaQuery.from(Event.class);

        var query = criteriaQuery.multiselect(root.get(Event_.status), builder.count(root))
                .groupBy(root.get(Event_.status));

        return em.createQuery(query)
                .getResultStream()
                .collect(Collectors.toMap(object -> (Status) object[0], object -> (Long) object[1]));

    }

    public Event getEventById(Integer id) {
        var event = em.find(Event.class, id);
        if (event == null) {
            throw new NoSuchElementException("Event with id %s not found".formatted(id));
        }
        return event;
    }

    public void saveEvent(Event event) {
        em.persist(event);
    }

    public boolean isEventWithNameExists(String name) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Event> criteriaQuery = builder.createQuery(Event.class);
        Root<Event> root = criteriaQuery.from(Event.class);

        var query = criteriaQuery.select(root)
                .where(builder.equal(root.get(Event_.name), name));

        return em.createQuery(query)
                .getResultStream()
                .findFirst()
                .isPresent();

    }

    public Collection<Category> getCategoryOrSubCategories() {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Category> criteriaQuery = builder.createQuery(Category.class);
        Root<Category> root = criteriaQuery.from(Category.class);

        return em.createQuery(criteriaQuery.select(root))
                .getResultList();
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

    private List<Order> buildEventOrders(EventSort sort,
            SortOrder order,
            CriteriaBuilder builder,
            Root<Event> root) {
        List<Order> orders = new ArrayList<>();
        if (sort == null) {
            logger.debugf("No sort provided, use default sort: by status and last modification date");
            orders.add(builder.asc(getStatusOrder(builder, root)));
            orders.add(builder.asc(getCriticalityOrder(builder, root)));
            orders.add(builder.desc(root.get(Event_.lastModificationDate)));
            return orders;
        }

        logger.debugf("Sort on %s with order %s", sort, order);
        var sortProperty = getSortProperty(sort, builder, root);
        orders.add(order == SortOrder.DESC ? builder.desc(sortProperty) : builder.asc(sortProperty));

        return orders;
    }

    private Expression<?> getSortProperty(EventSort event, CriteriaBuilder builder, Root<Event> root) {
        return switch (event) {
            case CREATION_DATE -> root.get(Event_.creationDate);
            case LAST_MODIFICATION_DATE -> root.get(Event_.lastModificationDate);
            case STATUS -> getStatusOrder(builder, root);
            case PROCEDURE_PROGRESS -> {
                var procedure = root.join(Event_.procedure, JoinType.LEFT);
                yield builder.coalesce(procedure.get(Procedure_.procedureProgress), 0);
            }
        };
    }

    private List<Category> getCategoryOrSubCategories(Category category) {
        var children = getSubCategories(category).toList();
        if (children.isEmpty()) {
            return List.of(category);
        }
        return children;
    }

    private Expression<Object> getStatusOrder(CriteriaBuilder builder, Root<Event> root) {
        return builder.selectCase(root.get(Event_.status))
                .when(Status.NEW, Status.NEW.getPriority())
                .when(Status.IN_PROGRESS, Status.IN_PROGRESS.getPriority())
                .when(Status.DONE, Status.DONE.getPriority())
                .otherwise(10);
    }

    private Expression<Object> getCriticalityOrder(CriteriaBuilder builder, Root<Event> root) {
        return builder.selectCase(root.get(Event_.criticality))
                .when(Criticality.HIGH, Criticality.HIGH.getPriority())
                .when(Criticality.MEDIUM, Criticality.MEDIUM.getPriority())
                .when(Criticality.LOW, Criticality.LOW.getPriority())
                .otherwise(10);
    }

}