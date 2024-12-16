package com.provoly.equipmentenriched;

import com.provoly.event.Criticality;

public record CondensedEvent(String category, Criticality criticality) {
}
