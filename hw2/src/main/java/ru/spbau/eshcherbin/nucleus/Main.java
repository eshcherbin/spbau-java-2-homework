package ru.spbau.eshcherbin.nucleus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.eshcherbin.nucleus.vcs.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    private static void printHelp(@Nullable String preMessage) {
        if (preMessage != null) {
            System.out.println(preMessage);
        }
        System.out.println("usage:" + "\n    nucleus init [<path>]" + "\n    nucleus add <path>"
                 + "\n    nucleus remove <path>" + "\n    nucleus commit <message>" + "\n    nucleus help"
                 + "\n    nucleus branch <branchName>" + "\n    nucleus checkout <revisionName>");
        System.out.println("shortcuts:" + "\n    rm = remove" + "\n    ci = commit" + "\n    cout = checkout");
    }

    private static void printHelp() {
        printHelp(null);
    }

    private static void printError(@NotNull String errorMessage) {
        System.err.println("Error: " + errorMessage);
        System.exit(1);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp("No arguments provided");
            return;
        }
        switch (args[0]) {
            case "init": {
                Path path = Paths.get("").toAbsolutePath();
                if (args.length >= 2) {
                    path = path.resolve(args[1]).normalize();
                }
                try {
                    NucleusManager.initRepository(path);
                } catch (IOException e) {
                    printError("IO Error");
                } catch (DirectoryExpectedException e) {
                    printError("Directory expected");
                } catch (RepositoryAlreadyInitializedException e) {
                    printError("Repository already initialized");
                }
                break;
            }
            case "add": {
                Path path = Paths.get("").toAbsolutePath();
                if (args.length >= 2) {
                    path = path.resolve(args[1]).normalize();
                } else {
                    printHelp("No path provided");
                    return;
                }
                try {
                    NucleusManager.addToIndex(path);
                } catch (RepositoryNotInitializedException e) {
                    printError("Repository not initialized");
                } catch (IOException e) {
                    printError("IO Error");
                } catch (IndexFileCorruptException e) {
                    printError("Index file corrupt");
                }
                break;
            }
            case "remove":
            case "rm": {
                Path path = Paths.get("").toAbsolutePath();
                if (args.length >= 2) {
                    path = path.resolve(args[1]).normalize();
                } else {
                    printHelp("No path provided");
                    return;
                }
                try {
                    NucleusManager.removeFromIndex(path);
                } catch (RepositoryNotInitializedException e) {
                    printError("Repository not initialized");
                } catch (IOException e) {
                    printError("IO Error");
                } catch (IndexFileCorruptException e) {
                    printError("Index file is corrupt");
                }
                break;
            }
            case "commit":
            case "ci": {
                Path path = Paths.get("").toAbsolutePath();
                if (args.length < 2) {
                    printHelp("No commit message provided");
                    return;
                }
                String message = args[1];
                try {
                    NucleusManager.commitChanges(path, message);
                } catch (IOException | DirectoryExpectedException e) {
                    // DirectoryExpectedException should not be thrown here
                    printError("IO Error");
                } catch (RepositoryNotInitializedException e) {
                    printError("Repository not initialized");
                } catch (HeadFileCorruptException e) {
                    printError("HEAD file is corrupt");
                } catch (IndexFileCorruptException e) {
                    printError("Index file is corrupt");
                }
                break;
            }
            case "branch": {
                Path path = Paths.get("").toAbsolutePath();
                if (args.length < 2) {
                    printHelp("No branch name provided");
                    return;
                }
                String branchName = args[1];
                try {
                    NucleusManager.newBranch(path, branchName);
                } catch (IOException | DirectoryExpectedException e) {
                    // DirectoryExpectedException should not be thrown here
                    printError("IO Error");
                } catch (RepositoryNotInitializedException e) {
                    printError("Repository not initialized");
                } catch (HeadFileCorruptException e) {
                    printError("HEAD file is corrupt");
                } catch (BranchAlreadyExistsException e) {
                    printError("Branch " + branchName + " already exists");
                }
                break;
            }
            case "log": {
                Path path = Paths.get("").toAbsolutePath();
                LogMessage logMessage = null;
                try {
                    logMessage = NucleusManager.getLog(path);
                } catch (IOException | DirectoryExpectedException e) {
                    // DirectoryExpectedException should not be thrown here
                    printError("IO Error");
                } catch (RepositoryNotInitializedException e) {
                    printError("Repository not initialized");
                } catch (HeadFileCorruptException e) {
                    printError("HEAD file is corrupt");
                } catch (RepositoryCorruptException e) {
                    printError("Repository is corrupt");
                }
                while (logMessage != null) {
                    System.out.println(logMessage.getMessage());
                    if (logMessage.getNextLogMessage() != null) {
                        System.out.println();
                    }
                    logMessage = logMessage.getNextLogMessage();
                }
                break;
            }
            case "checkout":
            case "cout": {
                Path path = Paths.get("").toAbsolutePath();
                if (args.length < 2) {
                    printHelp("No revision name provided");
                    return;
                }
                String revisionName = args[1];
                try {
                    NucleusManager.checkoutRevision(path, revisionName);
                } catch (IOException | DirectoryExpectedException e) {
                    // DirectoryExpectedException should not be thrown here
                    printError("IO Error");
                } catch (RepositoryNotInitializedException e) {
                    printError("Repository not initialized");
                } catch (HeadFileCorruptException e) {
                    printError("HEAD file is corrupt");
                } catch (RepositoryCorruptException e) {
                    printError("Repository is corrupt");
                } catch (IndexFileCorruptException e) {
                    printError("Index file is corrupt");
                } catch (NoSuchRevisionException e) {
                    printError("No such revision found");
                }
                break;
            }
            case "help": {
                printHelp();
                break;
            }
            default: {
                printHelp("Unknown argument");
                break;
            }
        }
    }
}
