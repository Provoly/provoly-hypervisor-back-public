package com.provoly.action.dto;

public class OtherActionReadDto extends ActionReadDto {
    private final String name;

    public OtherActionReadDto(ActionReadDto dto, String name) {
        super(dto);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
