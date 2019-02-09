package org.meltzg.jmlm.ui.components.controls;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.validation.ValidationSupport;
import org.meltzg.jmlm.device.FileSystemAudioContentDevice;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class LibraryRootSelectionPane extends ValidatableControl {
    private final FileSystemAudioContentDevice device;
    @FXML
    private TreeView deviceTree;

    public LibraryRootSelectionPane(FileSystemAudioContentDevice device) {
        super();
        this.device = device;
        deviceTree.setRoot(createNode(new PathWrapper(Paths.get(device.getRootPath()))));
    }

    @Override
    public void registerValidators(ValidationSupport vs) {

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
                    var location = getValue();
                    try {
                        isLeaf = device.getChildrenDirs(location.path).size() == 0;
                    } catch (IllegalAccessException | IOException e) {
                        log.error("Could not determine if location is a leaf: " + location, e);
                        isLeaf = true;
                    }
                }
                return isLeaf;
            }

            private ObservableList<TreeItem<PathWrapper>> buildChildren(TreeItem<PathWrapper> treeItem) {
                var location = getValue();
                try {
                    var children = device.getChildrenDirs(location.path);
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

    @AllArgsConstructor
    class PathWrapper {
        private Path path;

        @Override
        public String toString() {
            if (path.getFileName() == null) {
                return path.toString();
            }
            return path.getFileName().toString();
        }
    }
}
