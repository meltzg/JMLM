package org.meltzg.jmlm.ui.types;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.Paths;

@AllArgsConstructor
public class PathWrapper {
    @Getter
    private String path;

    @Override
    public String toString() {
        var pathPath = Paths.get(path);
        if (pathPath.getFileName() == null) {
            return path;
        }
        return pathPath.getFileName().toString();
    }
}