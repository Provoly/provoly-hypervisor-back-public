package com.provoly.service.coswin;

import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import com.provoly.DatabaseReader;

@ApplicationScoped
public class ServiceTypeDatabaseReader extends DatabaseReader {

    public ServiceTypeDatabaseReader(EntityManager em) {
        super(em);
    }

    public Stream<ServiceType> getAllServicesType(String domain) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<ServiceType> criteriaQuery = builder.createQuery(ServiceType.class);
        Root<ServiceType> root = criteriaQuery.from(ServiceType.class);

        var query = criteriaQuery.select(root);

        if (domain != null) {
            query.where(builder.equal(root.get(ServiceType_.domain), domain));
        }

        return em.createQuery(query)
                .getResultStream();
    }

    public ServiceType getServiceType(String type) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<ServiceType> criteriaQuery = builder.createQuery(ServiceType.class);
        Root<ServiceType> root = criteriaQuery.from(ServiceType.class);

        var query = criteriaQuery.select(root)
                .where(builder.equal(root.get(ServiceType_.type), type));

        return em.createQuery(query)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No service type found for type " + type));
    }
}
