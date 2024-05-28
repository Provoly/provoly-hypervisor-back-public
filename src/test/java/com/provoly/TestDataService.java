package com.provoly;

import java.time.Instant;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import com.provoly.action.Service;
import com.provoly.equipment.Equipment;
import com.provoly.equipment.EquipmentDatabaseReader;
import com.provoly.equipment.EquipmentEntity;
import com.provoly.equipment.Family;
import com.provoly.event.*;
import com.provoly.procedure.Procedure;

@ApplicationScoped
public class TestDataService {
    private EntityManager em;
    private EquipmentDatabaseReader equipmentDatabaseReader;
    private Random rand = new Random();
    public Procedure procedure1, procedure2, procedure3;
    private Domain domain;

    public TestDataService(EntityManager em, EquipmentDatabaseReader equipmentDatabaseReader) {
        this.em = em;
        this.equipmentDatabaseReader = equipmentDatabaseReader;
    }

    @Transactional
    public void init() {
        domain = equipmentDatabaseReader.getDomainByName("EP").get();

        var armoire = equipmentDatabaseReader.getFamilyByCode("EP_ARMOIRE").get();
        var foyerLumineux = equipmentDatabaseReader.getFamilyByCode("EP_FOYER_LUMINEUX").get();
        var ouvrage = equipmentDatabaseReader.getFamilyByCode("EP_OUVRAGE").get();

        var chalons = equipmentDatabaseReader.getEquipmentEntityByName("CHALONS_COMMUN").get();
        var agglo = equipmentDatabaseReader.getEquipmentEntityByName("AGGLO_COMMUN").get();
        var fagnieres = equipmentDatabaseReader.getEquipmentEntityByName("FAGNIERES_COMMUN").get();
        var stm = equipmentDatabaseReader.getEquipmentEntityByName("SAINT_MARTIN_COMMUN").get();

        var equip1 = initEquipment("P-1000", foyerLumineux, fagnieres, false);
        var equip2 = initEquipment("A-230", armoire, stm, true);
        var equip3 = initEquipment("A-4901", armoire, agglo, false);
        var equip4 = initEquipment("C-1034", foyerLumineux, agglo, true);
        var equip5 = initEquipment("C-7614", ouvrage, chalons, false);
        var equip6 = initEquipment("C-762", ouvrage, agglo, true);
        var equip7 = initEquipment("C-763", ouvrage, agglo, false);

        var service1 = new Service(UUID.randomUUID(), Instant.now(), Status.IN_PROGRESS, "service 1", null);
        var service2 = new Service(UUID.randomUUID(), Instant.now(), Status.NEW, "service 2", null);
        var service3 = new Service(UUID.randomUUID(), Instant.now(), Status.DONE, "service 3", null);
        var service4 = new Service(UUID.randomUUID(), Instant.now(), Status.IN_PROGRESS, "service 4", null);
        var service5 = new Service(UUID.randomUUID(), Instant.now(), Status.NEW, "service 5", null);
        var service6 = new Service(UUID.randomUUID(), Instant.now(), Status.DONE, "service 6", null);

        procedure1 = initProcedure("procedure1", List.of(service1, service2));
        procedure2 = initProcedure("procedure2", List.of(service3, service4, service5));
        procedure3 = initProcedure("procedure3", List.of(service6));

        initOperatorEvent("operator1", Category.OPERATOR_EVENT, Criticality.LOW, Status.NEW, null, equip1);
        initOperatorEvent("manfestation1", Category.MANIFESTATION, Criticality.MEDIUM, Status.IN_PROGRESS, procedure1,
                equip2);
        initReportEvent("report1", Criticality.LOW, Status.NEW, null, equip6);
        initReportEvent("report2", Criticality.HIGH, Status.IN_PROGRESS, procedure3, equip3);
        initReportEvent("report3", Criticality.MEDIUM, Status.DONE, procedure2, equip5);
        initAlertEvent("malfunction1", Category.ALERT_MALFUNCTION, Criticality.LOW, Status.NEW, procedure3, equip4);
        initAlertEvent("limint1", Category.ALERT_LIMIT, Criticality.LOW, Status.NEW, null, equip7);

    }

