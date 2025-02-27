package com.provoly.procedure;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.provoly.action.*;
import com.provoly.service.Service;
import com.provoly.service.ServiceService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.provoly.action.dto.ActionWriteDto;
import com.provoly.event.Event;
import com.provoly.event.EventService;
import com.provoly.event.Status;
import com.provoly.model.ProcedureModel;
import com.provoly.user.Role;

import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.SecurityIdentity;

import org.jboss.logging.Logger;

@ApplicationScoped
public class ProcedureService {
    private final ProcedureDatabaseReader databaseReader;

    private final ActionDatabaseReader actionDatabaseReader;
    private final EventService eventService;
    private final ActionService actionService;
    private final ActionMapper actionMapper;
    private final Logger logger;
    private final SecurityIdentity securityIdentity;

    public ProcedureService(ProcedureDatabaseReader databaseReader, ActionDatabaseReader actionDatabaseReader, EventService eventService, ActionService actionService,
                            ActionMapper actionMapper,
                            Logger logger, SecurityIdentity securityIdentity) {
        this.databaseReader = databaseReader;
        this.actionDatabaseReader = actionDatabaseReader;
        this.eventService = eventService;
        this.actionService = actionService;
        this.actionMapper = actionMapper;
        this.logger = logger;
        this.securityIdentity = securityIdentity;
    }

    @Transactional
    public Procedure getProcedureDetails(Integer id) {
        logger.infof("Get procedure detail with id %s", id);
        return databaseReader.getProcedureById(id);
    }

    @Transactional
    public void closeAllProcedureEventsById(Integer id) {
        logger.infof("Close all procedure events with id %s", id);
        var procedure = getProcedureDetails(id);
        closeAllProcedureEvent(procedure);
    }

    public void closeAllProcedureEvent(Procedure procedure) {
        procedure.getEvents().forEach(eventService::closeEvent);
    }

    @Transactional
    public void updateProcedure(Integer id, ProcedureWriteDto dto) {
        logger.infof("Update procedure %s with its %s actions and %s events", String.valueOf(id), dto.actions().size(),
                dto.events().size());

        var procedure = databaseReader.getProcedureById(id);

        logger.debug("Update events");
        for (var event : dto.events()) {
            eventService.updateEvent(event.getId(), event);
        }

        logger.debug("Update actions");
        var currentActionIds = procedure.getActions().stream().map(Action::getId).collect(Collectors.toSet());
        var newActionIds = dto.actions().stream().map(ActionWriteDto::getId).collect(Collectors.toSet());

        if (!currentActionIds.equals(newActionIds) && !securityIdentity.hasRole(Role.STR_EVENT_PROC_WRITE)) {
            throw new ForbiddenException("Missing permission to add or delete actions.");
        }

        currentActionIds.removeAll(newActionIds);
        if (!currentActionIds.isEmpty()) {
            logger.debugf("Delete actions with id %s", currentActionIds);
            for (var actionId : currentActionIds) {
                procedure.getAction(actionId).ifPresent(procedure::removeAction);
            }
        }

        int index = 0;
        for (var actionDto : dto.actions()) {
            actionService.saveActionForInstance(actionDto, procedure, index++);
        }

        procedure.calculateProgressActions();

        logger.debugf("Procedure %s is updated".formatted(procedure.getId()));
    }

    @Transactional
    public Procedure instantiateProcedureWithModelAndEvents(ProcedureModel model, Stream<Event> events) {
        Procedure procedure = new Procedure(model.getName(), model.getDescription());
        logger.debugf("Add events to procedure");
        events.forEach(procedure::addEvent);

        logger.debugf("Add actions to procedure");
        model.getActions().forEach(action -> procedure.addAction(actionMapper.duplicateAction(action)));

        databaseReader.saveProcedure(procedure);
        logger.infof("Procedure %s is instantiated".formatted(procedure.getId()));
        return procedure;
    }

    @Transactional
    public void deleteProcedure(Integer id) {
        logger.infof("Delete procedure %s and dissociate its events", id);
        var procedure = databaseReader.getProcedureById(id);
        procedure.dissociateEvents();
        for(Action a: procedure.getActions()){
            UUID idAction = a.getId();
            Action asked = actionService.getActionById(idAction);
            if(asked instanceof AskedService){
                ((AskedService) asked).setServiceExternalId(null);
                actionDatabaseReader.saveAction(asked);
            }
        }
        procedure.removeActions();
        databaseReader.removeProcedure(procedure);
    }

    @Transactional
    public Procedure getProcedureFromAction(UUID actionId) {
        logger.debugf("Get procedure with action %s", actionId);
        return databaseReader.getProcedureForAction(actionId);
    }

    @Transactional
    public void addEventToProcedure(Integer procedureId, Integer eventId) {
        logger.infof("Add event ¨%s to procedure %s", eventId, procedureId);
        var procedure = databaseReader.getProcedureById(procedureId);
        if (procedure.getCloseComment() != null) {
            throw new IllegalArgumentException(
                    "Procedure %s is closed, it's not possible to add new events".formatted(procedure.getId()));
        }

        var event = eventService.getEventDetails(eventId);
        if (event.getStatus() != Status.NEW) {
            throw new IllegalArgumentException(
                    "Event status of %s is %s instead of NEW".formatted(eventId, event.getStatus()));
        }
        procedure.addEvent(event);
    }
}
