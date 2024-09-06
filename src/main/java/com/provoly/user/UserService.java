package com.provoly.user;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import io.quarkus.security.identity.SecurityIdentity;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

@ApplicationScoped
public class UserService {
    private final Logger logger;
    private final SecurityIdentity securityIdentity;
    private final UserDatabaseReader databaseReader;

    public UserService(Logger logger, SecurityIdentity securityIdentity, UserDatabaseReader databaseReader) {
        this.logger = logger;
        this.securityIdentity = securityIdentity;
        this.databaseReader = databaseReader;
    }

    public String getCurrentUserName() {
        return securityIdentity.getPrincipal().getName();
    }

    public UUID getCurrentUserSubject() {
        var jsonWebToken = (JsonWebToken) securityIdentity.getPrincipal();
        return UUID.fromString(jsonWebToken.getSubject());
    }

    public String getCurrentUserFullName() {
        var jsonWebToken = (JsonWebToken) securityIdentity.getPrincipal();
        return jsonWebToken.getClaim("name");
    }

    public User buildUser() {
        return new User(getCurrentUserSubject(), getCurrentUserName(), getCurrentUserFullName());
    }

    public boolean hasRole(String role) {
        return securityIdentity.hasRole(role);
    }

    @Transactional
    public User getUserBySubject() {
        return databaseReader.getUserBySubject(getCurrentUserSubject())
                .orElse(saveAndReturnNewUser());
    }

    private User saveAndReturnNewUser() {
        logger.debugf("Unknown user, save it");
        var user = buildUser();
        databaseReader.saveUser(user);
        return user;
    }
}
