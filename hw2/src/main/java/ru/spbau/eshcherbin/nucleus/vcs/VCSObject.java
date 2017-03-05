package ru.spbau.eshcherbin.nucleus.vcs;

public abstract class VCSObject {
    protected String sha;
    protected VCSObjectType type;
    protected byte[] content;
}
