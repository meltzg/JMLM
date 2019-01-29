package org.meltzg.jmlm.device.content;

import java.util.Objects;
import java.util.UUID;

public class AudioContent {
    private UUID id;
    private String crossDeviceId;
    private long size;

    private String genre;
    private String artist;
    private String album;
    private String title;
    private int discNum;
    private int trackNum;

    public AudioContent() {}

    public AudioContent(UUID id, String crossDeviceId, long size, String genre, String artist, String album,
                        String title, int discNum, int trackNum) {
        this.id = id;
        this.crossDeviceId = crossDeviceId;
        this.size = size;
        this.genre = genre;
        this.artist = artist;
        this.album = album;
        this.title = title;
        this.discNum = discNum;
        this.trackNum = trackNum;
    }

    public UUID getId() {
        return id;
    }

    public String getCrossDeviceId() {
        return crossDeviceId;
    }

    public long getSize() {
        return size;
    }

    public String getGenre() {
        return genre;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getTitle() {
        return title;
    }

    public int getDiscNum() {
        return discNum;
    }

    public int getTrackNum() {
        return trackNum;
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
                getCrossDeviceId().equals(that.getCrossDeviceId()) &&
                getGenre().equals(that.getGenre()) &&
                getArtist().equals(that.getArtist()) &&
                getAlbum().equals(that.getAlbum()) &&
                getTitle().equals(that.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCrossDeviceId(), getSize(), getGenre(), getArtist(), getAlbum(), getTitle(), getDiscNum(), getTrackNum());
    }
}
