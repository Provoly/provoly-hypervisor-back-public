package com.provoly.model;

import static com.provoly.event.Status.DONE;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;

import com.provoly.action.Action;
import com.provoly.action.ActionService;
import com.provoly.action.dto.ActionWriteDto;
import com.provoly.error.AlreadyExistsException;
import com.provoly.event.Event;
import com.provoly.event.EventService;
import com.provoly.event.SortOrder;
import com.provoly.procedure.Procedure;
import com.provoly.procedure.ProcedureService;

import org.jboss.logging.Logger;

@ApplicationScoped
public class ProcedureModelService {

    private final Logger logger;
    private final ProcedureModelDatabaseReader databaseReader;
    private final ProcedureModelMapper procedureModelMapper;
    private final ProcedureService procedureService;
    private final EventService eventService;
    private final ActionService actionService;

    public ProcedureModelService(Logger logger, ProcedureModelDatabaseReader databaseReader,
            ProcedureModelMapper procedureModelMapper, ProcedureService procedureService, EventService eventService,
            ActionService actionService) {
        this.logger = logger;
        this.databaseReader = databaseReader;
        this.procedureModelMapper = procedureModelMapper;
        this.procedureService = procedureService;
        this.eventService = eventService;
        this.actionService = actionService;
    }

    @Transactional
    public ProcedureModel getProcedureModelDetails(Integer id) {
        logger.debugf("Get procedure model detail with id %s", id);
        return databaseReader.getProcedureModelById(id);
    }

    @Transactional
    public Collection<ProcedureModel> getProceduresModel(int page, int pageSize, String sort, String order,
            List<String> domains, String search) {
        logger.infof("""
                Get procedures models with from page %s and size %s with :
                domain : %s
                search on procedures model that contains : %s
                sort by %s
                """.formatted(page, pageSize, domains, search, sort));

        if (domains.stream().anyMatch(String::isEmpty)) {
            logger.debugf(
                    "Filter on empty domain will return empty procedure model list because domain is a required property");
            return List.of();
        }

        var domainEntities = domains.stream()
                .map(databaseReader::getDomainByCode)
                .toList();
        return databaseReader.getProcedureModels(page,
                pageSize,
                ProcedureModelSort.fromName(sort),
                SortOrder.fromString(order),
                domainEntities,
                search);
    }

    @Transactional
    public ProcedureModel saveProcedureModel(ProcedureModelWriteDto dto) {
        logger.infof("Save procedure model with name %s and its %s actions", dto.name(), dto.actions().size());
        if (databaseReader.isProcedureModelWithNameExists(dto.name())) {
            throw new AlreadyExistsException("Procedure model with name '%s' already exists".formatted(dto.name()));
        }
        var model = new ProcedureModel(dto.creator());
        procedureModelMapper.updateProcedureModel(model, dto);
        databaseReader.saveProcedureModel(model);

        addActionsForModel(dto.actions(), model);

        logger.debugf("Procedure model %s is saved".formatted(model.getId()));
        return model;
    }

    @Transactional
    public void updateProcedureModel(Integer id, ProcedureModelWriteDto dto) {
        logger.infof("Update procedure model %s and its %s actions", String.valueOf(id), dto.actions().size());
        var model = databaseReader.getProcedureModelById(id);
        if (!dto.creator().equals(model.getCreator())) {
            throw new IllegalArgumentException(
                    "It's not possible to update Procedure model creator for procedure %s".formatted(model.getId()));
        }

        if (!model.getName().equals(dto.name()) && databaseReader.isProcedureModelWithNameExists(dto.name())) {
            throw new AlreadyExistsException("Procedure model with name '%s' already exists".formatted(dto.name()));
        }

        procedureModelMapper.updateProcedureModel(model, dto);

        var currentActionIds = model.getActions().stream().map(Action::getId).collect(Collectors.toSet());
        var newActionIds = dto.actions().stream().map(ActionWriteDto::getId).collect(Collectors.toSet());

        currentActionIds.removeAll(newActionIds);
        if (!currentActionIds.isEmpty()) {
            logger.debugf("delete actions with id %s", currentActionIds);
            for (var actionId : currentActionIds) {
                model.getAction(actionId).ifPresent(model::removeAction);
            }
        }

        addActionsForModel(dto.actions(), model);
        logger.debugf("Procedure model %s is updated".formatted(model.getId()));

    }

    @Transactional
    public Procedure associateProcedureModelToEvents(Integer id, Collection<Integer> eventIds) {
        logger.infof("Associate procedure model %s to events %", String.valueOf(id), eventIds);
        var model = databaseReader.getProcedureModelById(id);

        logger.debugf("Retrieve events to associate them to procedure");
        var events = eventIds
                .stream()
                .map(eventId -> {
                    var event = eventService.getEventDetails(eventId);
                    checkEventCanBeAssociated(event);
                    return event;
                });

        var procedure = procedureService.instantiateProcedureWithModelAndEvents(model, events);
        model.incrementUseCount();
        return procedure;
    }

    private void checkEventCanBeAssociated(Event event) {
        if (event.getProcedure() != null) {
            throw new ForbiddenException("Event %s is already associated to a procedure".formatted(event.getId()));
        }
        if (event.getStatus() == DONE) {
            throw new ForbiddenException("Event %s can't be done".formatted(event.getId()));
        }
    }

    private void addActionsForModel(Collection<ActionWriteDto> actions, ProcedureModel model) {
        logger.infof("Save or update %s actions for model %s", actions.size(), model.getId());
        int index = 0;
        for (var dtoAction : actions) {
            actionService.saveActionForModel(dtoAction, model, index++);
        }
    }
}
