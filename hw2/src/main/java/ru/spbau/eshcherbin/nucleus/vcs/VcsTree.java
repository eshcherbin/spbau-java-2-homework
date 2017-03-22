package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * A node of a VCS tree.
 */
public class VcsTree extends VcsObjectWithName {
    /**
     * Children of this node.
     */
    protected @NotNull Set<VcsObjectWithName> children;

    public VcsTree(@NotNull String name) {
        super(name);
        type = VcsObjectType.TREE;
        // TreeSet to ensure sorted order
        children = new TreeSet<>();
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull String toString() {
        return children.stream()
                .map(child -> child.getType().toString() + "\t" +
                        child.getSha() + '\t' +
                        child.getName())
                .collect(Collectors.joining("\n"));
    }

    /**
     * Adds a child to this node.
     * @param object the new child
     */
    public void addChild(@NotNull VcsObjectWithName object) {
        children.add(object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getContent() {
        return toString().getBytes();
    }
}
