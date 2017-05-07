package ru.spbau.eshcherbin.nucleus.vcs;

/**
 * Type of the modification that happened to a file in repository.
 */
public enum StatusEntryType {
    /**
     * The file was removed.
     */
    REMOVED,
    /**
     * The file was added.
     */
    ADDED,
    /**
     * The file was modified and added to the index.
     */
    MODIFIED,
    /**
     * The file is modified and not added to the index.
     */
    UNSTAGED,
    /**
     * The file is not added to the repository.
     */
    UNTRACKED,
    /**
     * The file is in the repository but not present in the file system.
     */
    MISSING,
}
