package ru.spbau.eshcherbin.nucleus.vcs;

public abstract class VCSObject {
    protected String sha;
    protected VCSObjectType type;

    public abstract byte[] getContent();

    public String getSha() {
        return sha;
    }

    public VCSObjectType getType() {
        return type;
    }
}
