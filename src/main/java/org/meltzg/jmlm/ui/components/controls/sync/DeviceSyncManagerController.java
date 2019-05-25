package org.meltzg.jmlm.ui.components.controls.sync;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;
import org.meltzg.jmlm.device.content.AudioContent;
import org.meltzg.jmlm.exceptions.InsufficientSpaceException;
import org.meltzg.jmlm.exceptions.InvalidStateException;
import org.meltzg.jmlm.exceptions.SyncStrategyException;
import org.meltzg.jmlm.repositories.AudioContentRepository;
import org.meltzg.jmlm.repositories.FileSystemAudioContentDeviceRepository;
import org.meltzg.jmlm.sync.ContentSyncStatus;
import org.meltzg.jmlm.sync.DeviceSyncManager;
import org.meltzg.jmlm.sync.NotInLibraryStrategy;
import org.meltzg.jmlm.sync.strategies.GreedySyncStrategy;
import org.meltzg.jmlm.sync.strategies.LazySyncStrategy;
import org.meltzg.jmlm.ui.components.DialogController;
import org.meltzg.jmlm.ui.components.FXMLDialog;
import org.meltzg.jmlm.ui.types.DeviceWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
public class DeviceSyncManagerController implements DialogController, Initializable {
    @FXML
    public TableView<SelectedContent> contentTable;
    @FXML
    public TableColumn colOnDevice;
    @FXML
    public TableColumn colTitle;
    @FXML
    public TableColumn colArtist;
    @FXML
    public TableColumn colAlbum;
    @FXML
    public TableColumn colGenre;
    @FXML
    public TableColumn colTrack;
    @FXML
    public TableColumn colDisc;
    @FXML
    public Label lblStorageInfo;
    @FXML
    public ProgressBar prgCapacityBar;
    @FXML
    public Button btnSyncSelection;
    @Autowired
    FileSystemAudioContentDeviceRepository deviceRepository;
    @Autowired
    AudioContentRepository contentRepository;
    @Setter
    private FXMLDialog dialog;
    @FXML
    private ChoiceBox<DeviceWrapper> chcLibrary;
    @FXML
    private ChoiceBox<DeviceWrapper> chcAttached;
    private ObservableList<SelectedContent> selectedContent;
    private DeviceSyncManager syncManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshDevices();

