package com.provoly.procedure;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.provoly.action.Action;
import com.provoly.action.ActionMapper;
import com.provoly.action.ActionService;
import com.provoly.action.dto.ActionWriteDto;
import com.provoly.event.Event;
import com.provoly.event.EventService;
import com.provoly.model.ProcedureModel;
import com.provoly.user.Role;

import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.SecurityIdentity;

import org.jboss.logging.Logger;

@ApplicationScoped
public class ProcedureService {
    private final ProcedureDatabaseReader databaseReader;
    private final EventService eventService;
    private final ActionService actionService;
    private final ActionMapper actionMapper;
    private final Logger logger;
    private final SecurityIdentity securityIdentity;

    public ProcedureService(ProcedureDatabaseReader databaseReader, EventService eventService, ActionService actionService,
            ActionMapper actionMapper,
            Logger logger, SecurityIdentity securityIdentity) {
        this.databaseReader = databaseReader;
        this.eventService = eventService;
        this.actionService = actionService;
        this.actionMapper = actionMapper;
        this.logger = logger;
        this.securityIdentity = securityIdentity;
    }

    @Transactional
    public Procedure getProcedureDetails(Integer id) {
        logger.debugf("Get procedure detail with id %s", id);
        return databaseReader.getProcedureById(id);
    }

    @Transactional
    public void closeAllProcedureEventsById(Integer id) {
        logger.debugf("Close all procedure events with id %s", id);
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
        logger.debugf("Procedure %s is instantiated".formatted(procedure.getId()));
        return procedure;
    }

    @Transactional
    public void deleteProcedure(Integer id) {
        var procedure = databaseReader.getProcedureById(id);
        procedure.dissociateEvents();
        databaseReader.removeProcedure(procedure);
    }

    @Transactional
    public Procedure getProcedureFromAction(UUID actionId) {
        return databaseReader.getProcedureForAction(actionId);
    }
}
