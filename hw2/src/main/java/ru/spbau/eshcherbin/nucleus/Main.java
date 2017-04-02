package ru.spbau.eshcherbin.nucleus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spbau.eshcherbin.nucleus.vcs.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    private static final String logFileName = System.getProperty("user.home") + "/.nucleus.log";
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static void printHelp(@Nullable String preMessage) {
        if (preMessage != null) {
            System.out.println(preMessage);
        }
        System.out.println("usage:" + "\n    nucleus init [<path>]" + "\n    nucleus add <path>"
                + "\n    nucleus remove <path>" + "\n    nucleus commit <message>"
                + "\n    nucleus branch [delete] <branchName>" + "\n    nucleus checkout <revisionName>"
                + "\n    nucleus merge <revisionName>" + "\n    nucleus log"
                + "\n    nucleus status" + "\n    clean" + "\n    reset <path>" + "\n    nucleus help");
        System.out.println("shortcuts:" + "\n    rm = remove" + "\n    ci = commit" + "\n    cout = checkout"
                + "\n    st = status");
    }

    private static void printHelp() {
        printHelp(null);
    }

    private static void printError(@NotNull String errorMessage) {
        logger.error(errorMessage);
        System.err.println("An error occured: " + errorMessage);
        System.err.println("Consult " + logFileName + " for details");
        System.exit(1);
    }

    public static void main(String[] args) {

        logger.info("Nucleus started with following arguments: {}", (Object) args);
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
                    NucleusManager.initializeRepository(path);
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
                if (args.length < 2) {
                    printHelp("No path provided");
                    return;
                }
                Path path = Paths.get(args[1]).toAbsolutePath();
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
                if (args.length < 2 || args[1].isEmpty()) {
                    printHelp("No commit message provided");
                    return;
                }
                String message = args[1];
                try {
                    NucleusManager.commitChanges(path, message);
                } catch (IOException e) {
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
                if (args.length < 2 || (args[1].equals("delete") && args.length < 3)) {
                    printHelp("No branch name provided");
                    return;
                }
                String branchName;
                if (args[1].equals("delete")) {
                    branchName = args[2];
                    try {
                        NucleusManager.deleteBranch(path, branchName);
                    } catch (IOException e) {
                        printError("IO Error");
                    } catch (RepositoryNotInitializedException e) {
                        printError("Repository not initialized");
                    } catch (HeadFileCorruptException e) {
                        printError("HEAD file is corrupt");
                    } catch (NoSuchRevisionOrBranchException e) {
                        printError("Branch " + branchName + " does not exists");
                    } catch (DeletingHeadBranchException e) {
                        printError("Unable to delete current branch");
                    }
                } else {
                    branchName = args[1];
                    try {
                        NucleusManager.createBranch(path, branchName);
                    } catch (IOException e) {
                        // DirectoryExpectedException should not be thrown here
                        printError("IO Error");
                    } catch (RepositoryNotInitializedException e) {
                        printError("Repository not initialized");
                    } catch (HeadFileCorruptException e) {
                        printError("HEAD file is corrupt");
                    } catch (BranchAlreadyExistsException e) {
                        printError("Branch " + branchName + " already exists");
                    }
                }
                break;
            }
            case "log": {
                Path path = Paths.get("").toAbsolutePath();
                LogMessage logMessage = null;
                try {
                    logMessage = NucleusManager.getLog(path);
                } catch (IOException e) {
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
                } catch (IOException e) {
                    printError("IO Error");
                } catch (RepositoryNotInitializedException e) {
                    printError("Repository not initialized");
                } catch (HeadFileCorruptException e) {
                    printError("HEAD file is corrupt");
                } catch (RepositoryCorruptException e) {
                    printError("Repository is corrupt");
                } catch (IndexFileCorruptException e) {
                    printError("Index file is corrupt");
                } catch (NoSuchRevisionOrBranchException e) {
                    printError("No such branch or revision found");
                }
                break;
            }
            case "merge": {
                Path path = Paths.get("").toAbsolutePath();
                if (args.length < 2) {
                    printHelp("No revision name provided");
                    return;
                }
                String revisionName = args[1];
                try {
                    NucleusManager.mergeRevision(path, revisionName);
                } catch (IOException e) {
                    printError("IO Error");
                } catch (RepositoryNotInitializedException e) {
                    printError("Repository not initialized");
                } catch (HeadFileCorruptException e) {
                    printError("HEAD file is corrupt");
                } catch (RepositoryCorruptException e) {
                    printError("Repository is corrupt");
                } catch (IndexFileCorruptException e) {
                    printError("Index file is corrupt");
                } catch (NoSuchRevisionOrBranchException e) {
                    printError("No such branch or revision found");
                }
                break;
            }
            case "clean": {
                Path path = Paths.get("").toAbsolutePath();
                try {
                    NucleusManager.cleanRepository(path);
                } catch (IOException e) {
                    printError("IO Error");
                } catch (RepositoryNotInitializedException e) {
                    printError("Repository not initialized");
                } catch (IndexFileCorruptException e) {
                    printError("Index file is corrupt");
                }
                break;
            }
            case "reset": {
                if (args.length < 2) {
                    printHelp("No path provided");
                    return;
                }
                Path path = Paths.get(args[1]).toAbsolutePath();
                try {
                    NucleusManager.resetFile(path);
                } catch (IOException e) {
                    e.printStackTrace();
                    printError("IO Error");
                } catch (RepositoryNotInitializedException e) {
                    printError("Repository not initialized");
                } catch (IndexFileCorruptException e) {
                    printError("Index file is corrupt");
                } catch (FileNotInRepositoryException e) {
                    printError("File not in current revision");
                }
                break;
            }
            case "status":
            case "st": {
                Path path = Paths.get("").toAbsolutePath();
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
