package com.provoly.action;

import java.util.Collection;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ActionMapper {

    public ActionReadDto mapToActionReadDto(Action action) {
        return new ActionReadDto(
                action.getId(),
                action.getName(),
                action.getType(),
                action.getStatus(),
                action.getLastModificationDate());
    }

    public Collection<ActionReadDto> mapToActionReadDto(Collection<Action> actions) {
        return actions.stream().map(this::mapToActionReadDto).toList();
    }

    public void updateAction(Action action, ActionWriteDto dto) {
        action.setName(dto.name());
        action.setStatus(dto.status());
    }
}
