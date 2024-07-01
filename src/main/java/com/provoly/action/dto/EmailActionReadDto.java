package com.provoly.action.dto;

public class EmailActionReadDto extends ActionReadDto {
    private final String name;
    private final String email;

    public EmailActionReadDto(ActionReadDto dto, String name, String email) {
        super(dto);
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
