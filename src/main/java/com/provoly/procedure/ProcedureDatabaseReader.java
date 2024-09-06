package com.provoly.procedure;

import java.util.NoSuchElementException;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;

import com.provoly.DatabaseReader;
import com.provoly.action.Action_;
import com.provoly.event.Event;
import com.provoly.event.Event_;

@ApplicationScoped
public class ProcedureDatabaseReader extends DatabaseReader {

    protected ProcedureDatabaseReader(EntityManager em) {
        super(em);
    }

    public Procedure getProcedureById(Integer id) {
        var procedure = em.find(Procedure.class, id);
        if (procedure == null) {
            throw new NoSuchElementException("Procedure with id %s not found".formatted(id));
        }
        return procedure;
    }

    public void saveProcedure(Procedure procedure) {
        em.persist(procedure);
    }

    public long getLinkedEventCountForProcedure(Integer id) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
        var root = criteriaQuery.from(Event.class);
        var procedure = root.join(Event_.procedure);

        var query = criteriaQuery.select(builder.count(root))
                .where(builder.equal(procedure.get(Procedure_.id), id))
                .groupBy(procedure.get(Procedure_.id));

        return em.createQuery(query).getSingleResult();
    }

    public Procedure getProcedureForAction(UUID actionId) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Procedure> criteriaQuery = builder.createQuery(Procedure.class);
        var root = criteriaQuery.from(Procedure.class);
        var action = root.join(Procedure_.actions);

        var query = criteriaQuery.select(root)
                .where(builder.equal(action.get(Action_.id), actionId));

        return em.createQuery(query).getSingleResult();
    }

    public void removeProcedure(Procedure procedure) {
        em.remove(procedure);
    }
}
