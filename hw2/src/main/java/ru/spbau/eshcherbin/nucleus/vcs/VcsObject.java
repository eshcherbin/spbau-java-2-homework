package ru.spbau.eshcherbin.nucleus.vcs;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * An abstract VCS object.
 */
public abstract class VcsObject {
    /**
     * Type of this object.
     */
    protected VcsObjectType type;

    /**
     * Returns the content of the object. Object's sha is usually calculated using its content.
     * @return the content of the object
     */
    public abstract byte[] getContent();

    /**
     * Returns the sha of this object.
     * @return the sha of this object
     */
    public String getSha() {
        HashFunction sha1HashFunction = Hashing.sha1();
        return sha1HashFunction.newHasher().putBytes(getContent()).hash().toString();
    }

    /**
     * Returns the sha of this object.
     * @return the sha of this object
     */
    public VcsObjectType getType() {
        return type;
    }
}
