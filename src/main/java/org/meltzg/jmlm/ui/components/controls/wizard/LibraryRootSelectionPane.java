package org.meltzg.jmlm.ui.components.controls.wizard;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.validation.ValidationSupport;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;
import org.meltzg.jmlm.ui.types.PathWrapper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class LibraryRootSelectionPane extends ValidatableControl {
    private final FileSystemAudioContentDevice device;

    @FXML
    private TreeView<PathWrapper> deviceTree;

    @FXML
    private ListView<Path> lstLibraryRoots;

    private Set<Path> libraryRoots;

    public LibraryRootSelectionPane(FileSystemAudioContentDevice device) {
        super();
        this.device = device;
        libraryRoots = new HashSet<>();
        deviceTree.setRoot(createNode(new PathWrapper(Paths.get(device.getRootPath()))));
    }

    @Override
    public void registerValidators(ValidationSupport vs) {
    }

    @Override
    public Map<String, Object> getAdditionalSettings() {
        var settings = new HashMap<String, Object>();
        settings.put("libraryRoots", libraryRoots);
        return settings;
    }

    @FXML
    public void addSelectedItem(ActionEvent actionEvent) {
        var selection = deviceTree.getSelectionModel().getSelectedItem();
        if (selection != null) {
            var newRoot = selection.getValue().getPath();
            for (var existingPath : libraryRoots) {
                if (existingPath.startsWith(newRoot) || newRoot.startsWith(existingPath)) {
                    var alert = new Alert(
                            Alert.AlertType.ERROR,
                            "Cannot add parent or child of an existing selection");
                    alert.showAndWait();
                    return;
                }
            }
            libraryRoots.add(newRoot);
            lstLibraryRoots.getItems().setAll(libraryRoots);
        }
    }

    @FXML
    public void removeSelectedItem(ActionEvent actionEvent) {
        var selection = lstLibraryRoots.getSelectionModel().getSelectedItem();
        if (selection != null) {
            libraryRoots.remove(selection);
            lstLibraryRoots.getItems().setAll(libraryRoots);
        }
    }

    private TreeItem<PathWrapper> createNode(final PathWrapper location) {
        return new TreeItem<>(location) {
            private boolean isLeaf;
            private boolean isFirstTimeChildren = true;
            private boolean isFirstTimeLeaf = true;

            @Override
            public ObservableList<TreeItem<PathWrapper>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;
                    super.getChildren().setAll(buildChildren(this));
                }
                return super.getChildren();
            }

            @Override
            public boolean isLeaf() {
                if (isFirstTimeLeaf) {
                    isFirstTimeLeaf = false;
                    var location1 = getValue();
                    try {
                        isLeaf = device.getChildrenDirs(location1.getPath()).size() == 0;
                    } catch (IllegalAccessException | IOException e) {
                        log.error("Could not determine if location is a leaf: " + location1, e);
                        isLeaf = true;
                    }
                }
                return isLeaf;
            }

            private ObservableList<TreeItem<PathWrapper>> buildChildren(TreeItem<PathWrapper> treeItem) {
                var location = treeItem.getValue();
                try {
                    var children = device.getChildrenDirs(location.getPath());
                    children.sort(new Comparator<Path>() {
                        @Override
                        public int compare(Path o1, Path o2) {
                            return o1.compareTo(o2);
                        }
                    });
                    if (children.size() > 0) {
                        ObservableList<TreeItem<PathWrapper>> observableChildren = FXCollections.observableArrayList();
                        for (var child : children) {
                            observableChildren.add(createNode(new PathWrapper(child)));
                        }
                        return observableChildren;
                    }
                } catch (IllegalAccessException | IOException e) {
                    log.error("Could not build children for location: " + location, e);
                }
                return FXCollections.emptyObservableList();
            }
        };
    }
}
