package io.github.anjoismysign.growables.director;

import io.github.anjoismysign.bloblib.entities.GenericManager;
import io.github.anjoismysign.bloblib.managers.PluginManager;
import io.github.anjoismysign.bloblib.managers.asset.BukkitIdentityManager;
import io.github.anjoismysign.growables.Growables;
import io.github.anjoismysign.growables.entity.Growable;
import io.github.anjoismysign.holoworld.asset.IdentityGeneration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class GrowableManager extends GenericManager<Growables, GrowablesManagerDirector> {
    protected final BukkitIdentityManager<Growable> identityManager;

    protected GrowableManager(GrowablesManagerDirector director) {
        super(director);
        identityManager = PluginManager.getInstance().addIdentityManager(Growable.Info.class, Growables.getInstance(), "Growable", false);
        reload();
    }

    @Nullable
    public Growable get(@NotNull String identifier) {
        var generation = identityManager.fetchGeneration(identifier);
        if (generation == null)
            return null;
        return generation.asset();
    }

    public void serialize(@NotNull Growable growable) {
        Objects.requireNonNull(growable, "'growable' cannot be null");
        String identifier = growable.identifier();
        identityManager.add(new IdentityGeneration<>(identifier, growable.info()));
    }

}
