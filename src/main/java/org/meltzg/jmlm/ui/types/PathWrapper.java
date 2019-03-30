package org.meltzg.jmlm.ui.types;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.file.Path;

@AllArgsConstructor
public class PathWrapper {
    @Getter
    private Path path;

    @Override
    public String toString() {
        if (path.getFileName() == null) {
            return path.toString();
        }
        return path.getFileName().toString();
    }
}