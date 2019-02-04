package org.meltzg.jmlm.device.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AudioContent {
    @Id
    private String id;
    private long size;

    private String genre;
    private String artist;
    private String album;
    private String title;
    private int discNum;
    private int trackNum;
}
