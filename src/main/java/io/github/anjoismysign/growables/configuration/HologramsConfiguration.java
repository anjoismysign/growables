package io.github.anjoismysign.growables.director.config;

import java.util.List;

public class HologramConfig {
    private double y;
    private List<String> lines;

    public HologramConfig() {}

    public String getText() {
        return String.join("\n", lines);
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }
}
