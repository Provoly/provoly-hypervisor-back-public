package com.provoly.equipmentEnriched;

import com.provoly.event.Criticality;

public record CondensedEvent(String category, Criticality criticality) {
}
