package com.provoly.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;

import com.provoly.DatabaseReader;
import com.provoly.event.Domain;
import com.provoly.event.Domain_;
import com.provoly.event.SortOrder;

import org.jboss.logging.Logger;

@ApplicationScoped
public class ProcedureModelDatabaseReader extends DatabaseReader {
    private final Logger logger;

    protected ProcedureModelDatabaseReader(EntityManager em, Logger logger) {
        super(em);
        this.logger = logger;
    }

    public ProcedureModel getProcedureModelById(Integer id) {
        var procedureModel = em.find(ProcedureModel.class, id);
        if (procedureModel == null) {
            throw new NoSuchElementException("Procedure model with id %s not found".formatted(id));
        }
        return procedureModel;
    }

    public Collection<ProcedureModel> getProcedureModels(int page,
            int pageSize,
            ProcedureModelSort sort,
            SortOrder order,
            List<Domain> domains,
            String search) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<ProcedureModel> criteriaQuery = builder.createQuery(ProcedureModel.class);
        Root<ProcedureModel> root = criteriaQuery.from(ProcedureModel.class);

        Predicate domain = builder.and(); // default to true
        Predicate filters = builder.and();

        if (search != null) {
            search = "%" + search + "%";
            logger.debugf("filter on procedures model that contains '%s' in id, name or creator".formatted(search));
            filters = builder.or(builder.like(root.get(ProcedureModel_.id).as(String.class), search),
                    builder.like(root.get(ProcedureModel_.name), search),
                    builder.like(root.get(ProcedureModel_.creator), search));
        }

        if (!domains.isEmpty()) {
            logger.debugf("filter on domains %s", domains);
            domain = root.get(ProcedureModel_.domain).in(domains);
        }

        List<Order> orders = buildProcedureModelOrders(sort, order, builder, root);

        var query = criteriaQuery.select(root)
                .where(builder.and(filters, domain))
                .orderBy(orders);

        return em.createQuery(query)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public void saveProcedureModel(ProcedureModel procedureModel) {
        em.persist(procedureModel);
    }

    public boolean isProcedureModelWithNameExists(String name) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<ProcedureModel> criteriaQuery = builder.createQuery(ProcedureModel.class);
        Root<ProcedureModel> root = criteriaQuery.from(ProcedureModel.class);

        var query = criteriaQuery.select(root)
                .where(builder.equal(root.get(ProcedureModel_.name), name));

        return em.createQuery(query)
                .getResultStream()
                .findFirst()
                .isPresent();

    }

    private List<Order> buildProcedureModelOrders(ProcedureModelSort sort,
            SortOrder order,
            CriteriaBuilder builder,
            Root<ProcedureModel> root) {
        List<Order> orders = new ArrayList<>();
        if (sort == null) {
            logger.debugf("No sort provided, use default sort: by use count and name");
            orders.add(builder.desc(root.get(ProcedureModel_.useCount)));
            orders.add(builder.asc(root.get(ProcedureModel_.name)));
            return orders;
        }

        logger.debugf("Sort on %s with order %s", sort, order);
        var sortProperty = getSortProperty(sort, root);
        orders.add(order == SortOrder.DESC ? builder.desc(sortProperty) : builder.asc(sortProperty));
        return orders;
    }

    private Expression<?> getSortProperty(ProcedureModelSort sort, Root<ProcedureModel> root) {
        return switch (sort) {
            case ID -> root.get(ProcedureModel_.id);
            case USE_COUNT -> root.get(ProcedureModel_.useCount);
            case NAME -> root.get(ProcedureModel_.name);
            case CREATOR -> root.get(ProcedureModel_.creator);
            case DOMAIN -> {
                var domain = root.join(ProcedureModel_.domain, JoinType.LEFT);
                yield domain.get(Domain_.code);
            }
            case CREATION_DATE -> root.get(ProcedureModel_.creationDate);
            case LAST_MODIFICATION_DATE -> root.get(ProcedureModel_.lastModificationDate);
        };
    }

}
