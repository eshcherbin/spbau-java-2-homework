package ru.spbau.eshcherbin.nucleus.vcs;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public abstract class VcsObject {
    protected VcsObjectType type;

    public abstract byte[] getContent();

    public String getSha() {
        HashFunction sha1HashFunction = Hashing.sha1();
        return sha1HashFunction.newHasher().putBytes(getContent()).hash().toString();
    }

    public VcsObjectType getType() {
        return type;
    }
}
