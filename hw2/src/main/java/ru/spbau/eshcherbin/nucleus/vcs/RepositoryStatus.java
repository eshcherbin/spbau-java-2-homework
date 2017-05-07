package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Status of the repository that consists of the name of the current revision
 * and a summary of all added, modified, etc. files.
 */
public class RepositoryStatus {
    private @NotNull String revision;
    private @NotNull Set<StatusEntry> entries;

    public RepositoryStatus(@NotNull String revision) {
        this.revision = revision;
        entries = new HashSet<>();
    }

    /**
     * Returns the name of the current revision.
     * @return the name of the current revision
     */
    public @NotNull String getRevision() {
        return revision;
    }

    /**
     * Returns the set of all modifications of the repository.
     * @return the set of all modifications of the repository
     */
    public @NotNull Set<StatusEntry> getEntries() {
        return entries;
    }

    /**
     * Adds a new entry to the status.
     * @param entry the entry to be added
     */
    public void addEntry(@NotNull StatusEntry entry) {
        entries.add(entry);
    }

    /**
     * Adds a new entry to the status.
     * @param path path to the file in question
     * @param type type of the entry
     */
    public void addEntry(@NotNull Path path, @NotNull StatusEntryType type) {
        addEntry(new StatusEntry(path, type));
    }
}

