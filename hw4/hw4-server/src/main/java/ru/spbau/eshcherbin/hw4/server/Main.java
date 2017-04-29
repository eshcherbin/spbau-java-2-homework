package ru.spbau.eshcherbin.hw4.server;

import ru.spbau.eshcherbin.hw4.Config;

import java.io.IOException;
import java.util.Scanner;

/**
 * Main server application class.
 */
public class Main {
    /**
     * Starts the server application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Server server = new FtpServer(Config.serverBindingAddress);
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scanner scanner = new Scanner(System.in);
        System.out.println("Press ENTER to stop the server");
        scanner.nextLine();
        server.stop();
    }
}
