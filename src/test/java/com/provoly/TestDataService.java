package com.provoly;

import static com.provoly.event.EventMapper.DEFAULT_SOURCE;
import static com.provoly.service.ServiceStatus.ASKED;
import static com.provoly.service.ServiceStatus.DONE;
import static com.provoly.service.ServiceStatus.IN_PROGRESS;

import java.time.Instant;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import com.provoly.action.AskedService;
import com.provoly.comment.Comment;
import com.provoly.equipment.*;
import com.provoly.event.*;
import com.provoly.event.dto.EventWriteDto;
import com.provoly.model.ProcedureModel;
import com.provoly.procedure.Procedure;
import com.provoly.service.Service;
import com.provoly.service.ServiceCategory;
import com.provoly.service.ServiceDatabaseReader;
import com.provoly.user.User;
import com.provoly.user.UserDatabaseReader;

@ApplicationScoped
public class TestDataService {
    private EntityManager em;
    private EquipmentDatabaseReader equipmentDatabaseReader;
    private EventDatabaseReader eventDatabaseReader;
    private ServiceDatabaseReader serviceDatabaseReader;
    private UserDatabaseReader userDatabaseReader;
    private Random rand = new Random();
    private Map<String, Category> categories;
    private Procedure procedure1, procedure3;
    private Event event1, doneEvent, associatedEvent, externalEvent;
    private Domain domainEP, domainVP;
    private ServiceCategory prev;
    private ServiceCategory cura;
    private User user;

    public TestDataService(EntityManager em, EquipmentDatabaseReader equipmentDatabaseReader,
            EventDatabaseReader eventDatabaseReader,
            ServiceDatabaseReader serviceDatabaseReader, UserDatabaseReader userDatabaseReader) {
        this.em = em;
        this.equipmentDatabaseReader = equipmentDatabaseReader;
        this.eventDatabaseReader = eventDatabaseReader;
        this.serviceDatabaseReader = serviceDatabaseReader;
        this.userDatabaseReader = userDatabaseReader;
    }

    @Transactional
    public void initUser() {
        user = new User(UUID.randomUUID(), "reader", "name");
        userDatabaseReader.saveUser(user);
    }

    @Transactional
    public void init() {
        initUser();
        categories = eventDatabaseReader.getCategoryOrSubCategories()
                .stream()
                .collect(Collectors.toMap(Category::getCode, c -> c));

        domainEP = equipmentDatabaseReader.getDomainByCode("EP");
        domainVP = equipmentDatabaseReader.getDomainByCode("VP");
        prev = serviceDatabaseReader.getServiceCategoryByCode("PREV").get();
        cura = serviceDatabaseReader.getServiceCategoryByCode("CURA").get();

        var chalonsCity = equipmentDatabaseReader.getCityByCode("CHALONS").get();
        var chalonsDistrict = equipmentDatabaseReader.getDistrictByCode("CENTRE").get();

        var fagniereCity = equipmentDatabaseReader.getCityByCode("FAGNIERES").get();
        var fagniereDistrict = equipmentDatabaseReader.getDistrictByCode("FAGNIERES").get();

        var armoire = equipmentDatabaseReader.getFamilyByCode("EP_ARMOIRE").get();
        var foyerLumineux = equipmentDatabaseReader.getFamilyByCode("EP_FOYER_LUMINEUX").get();
        var ouvrage = equipmentDatabaseReader.getFamilyByCode("EP_OUVRAGE").get();
        var camera = equipmentDatabaseReader.getFamilyByCode("VP_CAM").get();

        var chalons = equipmentDatabaseReader.getEquipmentEntityByCode("CHALONS-COMMUN").get();
        var agglo = equipmentDatabaseReader.getEquipmentEntityByCode("AGGLO-COMMUN").get();
        var fagnieres = equipmentDatabaseReader.getEquipmentEntityByCode("FAGNIERES-COMMUN").get();
        var stm = equipmentDatabaseReader.getEquipmentEntityByCode("SAINT-MARTIN-COMMUN").get();

        var equip1 = initEquipment("P-1000", foyerLumineux, fagnieres, fagniereCity, fagniereDistrict, 0, domainEP);
        var equip2 = initEquipment("A-230", armoire, stm, chalonsCity, chalonsDistrict, 1, domainEP);
        var equip3 = initEquipment("A-4901", armoire, agglo, chalonsCity, chalonsDistrict, 0, domainEP);
        var equip4 = initEquipment("C-1034", foyerLumineux, agglo, chalonsCity, chalonsDistrict, 1, domainEP);
        var equip5 = initEquipment("C-7614", ouvrage, chalons, chalonsCity, chalonsDistrict, 0, domainEP);
        var equip6 = initEquipment("C-762", ouvrage, agglo, fagniereCity, fagniereDistrict, 1, domainEP);
        var equip7 = initEquipment("C-763", ouvrage, agglo, fagniereCity, fagniereDistrict, 0, domainEP);
        initEquipment("camera1", camera, agglo, fagniereCity, fagniereDistrict, 1, domainVP);
        initEquipment("camera2", camera, chalons, chalonsCity, chalonsDistrict, 1, domainVP);

        var service1 = new Service(UUID.randomUUID(), "DI1234", Instant.now(), Instant.now(), Instant.now(), Instant.now(),
                Instant.now(), equip3, domainEP, ASKED, prev);
        em.persist(service1);

        var service2 = new Service(UUID.randomUUID(), "DI5678@1223", Instant.now(), Instant.now(), Instant.now(), Instant.now(),
                Instant.now(), equip5, domainEP, IN_PROGRESS, cura);

        em.persist(service2);

        var asked1 = new AskedService(UUID.randomUUID(), Status.IN_PROGRESS, "service 1", 1);
        var asked2 = new AskedService(UUID.randomUUID(), Status.NEW, "service 2", 2);
        var asked6 = new AskedService(UUID.randomUUID(), Status.DONE, "service 6", 3, "DI1234");

        event1 = initEvent("operator1", categories.get("MANIFESTATION"), Criticality.LOW, Status.NEW, equip1, null);
        associatedEvent = initEvent("manfestation1", categories.get("MANIFESTATION"), Criticality.MEDIUM, Status.IN_PROGRESS,
                equip2, null);
        initEvent("report1", categories.get("OUTOFORDER"), Criticality.LOW, Status.NEW, equip6, "citylinx");
        externalEvent = initEvent("report2", categories.get("OUTOFORDER"), Criticality.HIGH, Status.IN_PROGRESS, equip3,
                "wintics");
        doneEvent = initEvent("report3", categories.get("OUTOFORDER"), Criticality.MEDIUM, Status.DONE, equip6, null);
        var event6 = initEvent("malfunction1", categories.get("OUTOFORDER"), Criticality.LOW, Status.IN_PROGRESS, equip4,
                "source");
        initEvent("limit1", categories.get("LIMIT"), Criticality.LOW, Status.NEW, equip7, "source");

        procedure1 = initProcedure("procedure1", List.of(asked1, asked2), List.of(associatedEvent));
        procedure3 = initProcedure("procedure3", List.of(asked6), List.of(externalEvent, event6));

        var procedureModel1 = new ProcedureModel("c'est le MOdèl", "flora", "desc", domainEP, List.of());
        var procedureModel2 = new ProcedureModel("ç'est le model1", "flora", "desc", domainVP, List.of());
        var procedureModel3 = new ProcedureModel("flora model2", "stella", "desc", domainEP, List.of());

        em.persist(procedureModel1);
        em.persist(procedureModel2);
        em.persist(procedureModel3);

        procedureModel3.incrementUseCount();
        procedureModel1.incrementUseCount();
        procedureModel1.incrementUseCount();
        procedureModel2.incrementUseCount();
    }

