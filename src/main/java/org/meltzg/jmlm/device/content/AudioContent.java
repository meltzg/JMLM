package org.meltzg.jmlm.device.content;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
@Data
@NoArgsConstructor
public class AudioContent {
    @Id
    @EqualsAndHashCode.Exclude
    private Long id;
    private long size;

    private String genre;
    private String artist;
    private String album;
    private String title;
    private int discNum;
    private int trackNum;

    public AudioContent(long size, String genre, String artist, String album,
                        String title, int discNum, int trackNum) {
        this.size = size;
        this.genre = genre;
        this.artist = artist;
        this.album = album;
        this.title = title;
        this.discNum = discNum;
        this.trackNum = trackNum;

        this.id = (long) hashCode();
    }
}
