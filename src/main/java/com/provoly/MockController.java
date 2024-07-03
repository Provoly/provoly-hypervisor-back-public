package com.provoly;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import com.provoly.event.*;
import com.provoly.model.ProcedureModel;

import io.quarkus.security.Authenticated;

import org.jboss.resteasy.reactive.RestQuery;

@Path("/mock")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MockController {
    private final Random rand = new SecureRandom();

    private final EntityManager entityManager;
    private final EventDatabaseReader databaseReader;

    public MockController(EntityManager entityManager, EventDatabaseReader databaseReader) {
        this.entityManager = entityManager;
        this.databaseReader = databaseReader;
    }

    @POST
    @Authenticated
    @Transactional
    public void mock(@DefaultValue("30") @Positive @RestQuery int eventNumber,
            @DefaultValue("10") @PositiveOrZero @RestQuery int procedureNumber) {

        var epDomain = databaseReader.getDomainByCode("EP").get();
        var vpDomain = databaseReader.getDomainByCode("VP").get();

        var categories = databaseReader.getCategories();

        for (int i = 0; i < procedureNumber; i++) {
            var procedureModel = new ProcedureModel("modele de procédure n°%s".formatted(suffix(UUID.randomUUID())),
                    "Agathe ThePower", "desc", randomDomain(List.of(epDomain, vpDomain)), List.of());
            entityManager.persist(procedureModel);
        }

        for (int i = 0; i <= eventNumber; i++) {
            Event event = new Event();
            event.setCategory(randomCategory(categories));
            event.setName("Evenement %s %s".formatted(event.getCategory().name, suffix(UUID.randomUUID())));

            if (event.getCategory().getCode().equals("MANIFESTATION")) {
                event.setStartDate(Instant.now());
                event.setEndDate(Instant.now().plus(rand.nextInt(1, 10), ChronoUnit.DAYS));
            }
            event.setAddress("%s rue de Chalons".formatted(i));
            event.setDescription("description of %s".formatted(event.getName()));
            event.setCriticality(randomCriticality());
            event.setDomain(randomDomain(List.of(epDomain, vpDomain)));
            entityManager.persist(event);
            setStatus(event);
            setCloseDate(event);
        }
    }

    private void setStatus(Event event) {
        var values = List.of(Status.NEW, Status.DONE);
        var randomStatus = values.get(rand.nextInt(values.size()));
        event.setStatus(randomStatus);
    }

    private void setCloseDate(Event event) {
        if (event.getStatus() == Status.DONE) {
            event.setCloseDate(randomInstant());
        }
    }

    private Instant randomInstant() {
        long date1 = 1704118449; // 1/1/24
        long date2 = 1735654449; //31/12/24
        return Instant.ofEpochSecond(rand.nextLong(date2 - date1) + date1);
    }

    private String suffix(UUID id) {
        return id.toString().split("-")[0];
    }

    private Criticality randomCriticality() {
        var values = Arrays.stream(Criticality.values()).toList();
        return values.get(rand.nextInt(values.size()));
    }

    private Domain randomDomain(List<Domain> domains) {
        return domains.get(rand.nextInt(domains.size()));
    }

    private Category randomCategory(Collection<Category> categories) {
        return categories.stream().toList().get(rand.nextInt(categories.size()));
    }

}
