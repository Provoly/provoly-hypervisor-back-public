package com.provoly.service.coswin;

public record ServiceTypeReadDto(String type,
        String domain,
        Integer gti,
        Integer gtr,
        Integer gtrp) {
}
