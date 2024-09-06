package com.provoly.user;

import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import com.provoly.DatabaseReader;

@ApplicationScoped
public class UserDatabaseReader extends DatabaseReader {
    public UserDatabaseReader(EntityManager em) {
        super(em);
    }

    public void saveUser(User user) {
        em.persist(user);
    }

    public Optional<User> getUserBySubject(UUID subject) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = builder.createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);

        var query = criteriaQuery.select(root)
                .where(builder.equal(root.get(User_.subject), subject));

        return em.createQuery(query)
                .getResultStream()
                .findFirst();
    }
}
