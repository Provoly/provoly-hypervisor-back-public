package com.provoly.service;

import java.util.Optional;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import com.provoly.DatabaseReader;

@ApplicationScoped
public class ServiceDatabaseReader extends DatabaseReader {

    public ServiceDatabaseReader(EntityManager em) {
        super(em);
    }

    public void saveService(Service service) {
        em.persist(service);
    }

    public Optional<ServiceCategory> getServiceCategoryByCode(String code) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<ServiceCategory> criteriaQuery = builder.createQuery(ServiceCategory.class);
        Root<ServiceCategory> root = criteriaQuery.from(ServiceCategory.class);

        var query = criteriaQuery.select(root)
                .where(builder.equal(root.get(ServiceCategory_.code), code));

        return em.createQuery(query)
                .getResultStream()
                .findFirst();
    }

    public Optional<Service> getServiceWithExternalId(String id) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Service> criteriaQuery = builder.createQuery(Service.class);
        Root<Service> root = criteriaQuery.from(Service.class);

        var query = criteriaQuery.select(root)
                .where(builder.equal(root.get(Service_.externalId), id));

        return em.createQuery(query)
                .getResultStream()
                .findFirst();
    }

    public Stream<Service> getAllServices() {
        var q = em.getCriteriaBuilder().createQuery(Service.class);
        q.select(q.from(Service.class));
        return em.createQuery(q).getResultStream();
    }
}
