package ru.spbau.eshcherbin.hw4.client;

import ru.spbau.eshcherbin.hw4.Config;
import ru.spbau.eshcherbin.hw4.ftp.FtpListResponse;
import ru.spbau.eshcherbin.hw4.ftp.FtpListResponseItem;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * Main client application class
 */
public class Main {
    /**
     * Starts the client application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        FtpClient client = new FtpClient();
        try {
            client.connect(
                    new InetSocketAddress(
                            "127.0.0.1",
                            ((InetSocketAddress) Config.serverBindingAddress).getPort()
                    )
            );
        } catch (ConnectException e) {
            System.out.println("Connection unsuccessful");
            System.exit(2);
        } catch (IOException | ClientAlreadyConnectedException e) {
            e.printStackTrace();
            System.exit(2);
        }
        Scanner scanner = new Scanner(System.in);
        boolean isRunning = true;
        while (isRunning) {
            System.out.print("> ");
            String command = scanner.nextLine();
            switch (command) {
                case "list":
                    System.out.print("Enter path: ");
                    String path = scanner.nextLine();
                    try {
                        FtpListResponse response = client.executeList(path);
                        for (FtpListResponseItem item : response.getResponseItems()) {
                            System.out.println((item.isDirectory() ? "dir  " : "file ") + item.getFileName());
                        }
                    } catch (IOException | ClientNotConnectedException e) {
                        e.printStackTrace();
                    }
                    break;
                case "exit":
                    isRunning = false;
                    break;
                default:
                    System.out.println("Unknown command");
                    break;
            }
        }
        try {
            client.disconnect();
        } catch (IOException | ClientNotConnectedException e) {
            e.printStackTrace();
        }
    }
}
