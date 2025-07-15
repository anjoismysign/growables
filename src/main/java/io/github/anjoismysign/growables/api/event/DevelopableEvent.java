package io.github.anjoismysign.growables.event;

import io.github.anjoismysign.growables.api.Developable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class DevelopableEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private @NotNull
    final Developable developable;

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public DevelopableEvent(@NotNull Developable developable) {
        Objects.requireNonNull(developable, "'developable' cannot be null");
        this.developable = developable;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public @NotNull Developable getDevelopable() {
        return developable;
    }
}
