package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class VCSTree extends VCSObjectWithName {
    private @NotNull Set<VCSObjectWithName> children;

    public VCSTree(@NotNull String name) {
        super(name);
        type = VCSObjectType.TREE;
        // TreeSet to ensure sorted order
        children = new TreeSet<>();
        this.name = name;
    }

    public void addChild(@NotNull VCSObjectWithName object) {
        children.add(object);
    }

    @Override
    public byte[] getContent() {
        return children.stream()
                       .map(child -> child.getType().toString() + "\t" +
                                     child.getSha() + '\t' +
                                     child.getName())
                       .collect(Collectors.joining("\n"))
                       .getBytes();
    }
}
