package com.provoly;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import com.provoly.event.Domain;
import com.provoly.event.Domain_;

@ApplicationScoped
public class DatabaseReader {
    protected EntityManager em;

    public DatabaseReader() {
        // This constructor is needed to avoid following error :
        // "It's not possible to automatically add a synthetic no-args constructor to an unproxyable bean class."
    }

    public DatabaseReader(EntityManager em) {
        this.em = em;
    }

    public Optional<Domain> getDomainByName(String name) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Domain> criteriaQuery = builder.createQuery(Domain.class);
        Root<Domain> root = criteriaQuery.from(Domain.class);

        var query = criteriaQuery.select(root)
                .where(builder.equal(root.get(Domain_.name), name));

        return em.createQuery(query)
                .getResultStream()
                .findFirst();
    }

    public Optional<Domain> getDomainByCode(String code) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Domain> criteriaQuery = builder.createQuery(Domain.class);
        Root<Domain> root = criteriaQuery.from(Domain.class);

        var query = criteriaQuery.select(root)
                .where(builder.equal(root.get(Domain_.code), code));

        return em.createQuery(query)
                .getResultStream()
                .findFirst();
    }

    public Collection<Domain> getDomains() {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Domain> criteriaQuery = builder.createQuery(Domain.class);
        Root<Domain> root = criteriaQuery.from(Domain.class);

        return em.createQuery(criteriaQuery.select(root))
                .getResultList();
    }

    protected Predicate[] getPredicatesAsArray(List<Predicate> predicatesList) {
        Predicate[] pred = new Predicate[predicatesList.size()];
        predicatesList.toArray(pred);
        return pred;
    }

}