package io.github.anjoismysign.growables.configuration;

import java.util.List;

public class HologramsConfiguration {
    private double y;
    private List<String> lines;
    private boolean enabled;

    public HologramsConfiguration() {}

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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
