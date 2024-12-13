package de.kulose.musicquizhost.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
public enum Status {
    OPEN("open"),
    ACTIVE("active"),
    CLOSED("closed");

    private final String text;
}
