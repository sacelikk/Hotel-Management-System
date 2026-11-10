package org.apache.maven.archetypes.hotelflow.Model;

import java.time.LocalDate;
import java.time.Month;

public enum Season {
    SPRING("Spring (Mar - May)"),
    SUMMER("Summer (Jun - Aug)"),
    AUTUMN("Autumn (Sep - Nov)"),
    WINTER("Winter (Dec - Feb)"),
    ALL_YEAR("All Year Round");

    private final String displayName;

    Season(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Season getCurrentSeason() {
        return forMonth(LocalDate.now().getMonth());
    }

    public static Season forMonth(Month month) {
        if (month == null) return ALL_YEAR;
        switch (month) {
            case MARCH:
            case APRIL:
            case MAY:
                return SPRING;
            case JUNE:
            case JULY:
            case AUGUST:
                return SUMMER;
            case SEPTEMBER:
            case OCTOBER:
            case NOVEMBER:
                return AUTUMN;
            case DECEMBER:
            case JANUARY:
            case FEBRUARY:
            default:
                return WINTER;
        }
    }
}
