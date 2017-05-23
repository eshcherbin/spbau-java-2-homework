package ru.spbau.eshcherbin.hw4.client.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.net.URL;

/**
 * ClientApplication client GUI class
 */
public class ClientApplication extends Application {
    private static final @NotNull Logger logger = LoggerFactory.getLogger(ClientApplication.class);
    private static final @NotNull Marker fatalMarker = MarkerFactory.getMarker("FATAL");

    /**
     * Starts the client application with graphical interface.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle(GuiConfig.STAGE_TITLE);
        primaryStage.setMinHeight(GuiConfig.STAGE_MIN_HEIGHT);
        primaryStage.setMinWidth(GuiConfig.STAGE_MIN_WIDTH);
        URL connectionSceneResource = getClass().getClassLoader().getResource(GuiConfig.CONNECTION_FXML_PATH);
        if (connectionSceneResource == null) {
            logger.error(fatalMarker, "{} resource not found, aborting", GuiConfig.CONNECTION_FXML_PATH);
            Platform.exit();
            System.exit(1);
        }
        Parent root = FXMLLoader.load(connectionSceneResource);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
