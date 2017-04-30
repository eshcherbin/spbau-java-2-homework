package ru.spbau.eshcherbin.hw4.client.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.spbau.eshcherbin.hw4.Config;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller of the connection scene of the application.
 */
public class ConnectionController implements Initializable {

    private @FXML TextField textFieldAddress;
    private @FXML TextField textFieldPort;
    private @FXML Button buttonConnect;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        textFieldAddress.setText(GuiConfig.DEFAULT_ADDRESS);
        textFieldPort.setText(String.valueOf(Config.serverBindingAddress.getPort()));
        buttonConnect.setOnMouseClicked(event -> {
            ((Stage) buttonConnect.getScene().getWindow()).setTitle("Connecting...");
        });
    }
}