    @Transactional
    public void clean() {
        removeEntities(Event.class);
        removeEntities(Service.class);
        removeEntities(Equipment.class);
        removeEntities(Procedure.class);
    }

    public UUID getProcedureId1() {
        return procedure1.getId();
    }

    public UUID getProcedureId3() {
        return procedure3.getId();
    }

    private Procedure initProcedure(String name, List<Service> services) {
        var procedure = new Procedure(UUID.randomUUID(), name, Instant.now());
        for (var service : services) {
            procedure.addAction(service);
        }
        em.persist(procedure);
        return procedure;
    }

    private void initOperatorEvent(String name, Category category, Criticality criticality, Status status,
            Procedure procedure,
            Equipment equipment) {
        var event = new EventOperator(UUID.randomUUID());
        event.setName(name);
        event.setAddress("event operator address");
        event.setDescription("description");
        event.setCategory(category);
        event.setCriticality(criticality);
        event.setStatus(status);
        if (event.getCategory() == Category.MANIFESTATION) {
            event.setStartDate(Instant.now());
            event.setEndDate(Instant.now());
        }
        if (status == Status.DONE) {
            event.setCloseDate(randomInstantBetweenNowAndAMonthLater());
        }
        if (procedure != null) {
            event.setProcedure(procedure);
        }
        if (equipment != null) {
            event.setEquipment(equipment);
        }
        event.setDomain(domain);
        em.persist(event);
    }

    private void initReportEvent(String name, Criticality criticality, Status status, Procedure procedure,
            Equipment equipment) {
        var event = new EventReport(UUID.randomUUID());
        event.setName(name);
        event.setAddress("report event address");
        event.setDescription("description");
        event.setCategory(Category.REPORT);
        event.setCriticality(criticality);
        event.setStatus(status);
        event.setExternalSourceRef("external_source");
        event.setDomain(domain);
        if (procedure != null) {
            event.setProcedure(procedure);
        }
        if (status == Status.DONE) {
            event.setCloseDate(randomInstantBetweenNowAndAMonthLater());
        }
        if (equipment != null) {
            event.setEquipment(equipment);
        }
        em.persist(event);
    }

    private void initAlertEvent(String name, Category category, Criticality criticality, Status status,
            Procedure procedure,
            Equipment equipment) {
        var event = new EventAlert(UUID.randomUUID());
        event.setName(name);
        event.setAddress("report event address");
        event.setDescription("description");
        event.setCategory(category);
        event.setCriticality(criticality);
        event.setStatus(status);
        event.setExternalSourceRef("external_source");
        event.setDomain(domain);
        if (procedure != null) {
            event.setProcedure(procedure);
        }
        if (status == Status.DONE) {
            event.setCloseDate(randomInstantBetweenNowAndAMonthLater());
        }

        if (equipment != null) {
            event.setEquipment(equipment);
        }
        em.persist(event);
    }

    @Transactional
    public void persistService(Service service) {
        em.persist(service);
    }

    private Equipment initEquipment(String name, Family family, EquipmentEntity entity, boolean managed) {
        var equipment = new Equipment(UUID.randomUUID());
        equipment.setExternalId(name);
        equipment.setName(name);
        equipment.setCode(name);
        equipment.setFamily(family);
        equipment.setEntity(entity);
        equipment.setDomain(domain);
        equipment.setAttributes(Map.of("managed", managed));
        em.persist(equipment);
        return equipment;
    }

    private Instant randomInstantBetweenNowAndAMonthLater() {
        long date1 = Instant.now().getEpochSecond();
        long date2 = Instant.now().plus(Period.ofDays(30)).getEpochSecond();
        return Instant.ofEpochSecond(rand.nextLong(date2 - date1) + date1);
    }

    private <T> void removeEntities(Class<T> className) {
        var cqService = em.getCriteriaBuilder().createQuery(className);
        var entities = em.createQuery(cqService.select(cqService.from(className))).getResultList();
        for (var e : entities) {
            em.remove(e);
        }
    }

}
