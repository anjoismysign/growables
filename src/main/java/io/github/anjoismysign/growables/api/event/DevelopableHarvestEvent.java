package io.github.anjoismysign.growables.api.event;

import io.github.anjoismysign.growables.api.Developable;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DevelopableHarvestEvent extends DevelopableEvent implements Cancellable {

    private final Player player;
    private boolean cancelled;

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public DevelopableHarvestEvent(@NotNull Developable developable,
                                   @NotNull Player player) {
        super(developable);
        this.player = player;
        this.cancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
