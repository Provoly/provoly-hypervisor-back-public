package com.provoly.action;

import java.util.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;

import com.provoly.DatabaseReader;

import org.jboss.logging.Logger;

@ApplicationScoped
public class ActionDatabaseReader extends DatabaseReader {
    private final Logger logger;

    protected ActionDatabaseReader(EntityManager em, Logger logger) {
        super(em);
        this.logger = logger;
    }

    public Optional<Action> getActionById(UUID id) {
        var action = em.find(Action.class, id);
        return Optional.of(action);
    }

    public boolean isActionWithIdExists(UUID id) {
        return em.find(Action.class, id) != null;
    }

}
