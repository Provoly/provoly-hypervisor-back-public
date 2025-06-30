package com.provoly.equipmentenriched;

import com.provoly.event.Criticality;

import com.fasterxml.jackson.annotation.JsonCreator;

public record CondensedEvent(String category, Criticality criticality) {
}
