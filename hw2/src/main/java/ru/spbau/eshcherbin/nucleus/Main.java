package ru.spbau.eshcherbin.nucleus;

import ru.spbau.eshcherbin.nucleus.vcs.DirectoryExpectedException;
import ru.spbau.eshcherbin.nucleus.vcs.NucleusManager;
import ru.spbau.eshcherbin.nucleus.vcs.RepositoryAlreadyInitializedException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        if (Objects.equals(args[0], "init")) {
            Path path = Paths.get("").toAbsolutePath();
            if (args.length >= 2) {
                path = path.resolve(args[1]).normalize();
            }
            try {
                NucleusManager.initRepository(path);
            } catch (IOException e) {
                System.out.println("IO Error");
            } catch (DirectoryExpectedException e) {
                System.out.println("Directory expected");
            } catch (RepositoryAlreadyInitializedException e) {
                System.out.println("Repository already initialized");
            }
        }
    }
}
