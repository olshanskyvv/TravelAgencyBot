package bcd.solution.dvgKiprBot.core.models;

import jakarta.persistence.Entity;


public enum Stars {
    nullstar,
    onestar,
    twostar,
    threestar,
    fourstar,
    fivestar;

    @Override
    public String toString() {
        switch (this) {
            case nullstar -> {return "Без звезд";}
            case onestar -> {return "⭐️";}
            case twostar -> {return "⭐️⭐️";}
            case threestar -> {return "⭐️⭐️⭐️";}
            case fourstar -> {return "⭐️⭐️⭐️⭐️";}
            case fivestar -> {return "⭐️⭐️⭐️⭐️⭐️";}
        }
        return "";
    }
}
