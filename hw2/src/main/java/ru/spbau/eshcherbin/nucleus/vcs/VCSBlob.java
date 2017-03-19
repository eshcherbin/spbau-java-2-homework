package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;

public class VCSBlob extends VCSObjectWithName {
    private byte[] content;

    public VCSBlob(byte[] content, @NotNull String name) {
        super(name);
        type = VCSObjectType.BLOB;
        this.content = content;
    }

    @Override
    public byte[] getContent() {
        return content;
    }
}
