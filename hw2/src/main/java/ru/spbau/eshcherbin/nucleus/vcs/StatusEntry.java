package ru.spbau.eshcherbin.nucleus.vcs;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * An entry in the repository status.
 * Represents a modification of the file inside the repository.
 */
public class StatusEntry {
    private @NotNull Path path;
    private @NotNull StatusEntryType type;

    public StatusEntry(@NotNull Path path, @NotNull StatusEntryType type) {
        this.path = path;
        this.type = type;
    }

    /**
     * Returns the path to the file.
     * @return the path to the file
     */
    public @NotNull Path getPath() {
        return path;
    }

    /**
     * Returns the type of the modification to the file.
     * @return the type of the modification to the file
     */
    public @NotNull StatusEntryType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatusEntry that = (StatusEntry) o;

        return path.equals(that.path) && type == that.type;
    }

    @Override
    public int hashCode() {
        int result = path.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
