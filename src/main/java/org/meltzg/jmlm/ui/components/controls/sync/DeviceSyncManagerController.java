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
    @Setter
    private FXMLDialog dialog;

    @Autowired
    FileSystemAudioContentDeviceRepository deviceRepository;
    @Autowired
    AudioContentRepository contentRepository;

    @FXML
    private ChoiceBox<DeviceWrapper> chcLibrary;
    @FXML
    private ChoiceBox<DeviceWrapper> chcAttached;
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

    public void syncSelection(MouseEvent mouseEvent) throws InsufficientSpaceException, SyncStrategyException, TagException, IOException, CannotReadException, ReadOnlyFileException, InvalidAudioFrameException, ClassNotFoundException {
        var desiredContent = selectedContent.stream()
                .filter((selection) -> selection.getSelectedProperty().get())
                .map(SelectedContent::getContentSyncStatusProperty)
                .map(ObjectProperty::get)
                .map(ContentSyncStatus::getContentInfo)
                .map(AudioContent::getId)
                .collect(Collectors.toSet());

//        try {
        syncManager.syncDevice(desiredContent, NotInLibraryStrategy.CANCEL_SYNC);
        refreshContentTable();
//        } catch (ClassNotFoundException | IOException | InsufficientSpaceException |
//                SyncStrategyException | ReadOnlyFileException | TagException | InvalidAudioFrameException |
//                CannotReadException e) {
//            log.error("Error syncing selected content to device", e);
//
//            showAlert("Unexpected Error",
//                    "Sync Failure",
//                    e.getMessage(),
//                    Alert.AlertType.ERROR);
//        }
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
        var libDeviceIdx = chcLibrary.getSelectionModel().getSelectedIndex();
        var attachedDeviceIdx = chcAttached.getSelectionModel().getSelectedIndex();

        log.info("Library: {}, Attached: {}", libDeviceIdx, attachedDeviceIdx);

        if (libDeviceIdx == -1 || attachedDeviceIdx == -1) {
            return;
        }

        if (libDeviceIdx == attachedDeviceIdx) {
            throw new InvalidStateException("Cannot use the same device for library and managed device");
        }

        var libDevice = chcLibrary.getItems().get(libDeviceIdx);
        var attachedDevice = chcAttached.getItems().get(attachedDeviceIdx);

        syncManager = new DeviceSyncManager(libDevice.getDevice(), attachedDevice.getDevice(),
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
