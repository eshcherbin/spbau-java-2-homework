package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;

/**
 * A binary large object
 */
public class VcsBlob extends VcsObjectWithName {
    /**
     * Object's content.
     */
    private final byte[] content;

    public VcsBlob(byte[] content, @NotNull String name) {
        super(name);
        type = VcsObjectType.BLOB;
        this.content = content;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getContent() {
        return content;
    }
}
