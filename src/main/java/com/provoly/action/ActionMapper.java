package com.provoly.action;

import java.util.Collection;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

import com.provoly.action.dto.*;
import com.provoly.event.Status;

@ApplicationScoped
public class ActionMapper {

    public ActionReadDto mapToActionReadDto(Action action) {
        var actionDto = new ActionReadDto(
                action.getId(),
                action.getType(),
                action.getStatus(),
                action.getLastModificationDate());

        return switch (action) {
            case EmailAction a -> new EmailActionReadDto(actionDto, a.getName(), a.getEmail());
            case OtherAction a -> new OtherActionReadDto(actionDto, a.getName());
            case AskedService a -> new AskedServiceReadDto(actionDto, a.getName(), a.getServiceExternalId());
            case SmsAction a -> new PhoneActionReadDto(actionDto, a.getName(), a.getNumber());
            case PhoneAction a -> new PhoneActionReadDto(actionDto, a.getName(), a.getNumber());
            default -> actionDto;
        };
    }

    public Collection<ActionReadDto> mapToActionReadDto(Collection<Action> actions) {
        return actions.stream().map(this::mapToActionReadDto).toList();
    }

    public Action mapToActionEntity(ActionWriteDto actionDto) {
        var action = new Action(actionDto.getId(), actionDto.getType(),
                actionDto.getStatus() == null ? Status.NEW : actionDto.getStatus());
        return switch (actionDto) {
            case EmailActionWriteDto dto -> new EmailAction(action, dto.getName(), dto.getEmail());
            case OtherActionWriteDto dto -> new OtherAction(action, dto.getName());
            case AskedServiceWriteDto dto -> new AskedService(action, dto.getName(), dto.getServiceId());
            case PhoneActionWriteDto dto -> {
                if (ActionType.valueOf(dto.getType()) == ActionType.SMS) {
                    yield new SmsAction(action.getId(), action.getType(), action.getStatus(), dto.getName(), dto.getNumber());
                }
                yield new PhoneAction(action.getId(), action.getType(), action.getStatus(), dto.getName(), dto.getNumber());
            }
            default -> action;
        };
    }

    public Action duplicateAction(Action action) {
        return switch (action) {
            case EmailAction a -> new EmailAction(a.getName(), a.getEmail());
            case OtherAction a -> new OtherAction(a.getName());
            case AskedService a -> new AskedService(a.getName(), a.getServiceExternalId());
            case SmsAction a -> new SmsAction(UUID.randomUUID(), ActionType.SMS.name(), Status.NEW, a.getName(), a.getNumber());
            case PhoneAction a -> new PhoneAction(a.getName(), a.getNumber());
            default -> new Action(action.getType());
        };
    }
}
