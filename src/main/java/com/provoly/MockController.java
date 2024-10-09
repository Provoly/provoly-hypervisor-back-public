package com.provoly;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import com.provoly.equipment.Equipment;
import com.provoly.event.*;
import com.provoly.model.ProcedureModel;
import com.provoly.service.ServiceService;
import com.provoly.service.ServiceStatus;
import com.provoly.service.ServiceWriteDto;

import io.quarkus.security.Authenticated;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/mock")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MockController {
    private final Random rand = new SecureRandom();

    private final Logger log;

    private final EntityManager entityManager;
    private final EventDatabaseReader databaseReader;
    private final ServiceService serviceService;
    private final EntityManager em;

    public MockController(Logger log,
            EntityManager entityManager,
            EventDatabaseReader databaseReader,
            ServiceService serviceService,
            EntityManager em) {
        this.log = log;
        this.entityManager = entityManager;
        this.databaseReader = databaseReader;
        this.serviceService = serviceService;
        this.em = em;
    }

    @POST
    @Authenticated
    @Transactional
    public void mock(@DefaultValue("30") @Positive @RestQuery int eventNumber,
            @DefaultValue("10") @PositiveOrZero @RestQuery int procedureNumber,
            @DefaultValue("false") @RestQuery boolean withService) {

        log.info("Populating database with sample results");

        var epDomain = databaseReader.getDomainByCode("EP");
        var vpDomain = databaseReader.getDomainByCode("VP");
        var equipments = selectAllEquipment();

        var categories = databaseReader.getCategoryOrSubCategories();

        for (int i = 0; i < procedureNumber; i++) {
            Domain domain = randomDomain(List.of(epDomain, vpDomain));
            var procedureModel = new ProcedureModel("modele de procédure n°%s".formatted(suffix(UUID.randomUUID())),
                    "Agathe ThePower", "desc", domain, List.of());
            entityManager.persist(procedureModel);
        }

        for (int i = 0; i < eventNumber; i++) {
            Event event = new Event();
            event.setCategory(randomCategory(categories));
            event.setName("MOCK: Evenement %s %s".formatted(event.getCategory().name, suffix(UUID.randomUUID())));
            event.setCreationDate(randomCreationDate());

            if (event.getCategory().getCode().equals("MANIFESTATION")) {
                event.setStartDate(event.getCreationDate().plus(1, ChronoUnit.DAYS));
                event.setEndDate(event.getCreationDate().plus(5, ChronoUnit.DAYS));
            }
            event.setAddress("%s rue de Chalons".formatted(i));
            event.setDescription("description of %s".formatted(event.getName()));
            event.setCriticality(randomCriticality());
            event.setDomain(randomDomain(List.of(epDomain, vpDomain)));
            event.setEquipment(randomEquipment(equipments));
            entityManager.persist(event);
            setStatus(event);
            setCloseDate(event);

            if (withService) {
                String serviceId = suffix(UUID.randomUUID());
                Instant creationDate = event.getCreationDate().minus(3, ChronoUnit.DAYS);
                Instant startDate = creationDate;
                var service = new ServiceWriteDto(
                        "Mock: service %s".formatted(serviceId),
                        "description of service %s".formatted(serviceId),
                        event.getEquipment().getName(),
                        creationDate,
                        creationDate.plus(5, ChronoUnit.DAYS),
                        startDate,
                        startDate.plus(3, ChronoUnit.DAYS),
                        startDate.plus(5, ChronoUnit.DAYS),
                        event.getDomain().getCode(),
                        ServiceStatus.DONE,
                        randomServiceCategory());
                serviceService.saveOrUpdateServices(List.of(service));

            }
        }
    }

    private Instant randomCreationDate() {
        var start = Instant.now().minus(4L * 365, ChronoUnit.DAYS);
        var end = Instant.now();
        var durationNbSeconds = Duration.between(start, end).toSeconds();
        var durationFromStart = Duration.of(rand.nextLong(durationNbSeconds), ChronoUnit.SECONDS);
        return start.plus(durationFromStart);
    }

    private List<Equipment> selectAllEquipment() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Equipment> cq = cb.createQuery(Equipment.class);
        Root<Equipment> rootEntry = cq.from(Equipment.class);
        CriteriaQuery<Equipment> all = cq.select(rootEntry);
        TypedQuery<Equipment> allQuery = em.createQuery(all).setMaxResults(100);
        return allQuery.getResultList();
    }

    private void setStatus(Event event) {
        var values = List.of(Status.NEW, Status.DONE);
        var randomStatus = values.get(rand.nextInt(values.size()));
        event.setStatus(randomStatus);
    }

    private void setCloseDate(Event event) {
        if (event.getStatus() == Status.DONE) {
            var start = event.getCreationDate();
            var duration = Duration.ofHours(rand.nextLong(5, 24L * 10L));
            event.setCloseDate(start.plus(duration));
        }
    }

    private String suffix(UUID id) {
        return id.toString().split("-")[0];
    }

    private Criticality randomCriticality() {
        var values = Arrays.stream(Criticality.values()).toList();
        return values.get(rand.nextInt(values.size()));
    }

    private String randomServiceCategory() {
        var values = List.of("PREV", "CURA");
        return values.get(rand.nextInt(values.size()));
    }

    private Domain randomDomain(List<Domain> domains) {
        return domains.get(rand.nextInt(domains.size()));
    }

    private Category randomCategory(Collection<Category> categories) {
        categories = categories.stream().filter(category -> !category.getCode().equals("ANOMALY")).toList();
        return categories.stream().toList().get(rand.nextInt(categories.size()));
    }

    private Equipment randomEquipment(List<Equipment> equipments) {
        return equipments.get(rand.nextInt(equipments.size()));
    }

}
