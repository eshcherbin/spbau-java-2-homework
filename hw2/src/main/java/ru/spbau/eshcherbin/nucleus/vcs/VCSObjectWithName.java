package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;

public abstract class VCSObjectWithName extends VCSObject implements Comparable<VCSObjectWithName> {
    protected @NotNull String name;

    public VCSObjectWithName(@NotNull String name) {
        this.name = name;
    }

    public @NotNull String getName() {
        return name;
    }

    @Override
    public int compareTo(@NotNull VCSObjectWithName other) {
        return (name + " " + sha).compareTo(other.name + " " + other.sha);
    }
}
