package com.provoly.service;

import java.util.UUID;

public record ExternalServiceWriteDto(String name,
                                      UUID equipment,
                                      String priority,
                                      String type,
                                      String description,
                                      String domain) {
}
