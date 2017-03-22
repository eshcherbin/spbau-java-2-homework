package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;

public class VcsBlob extends VcsObjectWithName {
    private byte[] content;

    public VcsBlob(byte[] content, @NotNull String name) {
        super(name);
        type = VcsObjectType.BLOB;
        this.content = content;
    }

    @Override
    public byte[] getContent() {
        return content;
    }
}
