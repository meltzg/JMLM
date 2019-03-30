package org.meltzg.jmlm.repositories;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meltzg.jmlm.device.content.AudioContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class AudioContentRepositoryTest {
    @Autowired
    AudioContentRepository contentRepo;

    @Test
    public void testAddDuplicate() {
        var content1 = new AudioContent(
                100,
                "genre",
                "artist",
                "album",
                "title",
                1,
                1);
        var content2 = new AudioContent(
                100,
                "genre",
                "artist",
                "album",
                "title",
                1,
                1);

        var saved1 = contentRepo.save(content1);
        var saved2 = contentRepo.save(content2);

        assertEquals(saved1, saved2);
        assertEquals(1, contentRepo.count());
    }
}
