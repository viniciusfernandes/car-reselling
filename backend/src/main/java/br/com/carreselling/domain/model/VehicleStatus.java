package br.com.carreselling.domain.model;

public enum VehicleStatus {
    IN_LOT,
    IN_SERVICE,
    READY_FOR_DISTRIBUTION,
    DISTRIBUTED,
    SOLD;

    public boolean alreadyDistribuited() {
        return this == DISTRIBUTED || this == SOLD;
    }
}
