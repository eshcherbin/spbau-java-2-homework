package ru.spbau.eshcherbin.hw4.client.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import ru.spbau.eshcherbin.hw4.client.ClientAlreadyConnectedException;
import ru.spbau.eshcherbin.hw4.client.ClientNotConnectedException;
import ru.spbau.eshcherbin.hw4.client.FtpClient;
import ru.spbau.eshcherbin.hw4.ftp.FtpListResponse;
import ru.spbau.eshcherbin.hw4.ftp.FtpListResponseItem;

import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

/**
 * Controller of the main scene of the application.
 */
public class MainController implements Initializable {
    private static final @NotNull Logger logger = LoggerFactory.getLogger(MainController.class);
    private static final @NotNull Marker fatalMarker = MarkerFactory.getMarker("FATAL");
    private @FXML Label labelStatus;
    private @FXML ListView<FtpListResponseItem> listViewDirectoryContent;
    private @FXML Label labelCurrentPath;
    private final @NotNull FtpClient client = new FtpClient();
    private Stage stage;
    private Image folderIconImage;
    private Image downloadIconImage;

    private @NotNull Path currentPath = Paths.get(GuiConfig.ROOT_FOLDER_NAME);
    private final @NotNull ObservableList<FtpListResponseItem> directoryContentItems =
            FXCollections.observableArrayList();

    /**
     * Sets the stage for the controller.
     * @param stage the stage
     */
    public void setStage(@NotNull Stage stage) {
        this.stage = stage;
    }

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
        try {
            client.connect(serverAddress);
        } catch (IOException e) {
            logger.error("Couldn't connect to {}", serverAddress);
            labelStatus.setText("Unable to connect");
            return;
        } catch (ClientAlreadyConnectedException e) {
            logger.error(fatalMarker, "Client already connected (shouldn't happen)");
            labelStatus.setText("Already connected");
            return;
        }
        labelStatus.setText("Connected to " + serverAddress);
        updateDirectoryContent();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        URL folderIconResource = getClass().getClassLoader().getResource(GuiConfig.FOLDER_ICON_PATH);
        if (folderIconResource == null) {
            logger.error(fatalMarker, "{} resource not found, aborting", GuiConfig.FOLDER_ICON_PATH);
            Platform.exit();
            System.exit(1);
        }
        URL downloadIconResource = getClass().getClassLoader().getResource(GuiConfig.DOWNLOAD_ICON_PATH);
        if (downloadIconResource == null) {
            logger.error(fatalMarker, "{} resource not found, aborting", GuiConfig.DOWNLOAD_ICON_PATH);
            Platform.exit();
            System.exit(1);
        }
        folderIconImage = new Image(folderIconResource.toExternalForm());
        downloadIconImage = new Image(downloadIconResource.toExternalForm());

        listViewDirectoryContent.setCellFactory(listView -> new DirectoryContentItemListCell());
        listViewDirectoryContent.setOnMouseClicked(this::onListItemClicked);
        listViewDirectoryContent.setItems(directoryContentItems);
    }

    private void onListItemClicked(MouseEvent event) {
        if (event.getClickCount() < 2) {
            return;
        }
        FtpListResponseItem selectedItem = listViewDirectoryContent.getSelectionModel().getSelectedItem();
        if (selectedItem.isDirectory()) {
            if (!currentPath.toString().equals(GuiConfig.ROOT_FOLDER_NAME) &&
                    listViewDirectoryContent.getSelectionModel().getSelectedIndex() == 0) {
                currentPath = currentPath.getParent();
            } else {
                currentPath = currentPath.resolve(selectedItem.getFileName());
            }
            updateDirectoryContent();
        } else {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(Paths.get("").toAbsolutePath().toFile());
            directoryChooser.setTitle("Choose directory to save " + selectedItem.getFileName());
            File destinationDirectory = directoryChooser.showDialog(stage);
            if (destinationDirectory != null) {
                try {
                    client.executeGet(currentPath.resolve(selectedItem.getFileName()).toString(),
                            destinationDirectory.toPath().resolve(selectedItem.getFileName()));
                } catch (ClientNotConnectedException | IOException e) {
                    logger.error("Disconnected from server");
                    labelStatus.setText("Disconnected from server");
                    directoryContentItems.clear();
                }
            }
        }
    }

    /**
     * Queries list of files at current path and fills the list view.
     */
    private void updateDirectoryContent() {
        labelCurrentPath.setText("At " + currentPath.toString());
        FtpListResponse response;
        try {
            response = client.executeList(currentPath.toString());
        } catch (ClientNotConnectedException | IOException e) {
            logger.error("Disconnected from server");
            labelStatus.setText("Disconnected from server");
            return;
        }
        directoryContentItems.clear();
        if (!currentPath.toString().equals(GuiConfig.ROOT_FOLDER_NAME)) {
            directoryContentItems.add(new FtpListResponseItem(GuiConfig.PARENT_FOLDER_NAME, true));
        }
        directoryContentItems.addAll(response.getResponseItems());
    }

    private class DirectoryContentItemListCell extends ListCell<FtpListResponseItem> {
        private @NotNull ImageView imageViewIcon = new ImageView();

        @Override
        protected void updateItem(@Nullable FtpListResponseItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            if (item.isDirectory()) {
                imageViewIcon.setImage(folderIconImage);
            } else {
                imageViewIcon.setImage(downloadIconImage);
            }
            setText(item.getFileName());
            setGraphic(imageViewIcon);
        }
    }
}
