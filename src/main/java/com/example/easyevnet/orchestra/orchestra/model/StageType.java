package com.example.easyevnet.orchestra.orchestra.model;

public enum StageType {

    ORDERED,
    DEFAULT,
    BRAKING;

    boolean equals(String name) {
        return this.name().equals(name);
    }
}
