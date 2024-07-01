package com.provoly.action;

import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import com.provoly.DatabaseReader;

@ApplicationScoped
public class ActionDatabaseReader extends DatabaseReader {

    protected ActionDatabaseReader(EntityManager em) {
        super(em);
    }

    public Optional<Action> getActionById(UUID id) {
        var action = em.find(Action.class, id);
        return Optional.ofNullable(action);
    }

    public void saveAction(Action action) {
        em.persist(action);
    }

    public boolean isCustomActionType(String type) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<CustomActionType> criteriaQuery = builder.createQuery(CustomActionType.class);
        Root<CustomActionType> root = criteriaQuery.from(CustomActionType.class);

        var query = criteriaQuery.select(root)
                .where(builder.equal(root.get(CustomActionType_.code), type));

        return em.createQuery(query)
                .getResultStream()
                .findFirst()
                .isPresent();
    }

}
