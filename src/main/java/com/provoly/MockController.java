package com.provoly;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import com.provoly.action.AskedService;
import com.provoly.action.TodoAction;
import com.provoly.event.*;
import com.provoly.procedure.Procedure;

import io.quarkus.security.Authenticated;

import org.jboss.resteasy.reactive.RestQuery;

@Path("/mock")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MockController {
    private Random rand = new Random();

    private EntityManager entityManager;
    private EventDatabaseReader databaseReader;

    public MockController(EntityManager entityManager, EventDatabaseReader databaseReader) {
        this.entityManager = entityManager;
        this.databaseReader = databaseReader;
    }

    @POST
    @Authenticated
    @Transactional
    public void mock(@DefaultValue("30") @Positive @RestQuery int eventNumber,
            @DefaultValue("10") @PositiveOrZero @RestQuery int procedureNumber) {

        var procedures = new ArrayList<Procedure>();
        var epDomain = databaseReader.getDomainByCode("EP").get();

        for (int i = 0; i < procedureNumber; i++) {
            var id = UUID.randomUUID();
            var action = new TodoAction(UUID.randomUUID(), randomInstant(), randomStatusDoneInProgress(),
                    "todo no%s.0".formatted(i));
            var action3 = new AskedService(UUID.randomUUID(), randomInstant(), randomStatusDoneInProgress(),
                    "demande d'intervention n°%s".formatted(i));
            var procedure = new Procedure(id, "procédure_%s no%s".formatted(suffix(id), i), Instant.now());
            procedure.addAction(action);
            procedure.addAction(action3);
            procedures.add(procedure);
            entityManager.persist(procedure);
        }

        for (int i = 0; i <= eventNumber; i++) {
            if (i % 3 == 0) {
                var id = UUID.randomUUID();
                var event = new EventOperator(id);
                event.setName(getName("Evenement operateur", id, i));
                event.setAddress(getAddress(i));
                event.setDescription("description of %s".formatted(event.getName()));
                event.setCategory(randomCategory(EventType.OPERATOR));
                event.setCriticality(randomCriticality());
                event.setDomain(epDomain);
                if (event.getCategory() == Category.MANIFESTATION) {
                    event.setStartDate(Instant.now());
                    event.setEndDate(Instant.now().plus(rand.nextInt(1, 10), ChronoUnit.DAYS));
                }

                entityManager.persist(event);
                setProcedure(eventNumber, procedures, event);
                setStatus(event);
                setCloseDate(event);
            }
            if (i % 3 == 1) {
                var id = UUID.randomUUID();
                var event = new EventAlert(id);
                event.setName(getName("Alerte", id, i));
                event.setAddress(getAddress(i));
                event.setDescription("description of %s".formatted(event.getName()));
                event.setCategory(randomCategory(EventType.ALERT));
                event.setCriticality(randomCriticality());
                event.setExternalSourceRef("citylinx_%s".formatted(i));
                event.setDomain(epDomain);

                entityManager.persist(event);
                setProcedure(eventNumber, procedures, event);
                setStatus(event);
                setCloseDate(event);
            }
            if (i % 3 == 2) {
                var id = UUID.randomUUID();
                var event = new EventReport(id);
                event.setName(getName("Signalement", id, i));
                event.setAddress(getAddress(i));
                event.setDescription("description of %s".formatted(event.getName()));
                event.setCategory(Category.REPORT);
                event.setCriticality(randomCriticality());
                event.setExternalSourceRef("grc_%s".formatted(i));
                event.setDomain(epDomain);

                entityManager.persist(event);
                setProcedure(eventNumber, procedures, event);
                setStatus(event);
                setCloseDate(event);
            }

        }
    }

    private void setStatus(Event event) {
        if (event.getProcedure() != null) {
            event.setStatus(randomStatusDoneInProgress());
        } else {
            event.setStatus(randomStatusNewDone());
        }
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

    private String getName(String prefix, UUID id, int i) {
        return "%s_%s no%s".formatted(prefix, suffix(id), i);
    }

    private String getAddress(int i) {
        return "%s rue de Chalons".formatted(i);
    }

    private String suffix(UUID id) {
        return id.toString().split("-")[0];
    }

    private void setProcedure(int eventNumber, ArrayList<Procedure> procedures, Event event) {
        if (eventNumber % 5 == 0 && !procedures.isEmpty()) {
            var proc = procedures.get(rand.nextInt(procedures.size()));
            event.setProcedure(proc);
        }
    }

    private Status randomStatusDoneInProgress() {
        var values = List.of(Status.IN_PROGRESS, Status.DONE);
        return values.get(rand.nextInt(values.size()));
    }

    private Status randomStatusNewDone() {
        var values = List.of(Status.NEW, Status.DONE);
        return values.get(rand.nextInt(values.size()));
    }

    private Criticality randomCriticality() {
        var values = Arrays.stream(Criticality.values()).toList();
        return values.get(rand.nextInt(values.size()));
    }

    private Category randomCategory(EventType type) {
        var values = Arrays.stream(Category.values()).filter(category -> category.getEventType() == type).toList();
        return values.get(rand.nextInt(values.size()));
    }

}
