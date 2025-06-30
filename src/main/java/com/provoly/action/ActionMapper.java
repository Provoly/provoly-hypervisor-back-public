package com.provoly.action;

import java.util.Collection;

import jakarta.enterprise.context.ApplicationScoped;

import com.provoly.action.dto.*;
import com.provoly.comment.CommentMapper;
import com.provoly.event.Status;
import com.provoly.service.ServiceService;

@ApplicationScoped
public class ActionMapper {

    private final CommentMapper commentMapper;
    private final ServiceService serviceService;

    public ActionMapper(CommentMapper commentMapper, ServiceService serviceService) {
        this.commentMapper = commentMapper;
        this.serviceService = serviceService;
    }

    public ActionReadDto mapToActionReadDto(Action action) {
        var actionDto = new ActionReadDto(
                action.getId(),
                action.getType(),
                action.getStatus(),
                action.getLastModificationDate(),
                commentMapper.mapLastCommentToDto(action.getComments()),
                action.getComments().size());

        return switch (action) {
            case EmailAction a -> new EmailActionReadDto(actionDto, a.getName(), a.getEmail());
            case OtherAction a -> new OtherActionReadDto(actionDto, a.getName());
            case AskedService a -> buildAskedServiceReadDto(a, actionDto);
            case AlertElected a -> buildAlerteElectedReadDto(a, actionDto);
            case SmsAction a -> new PhoneActionReadDto(actionDto, a.getName(), a.getNumber());
            case PhoneAction a -> new PhoneActionReadDto(actionDto, a.getName(), a.getNumber());
            default -> actionDto;
        };
    }

    public Collection<ActionReadDto> mapToActionReadDto(Collection<Action> actions) {
        return actions.stream().map(this::mapToActionReadDto).toList();
    }

    public void updateAction(ActionWriteDto actionDto, Action action, int index) {
        action.setId(actionDto.getId());
        action.setOrder(index);
        action.setStatus(actionDto.getStatus() == null ? Status.NEW : actionDto.getStatus());
        action.setType(actionDto.getType());

        switch (actionDto) {
            case EmailActionWriteDto dto -> {
                ((EmailAction) action).setName(dto.getName());
                ((EmailAction) action).setEmail(dto.getEmail());
            }
            case OtherActionWriteDto dto -> ((OtherAction) action).setName(dto.getName());
            case AskedServiceWriteDto dto -> {
                ((AskedService) action).setName(dto.getName());
                ((AskedService) action).setServiceExternalId(dto.getServiceExternalId());
            }
            case AlertElectedWriteDto dto -> {
                ((AlertElected) action).setName(dto.getName());
                ((AlertElected) action).setServiceExternalId(dto.getServiceExternalId());
            }
            case PhoneActionWriteDto dto -> {
                if (ActionType.valueOf(dto.getType()) == ActionType.SMS) {
                    ((SmsAction) action).setName(dto.getName());
                    ((SmsAction) action).setNumber(dto.getNumber());
                    return;
                }
                ((PhoneAction) action).setName(dto.getName());
                ((PhoneAction) action).setNumber(dto.getNumber());
            }
            default -> {
            }
        }
    }

    public Action duplicateAction(Action action) {
        return switch (action) {
            case EmailAction a -> new EmailAction(a.getOrder(), a.getName(), a.getEmail());
            case OtherAction a -> new OtherAction(a.getOrder(), a.getName());
            case AskedService a -> new AskedService(a.getOrder(), a.getName(), a.getServiceExternalId());
            case SmsAction a -> new SmsAction(a.getOrder(), a.getName(), a.getNumber());
            case PhoneAction a -> new PhoneAction(a.getOrder(), a.getName(), a.getNumber());
            default -> new Action(action.getOrder(), action.getType());
        };
    }

    private AskedServiceReadDto buildAskedServiceReadDto(AskedService a, ActionReadDto actionDto) {
        var service = serviceService.getServiceByExternalId(a.getServiceExternalId());
        var status = service != null ? service.getStatus() : null;
        var additionalInfo = service != null ? service.getAdditionalInfo() : null;
        return new AskedServiceReadDto(actionDto, a.getName(), a.getServiceExternalId(), additionalInfo, status);
    }

    private AlertElectedReadDto buildAlerteElectedReadDto(AlertElected a, ActionReadDto actionDto) {
        var service = serviceService.getServiceByExternalId(a.getServiceExternalId());
        var status = service != null ? service.getStatus() : null;
        var additionalInfo = service != null ? service.getAdditionalInfo() : null;
        return new AlertElectedReadDto(actionDto, a.getName(), a.getServiceExternalId(), additionalInfo, status);
    }
}
