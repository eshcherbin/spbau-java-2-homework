package ru.spbau.eshcherbin.nucleus.vcs;

/**
 * Utility class that contains some project-wide constants.
 */
class Constants {
    /**
     * Length of the subdirectory name in <tt>objects</tt> directory.
     * Each object's sha is split into two parts: first is used as a subdirectory name and second as a file name
     * in <tt>objects</tt> directory.
     */
    public static final int OBJECT_DIRECTORY_NAME_LENGTH = 2;

    /**
     * Name of the internal repository structure directory.
     */
    public static final String REPOSITORY_DIRECTORY_NAME = ".nuc";

    /**
     * Name of the directory which contains all objects.
     */
    public static final String OBJECTS_DIRECTORY_NAME = "objects";

    /**
     * Name of the directory which contains all references.
     */
    public static final String REFERENCES_DIRECTORY_NAME = "refs";

    /**
     * Name of the head file of the repository.
     */
    public static final String HEAD_FILE_NAME = "HEAD";

    /**
     * Name of the head file of the repository.
     */
    public static final String INDEX_FILE_NAME = "index";

    /**
     * Name of the default branch.
     */
    public static final String DEFAULT_BRANCH_NAME = "master";

    /**
     * System property which is used to retrieve current user's name.
     */
    public static final String USER_NAME_PROPERTY = "user.name";

    /**
     * Prefix which is used to distinguish between revision names (SHA-1) and references.
     */
    public static final String REFERENCE_HEAD_PREFIX = "ref: ";

    /**
     * Prefix which is used to identify a parent in a commit file.
     */
    public static final String PARENT_COMMIT_PREFIX = "parent: ";

    /**
     * Prefix which is used to identify the commit message in a commit file.
     */
    public static final String MESSAGE_COMMIT_PREFIX = "message: ";

    /**
     * Prefix which is added to the commit message in a merge commit.
     */
    public static final String MERGE_COMMIT_MESSAGE = "merged: ";
}
