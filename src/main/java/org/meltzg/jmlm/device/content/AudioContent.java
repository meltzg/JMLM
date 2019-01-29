package org.meltzg.jmlm.device.content;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;
import java.util.UUID;

public class AudioContent {
    protected String id;
    protected UUID libraryId;
    protected String libraryPath;
    protected long size;

    protected final StringProperty genre;
    protected final StringProperty artist;
    protected final StringProperty album;
    protected final StringProperty title;
    protected final IntegerProperty discNum;
    protected final IntegerProperty trackNum;

    public AudioContent(String id, UUID libraryId, String libraryPath, long size, String genre,
                        String artist, String album, String title, int discNum, int trackNum) {
        this.id = id;
        this.libraryId = libraryId;
        this.libraryPath = libraryPath;
        this.size = size;
        this.genre = new SimpleStringProperty(genre);
        this.artist = new SimpleStringProperty(artist);
        this.album = new SimpleStringProperty(album);
        this.title = new SimpleStringProperty(title);
        this.discNum = new SimpleIntegerProperty(discNum);
        this.trackNum = new SimpleIntegerProperty(trackNum);
    }

    public AudioContent() {
        this.genre = new SimpleStringProperty();
        this.artist = new SimpleStringProperty();
        this.album = new SimpleStringProperty();
        this.title = new SimpleStringProperty();
        this.discNum = new SimpleIntegerProperty();
        this.trackNum = new SimpleIntegerProperty();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UUID getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(UUID libraryId) {
        this.libraryId = libraryId;
    }

    public String getLibraryPath() {
        return libraryPath;
    }

    public void setLibraryPath(String libraryPath) {
        this.libraryPath = libraryPath;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getGenre() {
        return genre.get(); }

    public void setGenre(String genre) {
        this.genre.set(genre);
    }

    public String getArtist() {
        return artist.get();
    }

    public void setArtist(String artist) {
        this.artist.set(artist);
    }

    public String getAlbum() {
        return album.get();
    }

    public void setAlbum(String album) {
        this.album.set(album);
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public int getDiscNum() {
        return discNum.get();
    }

    public void setDiscNum(int discNum) {
        this.discNum.set(discNum);
    }

    public int getTrackNum() {
        return trackNum.get();
    }

    public void setTrackNum(int trackNum) {
        this.trackNum.set(trackNum);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioContent that = (AudioContent) o;
        return size == that.size &&
                discNum == that.discNum &&
                trackNum == that.trackNum &&
                id.equals(that.id) &&
                libraryPath.equals(that.libraryPath) &&
                genre.equals(that.genre) &&
                artist.equals(that.artist) &&
                album.equals(that.album) &&
                title.equals(that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, libraryPath, size, genre, artist, album, title, discNum, trackNum);
    }
}
