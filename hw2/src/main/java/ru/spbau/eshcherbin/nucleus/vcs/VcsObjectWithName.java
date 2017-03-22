package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;

public abstract class VcsObjectWithName extends VcsObject implements Comparable<VcsObjectWithName> {
    protected @NotNull String name;

    public VcsObjectWithName(@NotNull String name) {
        this.name = name;
    }

    public @NotNull String getName() {
        return name;
    }

    @Override
    public int compareTo(@NotNull VcsObjectWithName other) {
        return name.compareTo(other.name);
    }
}
