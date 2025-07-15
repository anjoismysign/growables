package io.github.anjoismysign.growables.event;

import io.github.anjoismysign.growables.api.Developable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DevelopableGrowEvent extends DevelopableEvent {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private int stage;

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public DevelopableGrowEvent(@NotNull Developable developable,
                                int stage) {
        super(developable);
        this.stage = stage;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public boolean isLastStage(){
        Developable developable = getDevelopable();
        return stage == developable.getLastStageIndex();
    }
}
