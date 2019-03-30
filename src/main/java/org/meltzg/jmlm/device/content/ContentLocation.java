package org.meltzg.jmlm.device.content;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ContentLocation {
    @Id
    private String id;
    private Long contentId;
    private UUID libraryId;
    private String librarySubPath;

    public ContentLocation(Long contentId, UUID libraryId, String librarySubPath) {
        id = UUID.randomUUID().toString();
        this.contentId = contentId;
        this.libraryId = libraryId;
        this.librarySubPath = librarySubPath;
    }
}
