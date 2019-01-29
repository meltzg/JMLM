package org.meltzg.jmlm.device.content;

import org.meltzg.jmlm.db.AbstractDBService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class AudioContentService extends AbstractDBService {
    private static final ColumnDefinition CROSS_DEVICE_ID;
    private static final ColumnDefinition SIZE;
    private static final ColumnDefinition GENRE;
    private static final ColumnDefinition ARTIST;
    private static final ColumnDefinition ALBUM;
    private static final ColumnDefinition TITLE;
    private static final ColumnDefinition DISC_NUM;
    private static final ColumnDefinition TRACK_NUM;

    public AudioContentService() throws SQLException, ClassNotFoundException {
        super();
    }

    public void save(AudioContent content) throws SQLException, ClassNotFoundException {
        var insertQuery = String.format("INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);",
                TABLE_NAME(),
                ID.getName(),
                CROSS_DEVICE_ID.getName(),
                SIZE.getName(),
                GENRE.getName(),
                ARTIST.getName(),
                ALBUM.getName(),
                TITLE.getName(),
                DISC_NUM.getName(),
                TRACK_NUM.getName());
        var params = Arrays.asList(
                new StatementParameter(content.getId(), ID.getType()),
                new StatementParameter(content.getCrossDeviceId(), CROSS_DEVICE_ID.getType()),
                new StatementParameter(content.getSize(), SIZE.getType()),
                new StatementParameter(content.getGenre(), GENRE.getType()),
                new StatementParameter(content.getArtist(), ARTIST.getType()),
                new StatementParameter(content.getAlbum(), ALBUM.getType()),
                new StatementParameter(content.getTitle(), TITLE.getType()),
                new StatementParameter(content.getDiscNum(), DISC_NUM.getType()),
                new StatementParameter(content.getTrackNum(), TRACK_NUM.getType())
        );

        try (var conn = getConnection()) {
            executeUpdate(insertQuery, params, conn);
        }
    }

    public AudioContent get(String crossDeviceId) throws SQLException, ClassNotFoundException {
        var selectQuery = String.format("SELECT * FROM %s WHERE %s = ?;", TABLE_NAME(), CROSS_DEVICE_ID.getName());
        var params = Collections.singletonList(new StatementParameter(crossDeviceId, CROSS_DEVICE_ID.getType()));
        AudioContent content = null;
        try (var conn = getConnection()) {
            var rs = executeQuery(selectQuery, params, conn);
            if (rs.next()) {
                content = extractAudioContentProps(rs);
            }
        }
        return content;
    }

    public int delete(String crossDeviceId) throws SQLException, ClassNotFoundException {
        var content = get(crossDeviceId);
        var numDeleted = 0;
        if (content != null) {
            try (var conn = getConnection()) {
                numDeleted = deleteById(content.getId(), conn);
            }
        }
        return numDeleted;
    }

    @Override
    public String TABLE_NAME() {
        return "audio_content";
    }

    @Override
    protected List<ColumnDefinition> getColumnDefinitions() {
        var definitions = super.getColumnDefinitions();
        definitions.add(CROSS_DEVICE_ID);
        definitions.add(SIZE);
        definitions.add(GENRE);
        definitions.add(ARTIST);
        definitions.add(ALBUM);
        definitions.add(TITLE);
        definitions.add(DISC_NUM);
        definitions.add(TRACK_NUM);
        return definitions;
    }

    private AudioContent extractAudioContentProps(ResultSet rs) throws SQLException {
        var id = (UUID) rs.getObject(ID.getName());
        var crossDeviceId = rs.getString(CROSS_DEVICE_ID.getName());
        var size = rs.getLong(SIZE.getName());
        var genre = rs.getString(GENRE.getName());
        var artist = rs.getString(ARTIST.getName());
        var album = rs.getString(ALBUM.getName());
        var title = rs.getString(TITLE.getName());
        var discNum = rs.getInt(DISC_NUM.getName());
        var trackNum = rs.getInt(TRACK_NUM.getName());

        return new AudioContent(id, crossDeviceId, size, genre, artist, album, title, discNum, trackNum);
    }

    static {
        CROSS_DEVICE_ID = new ColumnDefinition("crossDeviceId", DBType.TEXT, UniqueType.UNIQUE);
        SIZE = new ColumnDefinition("size", DBType.BIGINT);
        GENRE = new ColumnDefinition("genre", DBType.TEXT);
        ARTIST = new ColumnDefinition("artist", DBType.TEXT);
        ALBUM = new ColumnDefinition("album", DBType.TEXT);
        TITLE = new ColumnDefinition("title", DBType.TEXT);
        DISC_NUM = new ColumnDefinition("discNum", DBType.INT);
        TRACK_NUM = new ColumnDefinition("trackNum", DBType.INT);
    }
}
