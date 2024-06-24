package com.provoly;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

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

        for (int i = 0; i < procedureNumber; i++) {
            var procedureModel = new ProcedureModel("modele de procédure n°%s".formatted(suffix(UUID.randomUUID())),
                    "Agathe ThePower", "desc", randomDomain(List.of(epDomain, vpDomain)), List.of());
            entityManager.persist(procedureModel);
        }

        for (int i = 0; i <= eventNumber; i++) {
            Event event = new EventOperator();
            if (i % 3 == 0) {
                event.setName("Evenement operateur %s".formatted(suffix(UUID.randomUUID())));
                event.setCategory(randomCategory(EventType.OPERATOR));
                if (event.getCategory() == Category.MANIFESTATION) {
                    ((EventOperator) event).setStartDate(Instant.now());
                    ((EventOperator) event).setEndDate(Instant.now().plus(rand.nextInt(1, 10), ChronoUnit.DAYS));
                }

            }
            if (i % 3 == 1) {
                event = new EventAlert();
                event.setName("Alerte %s".formatted(suffix(UUID.randomUUID())));
                event.setCategory(randomCategory(EventType.ALERT));
                ((EventAlert) event).setExternalSourceRef("citylinx_%s".formatted(i));

            }
            if (i % 3 == 2) {
                event = new EventReport();
                event.setName("Signalement %s".formatted(suffix(UUID.randomUUID())));
                event.setCategory(Category.REPORT);
                ((EventReport) event).setExternalSourceRef("grc_%s".formatted(i));

            }
            event.setAddress(getAddress(i));
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

    private String getAddress(int i) {
        return "%s rue de Chalons".formatted(i);
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

    private Category randomCategory(EventType type) {
        var values = Arrays.stream(Category.values()).filter(category -> category.getEventType() == type).toList();
        return values.get(rand.nextInt(values.size()));
    }

}
