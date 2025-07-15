package io.github.anjoismysign.growables.entity;

import io.github.anjoismysign.bloblib.utilities.TextColor;
import io.github.anjoismysign.growables.Growables;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Hologram(@Nullable TextDisplay display) {

    public void update(SimpleDevelopable developable){
        if (display == null) {
            return;
        }
        display.setText(TextColor.PARSE(Growables.getInstance().getManagerDirector().getConfigManager().getConfiguration().getHolograms().getText()
                .replace("%progress%", developable.growthProgressBar())));
    }

    public static Hologram random(@NotNull Location location){
        boolean enabled = Growables.getInstance().getManagerDirector().getConfigManager().getConfiguration().getHolograms().isEnabled();
        if (!enabled){
            return new Hologram(null);
        }
        TextDisplay textDisplay = (TextDisplay) location.getWorld().spawnEntity(location, EntityType.TEXT_DISPLAY);
        textDisplay.setPersistent(false);
        textDisplay.setBillboard(Display.Billboard.CENTER);
        return new Hologram(textDisplay);
    }

}
