package io.github.anjoismysign.growables;

import io.github.anjoismysign.bloblib.entities.GenericManagerDirector;

public class GrowablesManagerDirector extends GenericManagerDirector<BlobGrowables> {

    public GrowablesManagerDirector(BlobGrowables blobPlugin) {
        super(blobPlugin);

    }

    @Override
    public void reload() {
    }

    @Override
    public void unload() {
    }
}