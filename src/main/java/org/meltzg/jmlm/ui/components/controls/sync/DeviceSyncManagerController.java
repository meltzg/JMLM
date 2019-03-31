package org.meltzg.jmlm.ui.components.controls.sync;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.Callback;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.tag.FieldKey;
import org.meltzg.jmlm.exceptions.InvalidStateException;
import org.meltzg.jmlm.repositories.FileSystemAudioContentDeviceRepository;
import org.meltzg.jmlm.sync.ContentSyncStatus;
import org.meltzg.jmlm.sync.DeviceSyncManager;
import org.meltzg.jmlm.sync.strategies.GreedySyncStrategy;
import org.meltzg.jmlm.sync.strategies.LazySyncStrategy;
import org.meltzg.jmlm.ui.components.DialogController;
import org.meltzg.jmlm.ui.components.FXMLDialog;
import org.meltzg.jmlm.ui.types.DeviceWrapper;
import org.springframework.beans.factory.annotation.Autowired;

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

    @FXML
    private ChoiceBox<DeviceWrapper> chcLibrary;
    @FXML
    private ChoiceBox<DeviceWrapper> chcAttached;
    @FXML
    public TableView contentTable;
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
    ObservableSet<ContentSyncStatus> selectedContent;
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

        selectedContent = FXCollections.observableSet();

        colOnDevice.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ContentSyncStatus, Boolean>, ObservableValue>) param ->
                new SimpleBooleanProperty(param.getValue().isOnDevice()));
        colOnDevice.setCellFactory(CheckBoxTableCell.forTableColumn(colOnDevice));

        setContentTableColumnFactory(colTitle, FieldKey.TITLE);
        setContentTableColumnFactory(colArtist, FieldKey.ARTIST);
        setContentTableColumnFactory(colAlbum, FieldKey.ALBUM);
        setContentTableColumnFactory(colGenre, FieldKey.GENRE);
        setContentTableColumnFactory(colTrack, FieldKey.TRACK);
        setContentTableColumnFactory(colDisc, FieldKey.DISC_NO);
    }

    public void refreshDevices() {
        var devices = new ArrayList<DeviceWrapper>();
        for (var device : deviceRepository.findAll()) {
            devices.add(new DeviceWrapper(device));
        }
        log.info(devices.stream().map(dev -> dev.getDevice().toString()).collect(Collectors.joining()));
        chcLibrary.getItems().setAll(devices);
        chcAttached.getItems().setAll(devices);
    }

    private void refreshContentTable() {
        contentTable.getItems().setAll(syncManager.getSyncStatuses().values());
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
    }

    private void setContentTableColumnFactory(TableColumn column, FieldKey fieldKey) {

        column.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ContentSyncStatus, String>, ObservableValue<Object>>) data -> {
            var contentInfo = data.getValue().getContentInfo();
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
}
