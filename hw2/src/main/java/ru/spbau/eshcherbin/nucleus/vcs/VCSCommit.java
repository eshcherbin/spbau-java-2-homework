package ru.spbau.eshcherbin.nucleus.vcs;

public class VCSCommit extends VCSObject {
    public VCSCommit() {
        type = VCSObjectType.COMMIT;
    }

    @Override
    public byte[] getContent() {
        return new byte[0];
    }
}
