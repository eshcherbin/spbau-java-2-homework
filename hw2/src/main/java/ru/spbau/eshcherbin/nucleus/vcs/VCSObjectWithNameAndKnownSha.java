package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;

public class VCSObjectWithNameAndKnownSha extends VCSObjectWithName {
    private @NotNull String sha;

    public VCSObjectWithNameAndKnownSha(@NotNull String name, @NotNull String sha, @NotNull VCSObjectType type) {
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
