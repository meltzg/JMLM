package org.meltzg.jmlm.device.content;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.HashCodeExclude;

import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
@Data
@NoArgsConstructor
public class AudioContent {
    @Id
    @HashCodeExclude
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