        for (var chcBox : Arrays.asList(chcLibrary, chcAttached)) {
            chcBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                    try {
                        handleSelectedDeviceChange();
                    } catch (InvalidStateException e) {
                        if (oldValue.intValue() == -1) {
                            chcBox.getSelectionModel().clearSelection();
                        } else {
                            chcBox.getSelectionModel().select(oldValue.intValue());
                        }
                    }
                }
            });
        }

        selectedContent = FXCollections.observableArrayList();

        colOnDevice.setCellValueFactory((Callback<TableColumn.CellDataFeatures<SelectedContent, Boolean>, ObservableValue>) param ->
                param.getValue().selectedProperty);
        colOnDevice.setCellFactory(CheckBoxTableCell.forTableColumn(colOnDevice));

        setContentTableColumnFactory(colTitle, FieldKey.TITLE);
        setContentTableColumnFactory(colArtist, FieldKey.ARTIST);
        setContentTableColumnFactory(colAlbum, FieldKey.ALBUM);
        setContentTableColumnFactory(colGenre, FieldKey.GENRE);
        setContentTableColumnFactory(colTrack, FieldKey.TRACK);
        setContentTableColumnFactory(colDisc, FieldKey.DISC_NO);

        contentTable.setItems(selectedContent);
        contentTable.setEditable(true);
    }

    public void refreshDevices() {
        var devices = new ArrayList<DeviceWrapper>();
        for (var device : deviceRepository.findAll()) {
            device.setContentRepo(contentRepository);
            devices.add(new DeviceWrapper(device));
        }
        log.info(devices.stream().map(dev -> dev.getDevice().toString()).collect(Collectors.joining()));
        chcLibrary.getItems().setAll(devices);
        chcAttached.getItems().setAll(devices);
    }

    public void syncSelection(MouseEvent mouseEvent) {
        var desiredContent = selectedContent.stream()
                .filter((selection) -> selection.getSelectedProperty().get())
                .map(SelectedContent::getContentSyncStatusProperty)
                .map(ObjectProperty::get)
                .map(ContentSyncStatus::getContentInfo)
                .map(AudioContent::getId)
                .collect(Collectors.toSet());

        try {
            syncManager.syncDevice(desiredContent, NotInLibraryStrategy.CANCEL_SYNC);
        } catch (SyncStrategyException e) {
            log.error("Could not apply any sync strategy");
            var choice = showAlert("Unexpected Error",
                    "Content to remove from device not in library",
                    "Click Apply to continue (DOES NOT TRANSFER TO LIBRARY)\n" +
                            "Click Cancel to cancel sync operation\n" +
                            "Click OK to transfer missing tracks to library",
                    Alert.AlertType.ERROR,
                    ButtonType.APPLY,
                    ButtonType.OK,
                    ButtonType.CANCEL);
            choice.ifPresent(buttonType -> {
                if (buttonType == ButtonType.APPLY) {
                    log.info("delete from library");
                    try {
                        syncManager.syncDevice(desiredContent, NotInLibraryStrategy.DELETE_FROM_DEVICE);
                    } catch (ClassNotFoundException | IOException | InsufficientSpaceException | SyncStrategyException |
                            ReadOnlyFileException | TagException | InvalidAudioFrameException | CannotReadException ex) {
                        log.error("Error syncing selected content to device", e);

                        showAlert("Unexpected Error",
                                "Sync Failure",
                                e.getMessage(),
                                Alert.AlertType.ERROR);
                    }
                } else if (buttonType == ButtonType.OK) {
                    try {
                        syncManager.syncDevice(desiredContent, NotInLibraryStrategy.TRANSFER_TO_LIBRARY);
                    } catch (ClassNotFoundException | IOException | InsufficientSpaceException | SyncStrategyException |
                            ReadOnlyFileException | TagException | InvalidAudioFrameException | CannotReadException ex) {
                        log.error("Error syncing selected content to device", e);

                        showAlert("Unexpected Error",
                                "Sync Failure",
                                e.getMessage(),
                                Alert.AlertType.ERROR);
                    }
                }
            });

        } catch (ClassNotFoundException | IOException | InsufficientSpaceException |
                ReadOnlyFileException | TagException | InvalidAudioFrameException |
                CannotReadException e) {
            log.error("Error syncing selected content to device", e);

            showAlert("Unexpected Error",
                    "Sync Failure",
                    e.getMessage(),
                    Alert.AlertType.ERROR);
        }

        deviceRepository.saveAll(Arrays.asList(getAttachedDevice(), getLibraryDevice()));
        refreshContentTable();
    }

    private FileSystemAudioContentDevice getDevice(boolean libraryDevice) {
        ChoiceBox<DeviceWrapper> chcBox = libraryDevice ? chcLibrary : chcAttached;

        var deviceIdx = chcBox.getSelectionModel().getSelectedIndex();

        log.info("Library: {}, Index: {}", libraryDevice, deviceIdx);

        if (deviceIdx == -1) {
            return null;
        }

        return chcBox.getItems().get(deviceIdx).getDevice();
    }

    private FileSystemAudioContentDevice getLibraryDevice() {
        return getDevice(true);
    }

    private FileSystemAudioContentDevice getAttachedDevice() {
        return getDevice(false);
    }

    public void resetSelection(MouseEvent mouseEvent) {
        for (var selectedContent : this.selectedContent) {
            selectedContent.getSelectedProperty()
                    .set(selectedContent.contentSyncStatusProperty.get()
                            .isOnDevice());
        }
    }

    private void refreshContentTable() {
        var mergedLibraryContent = syncManager.getSyncStatuses().values().stream()
                .map(contentSyncStatus -> new SelectedContent(contentSyncStatus.isOnDevice(), contentSyncStatus))
                .collect(Collectors.toList());
        selectedContent.setAll(mergedLibraryContent);
        contentTable.setItems(selectedContent);
    }

    private void refreshCapacityBar() {
        var attachedDeviceIdx = chcAttached.getSelectionModel().getSelectedIndex();
        var attachedDevice = chcAttached.getItems().get(attachedDeviceIdx);

        var usedCapacity = selectedContent.stream()
                .map(sc -> {
                    if (sc.selectedProperty.get()) {
                        return sc.getContentSyncStatusProperty().get().getContentInfo().getSize();
                    }
                    return 0L;
                })
                .mapToDouble(Long::doubleValue)
                .sum();

        var attachedCapacity = attachedDevice.getDevice().getLibraryRootCapacities().values().stream()
                .mapToDouble(Long::doubleValue)
                .sum();

        var pctUsed = usedCapacity / attachedCapacity;

        if (pctUsed > 1.0) {
            log.warn("Selection has exceeded device capacity");
            btnSyncSelection.setDisable(true);
        } else {
            btnSyncSelection.setDisable(false);
        }

        log.info("used: {}, total: {}, pct: {}", usedCapacity, attachedCapacity, pctUsed);

        lblStorageInfo.setText(String.format("%.2f/%.2f GB",
                usedCapacity / FileUtils.ONE_GB,
                attachedCapacity / FileUtils.ONE_GB));
        prgCapacityBar.setProgress(pctUsed);
    }

    private void handleSelectedDeviceChange() throws InvalidStateException {
        var libDevice = getLibraryDevice();
        var attachedDevice = getAttachedDevice();

        if (libDevice == null || attachedDevice == null) {
            return;
        }

        if (libDevice.getId() == attachedDevice.getId()) {
            throw new InvalidStateException("Cannot use the same device for library and managed device");
        }

        syncManager = new DeviceSyncManager(libDevice, attachedDevice,
                Arrays.asList(LazySyncStrategy.class.getCanonicalName(), GreedySyncStrategy.class.getCanonicalName()));

        refreshContentTable();
        refreshCapacityBar();
    }

    private void setContentTableColumnFactory(TableColumn column, FieldKey fieldKey) {

        column.setCellValueFactory((Callback<TableColumn.CellDataFeatures<SelectedContent, String>, ObservableValue<Object>>) data -> {
            var contentInfo = data.getValue().getContentSyncStatusProperty().get().getContentInfo();
            var value = new SimpleObjectProperty<>();
            switch (fieldKey) {
                case GENRE:
                    value.setValue(contentInfo.getGenre());
                    break;
                case ARTIST:
                    value.setValue(contentInfo.getArtist());
                    break;
                case ALBUM:
                    value.setValue(contentInfo.getAlbum());
                    break;
                case TITLE:
                    value.setValue(contentInfo.getTitle());
                    break;
                case TRACK:
                    value.setValue(contentInfo.getTrackNum());
                    break;
                case DISC_NO:
                    value.setValue(contentInfo.getDiscNum());
                    break;
                default:
                    value.setValue("N/A");
            }
            return value;
        });
    }


    @Data
    class SelectedContent {
        private BooleanProperty selectedProperty = new SimpleBooleanProperty();
        private ObjectProperty<ContentSyncStatus> contentSyncStatusProperty = new SimpleObjectProperty<>();

        SelectedContent(boolean selected, ContentSyncStatus contentSyncStatus) {
            selectedProperty.set(selected);
            selectedProperty.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    log.debug("Content Selection Change");
                    refreshCapacityBar();
                }
            });
            contentSyncStatusProperty.set(contentSyncStatus);
        }
    }
}
