package ru.spbau.eshcherbin.hw4.client.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.jetbrains.annotations.NotNull;
import ru.spbau.eshcherbin.hw4.client.ClientNotConnectedException;
import ru.spbau.eshcherbin.hw4.client.FtpClient;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller of the main scene of the application.
 */
public class MainController implements Initializable {
    private @FXML Label labelStatus;
    private @FXML ListView listViewDirectoryContent;

    private final @NotNull FtpClient client = new FtpClient();

    /**
     * Connects the client to a server with given address.
     * @param serverAddress the address of the server
     */
    public void connect(@NotNull SocketAddress serverAddress) {
        labelStatus.getScene().getWindow().setOnCloseRequest(event -> {
            if (client.isConnected()) {
                try {
                    client.disconnect();
                } catch (IOException | ClientNotConnectedException e) {
                    e.printStackTrace();
                }
            }
        });
        labelStatus.setText("Connecting to " + serverAddress);
        //TODO: connect
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }
}
