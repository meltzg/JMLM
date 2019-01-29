package org.meltzg.jmlm.device.content;

import java.util.Objects;
import java.util.UUID;

public class AudioContent {
    protected String id;
    protected UUID libraryId;
    protected String libraryPath;
    protected long size;

    protected String genre;
    protected String artist;
    protected String album;
    protected String title;
    protected int discNum;
    protected int trackNum;

    public AudioContent(String id, UUID libraryId, String libraryPath, long size, String genre, String artist, String album, String title, int discNum, int trackNum) {
        this.id = id;
        this.libraryId = libraryId;
        this.libraryPath = libraryPath;
        this.size = size;
        this.genre = genre;
        this.artist = artist;
        this.album = album;
        this.title = title;
        this.discNum = discNum;
        this.trackNum = trackNum;
    }

    public AudioContent() {
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
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDiscNum() {
        return discNum;
    }

    public void setDiscNum(int discNum) {
        this.discNum = discNum;
    }

    public int getTrackNum() {
        return trackNum;
    }

    public void setTrackNum(int trackNum) {
        this.trackNum = trackNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioContent that = (AudioContent) o;
        return getSize() == that.getSize() &&
                getDiscNum() == that.getDiscNum() &&
                getTrackNum() == that.getTrackNum() &&
                getId().equals(that.getId()) &&
                getGenre().equals(that.getGenre()) &&
                getArtist().equals(that.getArtist()) &&
                getAlbum().equals(that.getAlbum()) &&
                getTitle().equals(that.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getSize(), getGenre(),
                getArtist(), getAlbum(), getTitle(), getDiscNum(), getTrackNum());
    }
}
