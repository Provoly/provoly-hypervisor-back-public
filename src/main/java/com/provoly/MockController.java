package com.provoly;

import java.time.Instant;
import java.util.*;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import com.provoly.action.Service;
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
    private DatabaseReader databaseReader;

    public MockController(EntityManager entityManager, DatabaseReader databaseReader) {
        this.entityManager = entityManager;
        this.databaseReader = databaseReader;
    }

    @POST
    @Authenticated
    @Transactional
    public void mock(@DefaultValue("70") @Positive @RestQuery int eventNumber,
            @DefaultValue("10") @Positive @RestQuery int procedureNumber) {

        var procedures = new ArrayList<Procedure>();
        var domains = databaseReader.getDomains().stream().toList();

        for (int i = 0; i < procedureNumber; i++) {
            var id = UUID.randomUUID();
            var action = new TodoAction(UUID.randomUUID(), randomInstant(), randomStatus(), "todo no%s.0".formatted(i));
            var action2 = new TodoAction(UUID.randomUUID(), randomInstant(), randomStatus(), "todo no%s.1".formatted(i));
            var action3 = new Service(UUID.randomUUID(), randomInstant(), randomStatus(),
                    "demande d'intervention n°%s".formatted(i));
            var procedure = new Procedure(id, "procédure_%s no%s".formatted(suffix(id), i), Instant.now());
            procedure.addAction(action);
            procedure.addAction(action2);
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
                event.setDescription("description");
                event.setCategory(randomOpertaorCategory());
                event.setCriticality(randomCriticality());
                event.setStatus(randomStatus());
                if (event.getCategory() == OperatorCategory.MANIFESTATION) {
                    event.setStartDate(Instant.now());
                    event.setEndDate(Instant.now());
                }
                setDomain(event, domains);
                setProcedure(eventNumber, procedures, event);
                setCloseDate(event);
                entityManager.persist(event);
            }
            if (i % 3 == 1) {
                var id = UUID.randomUUID();
                var event = new EventAlert(id);
                event.setName(getName("Alerte", id, i));
                event.setAddress(getAddress(i));
                event.setDescription("description");
                event.setCategory(randomAlertCategory());
                event.setCriticality(randomCriticality());
                event.setStatus(randomStatus());
                event.setExternalSourceRef("citylinx_%s".formatted(i));
                setDomain(event, domains);
                setProcedure(eventNumber, procedures, event);
                setCloseDate(event);
                entityManager.persist(event);
            }
            if (i % 3 == 2) {
                var id = UUID.randomUUID();
                var event = new EventReport(id);
                event.setName(getName("Signalement", id, i));
                event.setAddress(getAddress(i));
                event.setDescription("description");
                event.setCategory(ReportCategory.REPORT);
                event.setCriticality(randomCriticality());
                event.setStatus(randomStatus());
                event.setExternalSourceRef("grc_%s".formatted(i));
                setDomain(event, domains);
                setProcedure(eventNumber, procedures, event);
                setCloseDate(event);
                entityManager.persist(event);
            }

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

    private void setDomain(Event event, List<Domain> domains) {
        event.setDomain(domains.get(rand.nextInt(domains.size())));
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
        if (eventNumber % 5 == 0) {
            var proc = procedures.get(rand.nextInt(procedures.size()));
            event.setProcedure(proc);
        }
    }

    private Status randomStatus() {
        var values = Collections.unmodifiableList(Arrays.asList(Status.values()));
        return values.get(rand.nextInt(values.size()));
    }

    private Criticality randomCriticality() {
        var values = Collections.unmodifiableList(Arrays.asList(Criticality.values()));
        return values.get(rand.nextInt(values.size()));
    }

    private OperatorCategory randomOpertaorCategory() {
        var values = Collections.unmodifiableList(Arrays.asList(OperatorCategory.values()));
        return values.get(rand.nextInt(values.size()));
    }

    private AlertCategory randomAlertCategory() {
        var values = Collections.unmodifiableList(Arrays.asList(AlertCategory.values()));
        return values.get(rand.nextInt(values.size()));
    }
}
