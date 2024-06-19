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

        initOperatorEvent("operator1", Category.OPERATOR, Criticality.LOW, Status.NEW, equip1);
        var event2 = initOperatorEvent("manfestation1", Category.MANIFESTATION, Criticality.MEDIUM, Status.IN_PROGRESS,
                equip2);
        initReportEvent("report1", Criticality.LOW, Status.NEW, equip6);
        var event4 = initReportEvent("report2", Criticality.HIGH, Status.IN_PROGRESS, equip3);
        var event5 = initReportEvent("report3", Criticality.MEDIUM, Status.DONE, equip6);
        var event6 = initAlertEvent("malfunction1", Category.MALFUNCTION, Criticality.LOW, Status.NEW, equip4);
        initAlertEvent("limit1", Category.LIMIT, Criticality.LOW, Status.NEW, equip7);

        procedure1 = initProcedure("procedure1", List.of(asked1, asked2), List.of(event2));
        procedure2 = initProcedure("procedure2", List.of(asked3, asked4, asked5), List.of(event5));
        procedure3 = initProcedure("procedure3", List.of(asked6), List.of(event4, event6));
    }

    @Transactional
    public void clean() {
        removeEntities(Event.class);
        removeEntities(Service.class);
        removeEntities(Equipment.class);
        removeEntities(Procedure.class);
    }

    public Integer getProcedureId1() {
        return procedure1.getId();
    }

    public Integer getProcedureId3() {
        return procedure3.getId();
    }

    @Transactional
    public void persistDoneService(String externalId, Instant closeDate, Equipment equip, boolean isCura) {
        var service = new Service(UUID.randomUUID(), externalId, Instant.now(), Instant.now(), Instant.now(), Instant.now(),
                closeDate, equip, domain, DONE, isCura ? cura : prev);
        em.persist(service);
    }

    private Event initOperatorEvent(String name, Category category, Criticality criticality, Status status,
            Equipment equipment) {
        var event = (EventOperator) initEvent(EventType.OPERATOR, name, category, criticality, status, equipment);
        if (event.getCategory() == Category.MANIFESTATION) {
            event.setStartDate(Instant.now());
            event.setEndDate(Instant.now());
        }
        em.persist(event);
        return event;
    }

    private Event initAlertEvent(String name, Category category, Criticality criticality, Status status,
            Equipment equipment) {
        var event = (EventAlert) initEvent(EventType.ALERT, name, category, criticality, status, equipment);
        event.setExternalSourceRef("external_source_ref");
        em.persist(event);
        return event;
    }

    private Event initReportEvent(String name, Criticality criticality, Status status,
            Equipment equipment) {
        var event = (EventReport) initEvent(EventType.REPORT, name, Category.REPORT, criticality, status, equipment);
        event.setExternalSourceRef("external_source_ref");
        em.persist(event);
        return event;
    }

    private Event initEvent(EventType type, String name, Category category, Criticality criticality, Status status,
            Equipment equipment) {
        var event = switch (type) {
            case ALERT -> new EventAlert();
            case REPORT -> new EventReport();
            case OPERATOR -> new EventOperator();
        };

        event.setName(name);
        event.setAddress("event address");
        event.setDescription("description");
        event.setCategory(category);
        event.setCriticality(criticality);
        event.setStatus(status);
        event.setDomain(domain);
        if (status == Status.DONE) {
            event.setCloseDate(randomInstantBetweenNowAndAMonthLater());
        }

        if (equipment != null) {
            event.setEquipment(equipment);
        }
        return event;
    }

    @Transactional
    public Procedure initProcedure(String name, List<AskedService> services, List<Event> events) {
        var procedure = new Procedure(name);
        for (var service : services) {
            procedure.addAction(service);
        }
        for (var event : events) {
            procedure.addEvent(event);
        }
        em.persist(procedure);
        return procedure;
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
