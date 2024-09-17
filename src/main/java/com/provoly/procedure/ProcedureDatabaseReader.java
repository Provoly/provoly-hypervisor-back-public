package com.provoly.procedure;

import java.util.NoSuchElementException;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;

import com.provoly.DatabaseReader;
import com.provoly.action.Action_;

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
