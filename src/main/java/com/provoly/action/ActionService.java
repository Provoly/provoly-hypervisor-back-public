package com.provoly.action;

import static com.provoly.action.ActionType.isDefaultActionType;

import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.provoly.action.dto.ActionWriteDto;
import com.provoly.model.ProcedureModel;
import com.provoly.procedure.Procedure;

@ApplicationScoped
public class ActionService {
    private final ActionDatabaseReader databaseReader;
    private final ActionMapper actionMapper;

    public ActionService(ActionDatabaseReader databaseReader, ActionMapper actionMapper) {
        this.databaseReader = databaseReader;
        this.actionMapper = actionMapper;
    }

    @Transactional
    public Optional<Action> getActionById(UUID id) {
        return databaseReader.getActionById(id);
    }

    @Transactional
    public void saveActionForModel(ActionWriteDto dto, ProcedureModel model) {
        var newAction = buildAction(dto);
        model.addAction(newAction);
        databaseReader.saveAction(newAction);
    }

    @Transactional
    public void saveActionForInstance(ActionWriteDto dto, Procedure instance) {
        var newAction = buildAction(dto);
        instance.addAction(newAction);
        databaseReader.saveAction(newAction);
    }

    private Action buildAction(ActionWriteDto dto) {
        if (!databaseReader.isCustomActionType(dto.getType()) && !isDefaultActionType(dto.getType())) {
            throw new IllegalArgumentException("Invalid action type: " + dto.getType());
        }
        return actionMapper.mapToActionEntity(dto);
    }
}
