package com.provoly.action.dto;

public class PhoneActionReadDto extends ActionReadDto {
    private final String name;
    private final String number;

    public PhoneActionReadDto(ActionReadDto dto, String name, String number) {
        super(dto);
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }
}
