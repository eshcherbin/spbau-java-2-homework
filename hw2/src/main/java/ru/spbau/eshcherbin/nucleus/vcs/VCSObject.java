package ru.spbau.eshcherbin.nucleus.vcs;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public abstract class VCSObject {
    protected VCSObjectType type;

    public abstract byte[] getContent();

    public String getSha() {
        HashFunction sha1HashFunction = Hashing.sha1();
        return sha1HashFunction.newHasher().putBytes(getContent()).hash().toString();
    }

    public VCSObjectType getType() {
        return type;
    }
}
