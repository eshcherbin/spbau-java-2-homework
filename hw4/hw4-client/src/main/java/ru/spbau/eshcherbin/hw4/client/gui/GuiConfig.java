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
    public static final @NotNull String MAIN_FXML_PATH = "scenes/main.fxml";

    /**
     * Path to folder icon file.
     */
    public static final @NotNull String FOLDER_ICON_PATH = "icons/folder.png";

    /**
     * Path to download icon file.
     */
    public static final @NotNull String DOWNLOAD_ICON_PATH = "icons/download.png";

    /**
     * Title of the primary stage.
     */
    public static final @NotNull String STAGE_TITLE = "Simple FTP client";

    /**
     * Default address for the connection scene.
     */
    public static final @NotNull String DEFAULT_ADDRESS = "127.0.0.1";

    /**
     * Name of the root folder.
     */
    public static final @NotNull String ROOT_FOLDER_NAME = "/";

    /**
     * Name which is used to identify the parent folder.
     */
    public static final @NotNull String PARENT_FOLDER_NAME = "..";

    /**
     * Minimum height of the application stage.
     */
    public static final double STAGE_MIN_HEIGHT = 400;

    /**
     * Minimum width of the application stage.
     */
    public static final double STAGE_MIN_WIDTH = 400;
}
