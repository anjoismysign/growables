package io.github.anjoismysign.growables.phatloots;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface PhatLootsHook {

    void lootAt(@NotNull Location location,
                @NotNull String phatLoot,
                @NotNull Player player);

}
