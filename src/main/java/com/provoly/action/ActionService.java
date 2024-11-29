package com.provoly.action;

import static com.provoly.action.ActionType.isDefaultActionType;

import java.util.NoSuchElementException;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.provoly.action.dto.*;
import com.provoly.event.Status;
import com.provoly.model.ProcedureModel;
import com.provoly.procedure.Procedure;
import com.provoly.user.Role;
import com.provoly.user.UserService;

import io.quarkus.security.ForbiddenException;

import org.jboss.logging.Logger;

@ApplicationScoped
public class ActionService {
    private final Logger logger;
    private final ActionDatabaseReader databaseReader;
    private final ActionMapper actionMapper;
    private final UserService userService;

    public ActionService(Logger logger, ActionDatabaseReader databaseReader, ActionMapper actionMapper,
            UserService userService) {
        this.logger = logger;
        this.databaseReader = databaseReader;
        this.actionMapper = actionMapper;
        this.userService = userService;
    }

    @Transactional
    public void saveActionForModel(ActionWriteDto dto, ProcedureModel model, int index) {
        checkActionType(dto);

        model.getAction(dto.getId())
                .ifPresentOrElse(
                        action -> {
                            logger.debugf("Update model action %s", action.getId());
                            actionMapper.updateAction(dto, action, index);
                        },
                        () -> {
                            var action = buildAction(dto, index);
                            logger.debugf("Save model action %s", action.getId());
                            model.addAction(action);
                            databaseReader.saveAction(action);
                        });
    }

    @Transactional
    public void saveActionForInstance(ActionWriteDto dto, Procedure procedure, int index) {
        checkActionType(dto);

        procedure.getAction(dto.getId())
                .ifPresentOrElse(
                        action -> {
                            if (!userService.hasRole(Role.STR_EVENT_WRITE)
                                    && action.getStatus() != dto.getStatus()
                                    && dto.getStatus() == Status.DONE) {
                                throw new ForbiddenException("Missing permission to update action status.");
                            }
                            logger.debugf("Update instance action %s", action.getId());
                            actionMapper.updateAction(dto, action, index);
                        },
                        () -> {
                            var action = buildAction(dto, index);
                            logger.debugf("Save instance action %s", action.getId());
                            procedure.addAction(action);
                            databaseReader.saveAction(action);
                        });

    }

    @Transactional
    public Action getActionById(UUID id) {
        return databaseReader.getActionById(id)
                .orElseThrow(() -> new NoSuchElementException("Action with id %s not found".formatted(id)));
    }

    private void checkActionType(ActionWriteDto dto) {
        if (!databaseReader.isCustomActionType(dto.getType()) && !isDefaultActionType(dto.getType())) {
            throw new IllegalArgumentException("Invalid action type: " + dto.getType());
        }
    }

    private Action buildAction(ActionWriteDto dto, int index) {
        var action = switch (dto) {
            case EmailActionWriteDto ignored -> new EmailAction();
            case PhoneActionWriteDto d -> {
                if (ActionType.valueOf(d.getType()) == ActionType.SMS) {
                    yield new SmsAction();
                }
                yield new PhoneAction();
            }
            case AskedServiceWriteDto ignored -> new AskedService();
            case OtherActionWriteDto ignored -> new OtherAction();
            default -> new Action();
        };
        actionMapper.updateAction(dto, action, index);
        return action;
    }
}
