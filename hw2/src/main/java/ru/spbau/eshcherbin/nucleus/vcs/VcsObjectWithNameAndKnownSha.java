package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;

/**
 * A VCS object with a name and known sha.
 * It is used when we do not want to store the actual object, e.g. to represent a blob in a tree.
 */
public class VcsObjectWithNameAndKnownSha extends VcsObjectWithName {
    /**
     * Sha of this object.
     */
    private @NotNull String sha;

    public VcsObjectWithNameAndKnownSha(@NotNull String name, @NotNull String sha, @NotNull VcsObjectType type) {
        super(name);
        this.sha = sha;
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull String getSha() {
        return sha;
    }

    /**
     * {@inheritDoc}
     * This method should not be used because an instance of this class contains no actual content but only a sha of it.
     */
    @Override
    public byte[] getContent() {
        return new byte[0];
    }
}
