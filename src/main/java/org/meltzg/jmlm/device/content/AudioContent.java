package org.meltzg.jmlm.device.content;

import java.util.Objects;

public class AudioContent {
    private String libraryPath;
    private long size;

    public long getSize() {
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioContent that = (AudioContent) o;
        return getSize() == that.getSize() &&
                libraryPath.equals(that.libraryPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(libraryPath, getSize());
    }
}
