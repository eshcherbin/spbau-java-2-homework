package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;

public class VcsObjectWithNameAndKnownSha extends VcsObjectWithName {
    private @NotNull String sha;

    public VcsObjectWithNameAndKnownSha(@NotNull String name, @NotNull String sha, @NotNull VcsObjectType type) {
        super(name);
        this.sha = sha;
        this.type = type;
    }

    @Override
    public @NotNull String getSha() {
        return sha;
    }

    @Override
    public byte[] getContent() {
        return new byte[0];
    }
}
