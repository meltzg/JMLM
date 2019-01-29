package org.meltzg.jmlm.device.content;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.Assert.*;

public class AudioContentServiceTest {

    private AudioContentService service;

    @Before
    public void setUp() throws SQLException, ClassNotFoundException {
        this.service = new AudioContentService();
    }

    @After
    public void tearDown() throws SQLException, ClassNotFoundException {
        service.dropTable();
    }

    @Test
    public void testCRUDOps() throws SQLException, ClassNotFoundException {
        var content = new AudioContent(
                UUID.randomUUID(),
                "12345",
                42,
                "jam",
                "Grateful Dead",
                "Aoxomoxoa",
                "St. Stephen",
                1,
                1);
        service.save(content);
        var retrieved = service.get(content.getCrossDeviceId());
        assertEquals(content, retrieved);
        assertEquals(1, service.delete(content.getCrossDeviceId()));
        assertNull(service.get(content.getCrossDeviceId()));
    }

    @Test(expected = SQLException.class)
    public void testDuplicateCrossDeviceId() throws SQLException, ClassNotFoundException {
        var content = new AudioContent(
                UUID.randomUUID(),
                "12345",
                42,
                "jam",
                "Grateful Dead",
                "Aoxomoxoa",
                "St. Stephen",
                1,
                1);
        var content2 = new AudioContent(
                UUID.randomUUID(),
                "12345",
                42,
                "jam",
                "Grateful Dead",
                "Aoxomoxoa",
                "St. Stephen",
                1,
                1);
        service.save(content);
        service.save(content2);
    }

    @Test
    public void testGetNotFound() throws SQLException, ClassNotFoundException {
        assertNull(service.get("67890"));
    }

    @Test
    public void testDeleteNotFound() throws SQLException, ClassNotFoundException {
        assertEquals(0, service.delete("67890"));
    }
}