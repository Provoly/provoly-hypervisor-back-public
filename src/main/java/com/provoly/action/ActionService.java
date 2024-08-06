package com.provoly.action;

import static com.provoly.action.ActionType.isDefaultActionType;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.provoly.action.dto.*;
import com.provoly.event.Status;
import com.provoly.model.ProcedureModel;
import com.provoly.procedure.Procedure;
import com.provoly.user.Role;

import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.SecurityIdentity;

@ApplicationScoped
public class ActionService {
    private final ActionDatabaseReader databaseReader;
    private final ActionMapper actionMapper;
    private final SecurityIdentity securityIdentity;

    public ActionService(ActionDatabaseReader databaseReader, ActionMapper actionMapper, SecurityIdentity securityIdentity) {
        this.databaseReader = databaseReader;
        this.actionMapper = actionMapper;
        this.securityIdentity = securityIdentity;
    }

    @Transactional
    public void saveActionForModel(ActionWriteDto dto, ProcedureModel model, int index) {
        checkActionType(dto);

        model.getAction(dto.getId())
                .ifPresentOrElse(
                        action -> actionMapper.updateAction(dto, action, index),
                        () -> {
                            var action = buildAction(dto, index);
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
                            if (!securityIdentity.hasRole(Role.STR_EVENT_WRITE)
                                    && action.getStatus() != dto.getStatus()
                                    && dto.getStatus() == Status.DONE) {
                                throw new ForbiddenException("Missing permission to update action status.");
                            }
                            actionMapper.updateAction(dto, action, index);
                        },
                        () -> {
                            var action = buildAction(dto, index);
                            procedure.addAction(action);
                            databaseReader.saveAction(action);
                        });

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
