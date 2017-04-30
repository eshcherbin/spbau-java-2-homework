package ru.spbau.eshcherbin.hw4.client.gui;

import com.google.common.primitives.Ints;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import ru.spbau.eshcherbin.hw4.Config;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller of the connection scene of the application.
 */
public class ConnectionController implements Initializable {
    private static final @NotNull Logger logger = LoggerFactory.getLogger(ConnectionController.class);
    private static final @NotNull Marker fatalMarker = MarkerFactory.getMarker("FATAL");
    private static final int MAX_PORT = 65535;
    private static final int MIN_PORT = 1025;

    private @FXML Label labelPrompt;
    private @FXML TextField textFieldAddress;
    private @FXML TextField textFieldPort;
    private @FXML Button buttonConnect;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        textFieldAddress.setText(GuiConfig.DEFAULT_ADDRESS);
        textFieldPort.setText(String.valueOf(Config.serverBindingAddress.getPort()));
        buttonConnect.setOnMouseClicked(event -> {
            Integer port = Ints.tryParse(textFieldPort.getText());
            if (!isValidPort(port)) {
                labelPrompt.setText("Invalid port");
                return;
            }
            InetSocketAddress serverAddress = new InetSocketAddress(textFieldAddress.getText(), port);

            Scene scene = buttonConnect.getScene();
            Parent newRoot;
            FXMLLoader fxmlLoader;
            try {
                URL mainSceneResource = getClass().getClassLoader().getResource(GuiConfig.MAIN_FXML_PATH);
                if (mainSceneResource == null) {
                    logger.error(fatalMarker, "{} resource not found, aborting", GuiConfig.MAIN_FXML_PATH);
                    Platform.exit();
                    System.exit(1);
                }
                fxmlLoader = new FXMLLoader(mainSceneResource);
                newRoot = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
                logger.error(fatalMarker, "Couldn't load {} resource, aborting", GuiConfig.MAIN_FXML_PATH);
                Platform.exit();
                System.exit(1);
                return; // necessary to ensure that newRoot is initialized in the following code
            }
            scene.setRoot(newRoot);
            MainController mainController = fxmlLoader.getController();
            mainController.setStage((Stage) scene.getWindow());
            mainController.connect(serverAddress);
        });
    }

    @Contract(value = "null -> false", pure = true)
    private boolean isValidPort(@Nullable Integer port) {
        return port != null && MIN_PORT <= port && port <= MAX_PORT;
    }
}
