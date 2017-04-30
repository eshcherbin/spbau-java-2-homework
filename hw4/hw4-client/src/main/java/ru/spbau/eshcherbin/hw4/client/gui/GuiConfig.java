package ru.spbau.eshcherbin.hw4.client.gui;

import org.jetbrains.annotations.NotNull;

/**
 * GUI configuration class that contains some constants.
 */
class GuiConfig {
    /**
     * Path to connection scene layout file.
     */
    public static final @NotNull String CONNECTION_FXML_PATH = "scenes/connection.fxml";

    /**
     * Path to main scene layout file.
     */
    public static final @NotNull String MAIN_FXML_PATH = "scenes/connection.fxml";

    /**
     * Title of the primary stage.
     */
    public static final @NotNull String STAGE_TITLE = "Simple FTP client";

    /**
     * Default address for the connection scene.
     */
    public static final @NotNull String DEFAULT_ADDRESS = "127.0.0.1";
}
