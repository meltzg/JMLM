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
    String id;
    long size;

    String genre;
    String artist;
    String album;
    String title;
    int discNum;
    int trackNum;
}