    @Transactional
    public void clean() {
        removeEntities(Event.class);
        removeEntities(Service.class);
        removeEntities(Equipment.class);
        removeEntities(Procedure.class);
        removeEntities(ProcedureModel.class);
        removeEntities(Comment.class);
        removeEntities(User.class);
    }

    public User getUser() {
        return user;
    }

    public Procedure getProcedure1() {
        return procedure1;
    }

    public Procedure getProcedure3() {
        return procedure3;
    }

    public Category getCategory(String code) {
        return categories.get(code);
    }

    public Event getEvent1() {
        return event1;
    }

    public Event getDoneEvent() {
        return doneEvent;
    }

    public Event getAssociatedEvent() {
        return associatedEvent;
    }

    public Event getExternalEvent() {
        return externalEvent;
    }

    public EventWriteDto buildEvent(String name, String category, Criticality criticality, boolean isWithDate) {
        return new EventWriteDto(null,
                name,
                "desc",
                criticality,
                category,
                null,
                null,
                null,
                null,
                isWithDate ? Instant.now() : null,
                isWithDate ? Instant.now().minusMillis(1000) : null,
                null);
    }

    public EventWriteDto buildExternalEvent(String name, String category, Criticality criticality, boolean isWithDate,
            UUID equipmentId, String source) {
        return new EventWriteDto(null,
                name,
                "desc",
                criticality,
                category,
                null,
                null,
                equipmentId,
                null,
                isWithDate ? Instant.now() : null,
                isWithDate ? Instant.now().minusMillis(1000) : null,
                source);
    }

    @Transactional
    public void persistDoneService(String externalId, Instant closeDate, Equipment equip, boolean isCura, boolean isVp) {
        var service = new Service(UUID.randomUUID(), externalId, Instant.now(), Instant.now(), Instant.now(), Instant.now(),
                closeDate, equip, isVp ? domainVP : domainEP, DONE, isCura ? cura : prev);
        em.persist(service);
    }

    @Transactional
    public void persistDoneService(String externalId, Instant closeDate, Equipment equip, boolean isCura) {
        persistDoneService(externalId, closeDate, equip, isCura, false);
    }

    @Transactional
    public Procedure initProcedure(String name, List<AskedService> services, List<Event> events) {
        var procedure = new Procedure(name, "desc");
        for (var service : services) {
            procedure.addAction(service);
        }
        for (var event : events) {
            procedure.addEvent(event);
        }
        em.persist(procedure);
        return procedure;
    }

    private Event initEvent(String name,
            Category category,
            Criticality criticality,
            Status status,
            Equipment equipment,
            String externalRef) {

        Event event;
        if (externalRef != null) {
            event = new Event(UUID.randomUUID().toString());
        } else {
            event = new Event();
        }

        event.setName(name);
        event.setAddress("event address");
        event.setDescription("description");
        event.setCategory(category);
        event.setCriticality(criticality);
        event.setStatus(status);
        event.setDomain(domainEP);
        event.setCreationDate(Instant.now());
        if (status == Status.DONE) {
            event.setCloseDate(randomInstantBetweenNowAndAMonthLater());
        }

        if (equipment != null) {
            event.setEquipment(equipment);
        }
        if (category.getCode().equals("MANIFESTATION")) {
            event.setStartDate(Instant.now());
            event.setEndDate(Instant.now());
        }
        event.setExternalSourceRef(externalRef == null ? DEFAULT_SOURCE : externalRef);
        ;
        em.persist(event);
        return event;
    }

    private Equipment initEquipment(String name, Family family, EquipmentEntity entity, City city, District district,
            int managed, Domain domain) {
        var equipment = new Equipment(UUID.randomUUID());
        equipment.setExternalId(Map.of(name, name));
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
