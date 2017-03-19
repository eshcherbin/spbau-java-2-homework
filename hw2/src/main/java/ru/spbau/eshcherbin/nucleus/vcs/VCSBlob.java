package ru.spbau.eshcherbin.nucleus.vcs;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.jetbrains.annotations.NotNull;

public class VCSBlob extends VCSObjectWithName {
    private byte[] content;

    public VCSBlob(byte[] content, @NotNull String name) {
        super(name);
        type = VCSObjectType.BLOB;
        this.content = content;
        HashFunction sha1HashFunction = Hashing.sha1();
        sha = sha1HashFunction.newHasher().putBytes(content).hash().toString();
    }

    @Override
    public byte[] getContent() {
        return content;
    }
}
