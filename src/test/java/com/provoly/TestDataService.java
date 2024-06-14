package com.provoly;

import static com.provoly.service.ServiceStatus.ASKED;
import static com.provoly.service.ServiceStatus.DONE;
import static com.provoly.service.ServiceStatus.IN_PROGRESS;

import java.time.Instant;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import com.provoly.action.AskedService;
import com.provoly.equipment.*;
import com.provoly.event.*;
import com.provoly.procedure.Procedure;
import com.provoly.service.Service;
import com.provoly.service.ServiceCategory;
import com.provoly.service.ServiceDatabaseReader;

@ApplicationScoped
public class TestDataService {
    private EntityManager em;
    private EquipmentDatabaseReader equipmentDatabaseReader;
    private ServiceDatabaseReader serviceDatabaseReader;
    private Random rand = new Random();
    public Procedure procedure1, procedure2, procedure3;
    private Domain domain;
    private ServiceCategory prev;
    private ServiceCategory cura;

    public TestDataService(EntityManager em, EquipmentDatabaseReader equipmentDatabaseReader,
            ServiceDatabaseReader serviceDatabaseReader) {
        this.em = em;
        this.equipmentDatabaseReader = equipmentDatabaseReader;
        this.serviceDatabaseReader = serviceDatabaseReader;
    }

    @Transactional
    public void init() {
        domain = equipmentDatabaseReader.getDomainByCode("EP").get();
        prev = serviceDatabaseReader.getServiceCategoryByCode("PREV").get();
        cura = serviceDatabaseReader.getServiceCategoryByCode("CURA").get();

        var chalonsCity = equipmentDatabaseReader.getCityByCode("CH").get();
        var chalonsDistrict = equipmentDatabaseReader.getDistrictByCode("CH_C").get();

        var fagniereCity = equipmentDatabaseReader.getCityByCode("FAGN").get();
        var fagniereDistrict = equipmentDatabaseReader.getDistrictByCode("FAGN").get();

        var armoire = equipmentDatabaseReader.getFamilyByCode("EP_ARMOIRE").get();
        var foyerLumineux = equipmentDatabaseReader.getFamilyByCode("EP_FOYER_LUMINEUX").get();
        var ouvrage = equipmentDatabaseReader.getFamilyByCode("EP_OUVRAGE").get();

        var chalons = equipmentDatabaseReader.getEquipmentEntityByName("CHALONS_COMMUN").get();
        var agglo = equipmentDatabaseReader.getEquipmentEntityByName("AGGLO_COMMUN").get();
        var fagnieres = equipmentDatabaseReader.getEquipmentEntityByName("FAGNIERES_COMMUN").get();
        var stm = equipmentDatabaseReader.getEquipmentEntityByName("SAINT_MARTIN_COMMUN").get();

        var equip1 = initEquipment("P-1000", foyerLumineux, fagnieres, fagniereCity, fagniereDistrict, 0);
        var equip2 = initEquipment("A-230", armoire, stm, chalonsCity, chalonsDistrict, 1);
        var equip3 = initEquipment("A-4901", armoire, agglo, chalonsCity, chalonsDistrict, 0);
        var equip4 = initEquipment("C-1034", foyerLumineux, agglo, chalonsCity, chalonsDistrict, 1);
        var equip5 = initEquipment("C-7614", ouvrage, chalons, chalonsCity, chalonsDistrict, 0);
        var equip6 = initEquipment("C-762", ouvrage, agglo, fagniereCity, fagniereDistrict, 1);
        var equip7 = initEquipment("C-763", ouvrage, agglo, fagniereCity, fagniereDistrict, 0);

        var service1 = new Service(UUID.randomUUID(), "DI1234", Instant.now(), Instant.now(), Instant.now(), Instant.now(),
                Instant.now(), equip3, domain, ASKED, prev);
        em.persist(service1);

        var service2 = new Service(UUID.randomUUID(), "DI5678@1223", Instant.now(), Instant.now(), Instant.now(), Instant.now(),
                Instant.now(), equip5, domain, IN_PROGRESS, cura);

        em.persist(service2);

        var asked1 = new AskedService(UUID.randomUUID(), Instant.now(), Status.IN_PROGRESS, "service 1");
        var asked2 = new AskedService(UUID.randomUUID(), Instant.now(), Status.NEW, "service 2");
        var asked3 = new AskedService(UUID.randomUUID(), Instant.now(), Status.DONE, "service 3");
        var asked4 = new AskedService(UUID.randomUUID(), Instant.now(), Status.IN_PROGRESS, "service 4");
        var asked5 = new AskedService(UUID.randomUUID(), Instant.now(), Status.NEW, "service 5");
        var asked6 = new AskedService(UUID.randomUUID(), Instant.now(), Status.DONE, "service 6");

        procedure1 = initProcedure("procedure1", List.of(asked1, asked2));
        procedure2 = initProcedure("procedure2", List.of(asked3, asked4, asked5));
        procedure3 = initProcedure("procedure3", List.of(asked6));

        initOperatorEvent("operator1", Category.OPERATOR, Criticality.LOW, Status.NEW, null, equip1);
        initOperatorEvent("manfestation1", Category.MANIFESTATION, Criticality.MEDIUM, Status.IN_PROGRESS, procedure1,
                equip2);
        initReportEvent("report1", Criticality.LOW, Status.NEW, null, equip6);
        initReportEvent("report2", Criticality.HIGH, Status.IN_PROGRESS, procedure3, equip3);
        initReportEvent("report3", Criticality.MEDIUM, Status.DONE, procedure2, equip6);
        initAlertEvent("malfunction1", Category.MALFUNCTION, Criticality.LOW, Status.NEW, procedure3, equip4);
        initAlertEvent("limit1", Category.LIMIT, Criticality.LOW, Status.NEW, null, equip7);
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

    public Procedure initProcedure(String name, List<AskedService> services) {
        var procedure = new Procedure(UUID.randomUUID(), name);
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

    public void initReportEvent(String name, Criticality criticality, Status status, Procedure procedure,
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

    public void initAlertEvent(String name, Category category, Criticality criticality, Status status,
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
    public void persistDoneService(String externalId, Instant closeDate, Equipment equip, boolean isCura) {
        var service = new Service(UUID.randomUUID(), externalId, Instant.now(), Instant.now(), Instant.now(), Instant.now(),
                closeDate, equip, domain, DONE, isCura ? cura : prev);
        em.persist(service);
    }

    private Equipment initEquipment(String name, Family family, EquipmentEntity entity, City city, District district,
            int managed) {
        var equipment = new Equipment(UUID.randomUUID());
        equipment.setExternalId(name);
        equipment.setName(name);
        equipment.setCode(name);
        equipment.setAddress("address");
        equipment.setFamily(family);
        equipment.setEntity(entity);
        equipment.setDomain(domain);
        equipment.setCity(city);
        equipment.setDistrict(district);
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
