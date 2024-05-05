package com.capstone.emergency.pharmacy.core.item.repository.model;

import lombok.Getter;

@Getter
public enum Unit {
    // Weight
    G("gram"),
    MG("milligram"),
    UG("microgram"),
    NG("nanogram"),

    // Volume
    L("liter"),
    ML("milliliter"),
    UL("microliter"),
    NL("nanoliter");

    private final String value;

    Unit(String value) {
        this.value = value;
    }
}