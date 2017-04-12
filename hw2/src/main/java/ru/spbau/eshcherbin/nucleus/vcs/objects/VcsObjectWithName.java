package ru.spbau.eshcherbin.nucleus.vcs.objects;

import org.jetbrains.annotations.NotNull;

/**
 * A VCS object with a name.
 */
public abstract class VcsObjectWithName extends VcsObject implements Comparable<VcsObjectWithName> {
    /**
     * Name of this object.
     */
    protected @NotNull String name;

    public VcsObjectWithName(@NotNull String name) {
        this.name = name;
    }

    /**
     * Returns the name of this object.
     * @return the name of this object
     */
    public @NotNull String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@NotNull VcsObjectWithName other) {
        return name.compareTo(other.name);
    }
}
