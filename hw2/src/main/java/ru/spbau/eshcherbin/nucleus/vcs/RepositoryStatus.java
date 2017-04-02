package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class RepositoryStatus {
    private @NotNull String revision;
    private @NotNull Set<StatusEntry> entries;

    public RepositoryStatus(@NotNull String revision) {
        this.revision = revision;
        entries = new HashSet<>();
    }

    public @NotNull String getRevision() {
        return revision;
    }

    public @NotNull Set<StatusEntry> getEntries() {
        return entries;
    }

    public void addEntry(StatusEntry entry) {
        entries.add(entry);
    }

    public void addEntry(@NotNull Path path, @NotNull StatusEntryType type) {
        addEntry(new StatusEntry(path, type));
    }
}

