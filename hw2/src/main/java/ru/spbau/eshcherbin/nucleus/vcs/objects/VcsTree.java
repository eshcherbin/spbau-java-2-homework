package ru.spbau.eshcherbin.nucleus.vcs.objects;

import com.google.common.base.Splitter;
import org.jetbrains.annotations.NotNull;
import ru.spbau.eshcherbin.nucleus.vcs.NucleusRepository;
import ru.spbau.eshcherbin.nucleus.vcs.RepositoryCorruptException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * A node of a VCS tree.
 */
public class VcsTree extends VcsObjectWithName {

    /**
     * Children of this node.
     */
    private @NotNull Set<VcsObjectWithName> children;

    /**
     * Reads a tree from a file.
     * @param repository the operated repository
     * @param treeSha the tree's sha
     * @param treeName the tree's desired name
     * @return the tree
     * @throws RepositoryCorruptException if repository's inner structure is damaged
     * @throws IOException if an I/O error occurs
     */
    public static @NotNull VcsTree readTree(@NotNull NucleusRepository repository, @NotNull String treeSha,
                                               @NotNull String treeName)
            throws RepositoryCorruptException, IOException {
        if (!repository.isValidSha(treeSha)) {
            throw new RepositoryCorruptException();
        }
        VcsTree tree = new VcsTree(treeName);
        Splitter onTabSplitter = Splitter.on('\t').omitEmptyStrings().trimResults();
        for (String line : Files.readAllLines(repository.getObject(treeSha))) {
            List<String> splitResults = onTabSplitter.splitToList(line);
            if (splitResults.size() != 3) {
                throw new RepositoryCorruptException();
            }
            VcsObjectType type;
            try {
                type = VcsObjectType.valueOf(splitResults.get(0));
            } catch (IllegalArgumentException e) {
                throw new RepositoryCorruptException();
            }
            if (!(type == VcsObjectType.BLOB || type == VcsObjectType.TREE)) {
                throw new RepositoryCorruptException();
            }
            String sha = splitResults.get(1);
            String name = splitResults.get(2);
            if (type == VcsObjectType.BLOB) {
                if (!repository.isValidSha(sha)) {
                    throw new RepositoryCorruptException();
                }
                tree.addChild(new VcsObjectWithNameAndKnownSha(name, sha, type));
            } else {
                tree.addChild(readTree(repository, sha, treeName));
            }
        }
        return tree;
    }

    public VcsTree(@NotNull String name) {
        super(name);
        type = VcsObjectType.TREE;
        // TreeSet to ensure sorted order
        children = new TreeSet<>();
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull String toString() {
        return children.stream()
                .map(child -> child.getType().toString() + "\t" +
                        child.getSha() + '\t' +
                        child.getName())
                .collect(Collectors.joining("\n"));
    }

    /**
     * Adds a child to this node.
     * @param object the new child
     */
    public void addChild(@NotNull VcsObjectWithName object) {
        children.add(object);
    }

    public @NotNull Set<VcsObjectWithName> getChildren() {
        return children;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getContent() {
        return toString().getBytes();
    }
}
