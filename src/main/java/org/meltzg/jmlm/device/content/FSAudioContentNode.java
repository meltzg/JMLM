package org.meltzg.jmlm.device.content;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.meltzg.jmlm.device.FSAudioContentDevice;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

/**
 * File System Audio Content Node
 * Represents either a directory or audio file in a standard filesystem
 *
 * @author Greg Meltzer
 * @author https://github.com/meltzg
 */
public class FSAudioContentNode extends AbstractContentNode {
    protected String genre;
    protected String artist;
    protected String album;
    protected String title;
    protected int discNum;
    protected int trackNum;

    /**
     * Creates a node that can be used as the root of a FSAudio device content
     */
    public FSAudioContentNode() {
        super(ROOT_ID);
    }

    /**
     * Creates a new FSAudioContentNode from the given ID
     * This ID should be the path for the content that this node represents
     * <p>
     * If the underlying path does not exist, the node will be isValid := false
     * If the underlying path is a file, it's info is automatically scraped.
     * If it is not an audio file, isValid := false
     *
     * @param id The path this node will represent (will become an absolute path)
     */
    public FSAudioContentNode(String id) {
        super(id);

        File content = new File(id);
        this.id = content.getAbsolutePath();
        this.pId = content.getParentFile().getAbsolutePath();
        this.origName = content.getName();
        this.isDir = content.isDirectory();
        this.size = !this.isDir ? BigInteger.valueOf(content.length()) : BigInteger.ZERO;
        this.capacity = BigInteger.ZERO;
        this.isValid = true;

        if (!this.isDir) {
            try {
                AudioFile af = AudioFileIO.read(content);
                Tag tag = af.getTag();
                this.genre = tag.getFirst(FieldKey.GENRE);
                this.artist = tag.getFirst(FieldKey.ARTIST);
                this.album = tag.getFirst(FieldKey.ALBUM);
                this.title = tag.getFirst(FieldKey.TITLE);
                this.trackNum = Integer.parseInt(tag.getFirst(FieldKey.TRACK));

                String strDiscNum = tag.getFirst(FieldKey.DISC_NO);
                this.discNum = strDiscNum.length() > 0 ? Integer.parseInt(strDiscNum) : 1;
            } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
                e.printStackTrace();
                System.err.println(content.getAbsolutePath());
                this.isValid = false;
            }
        }
    }

    /**
     * @return the song's genre
     */
    public String getGenre() {
        return genre;
    }

    /**
     * @return the song's artist
     */
    public String getArtist() {
        return artist;
    }

    /**
     * @return the song's album
     */
    public String getAlbum() {
        return album;
    }

    /**
     * @return the song's title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the song's discNum
     */
    public int getDiscNum() {
        return discNum;
    }

    /**
     * @return the song's trackNum
     */
    public int getTrackNum() {
        return trackNum;
    }

    /**
     * @return the node's validity
     */
    public boolean isValid() {
        return isValid;
    }

    @Override
    protected AbstractContentNode getInstance() {
        return new FSAudioContentNode();
    }

    @Override
    protected JsonElement serializeProperties() {
        JsonObject serialized = super.serializeProperties().getAsJsonObject();
        serialized.addProperty("genre", genre);
        serialized.addProperty("artist", artist);
        serialized.addProperty("album", genre);
        serialized.addProperty("discNum", genre);
        serialized.addProperty("trackNum", genre);
        return serialized;
    }

    @Override
    protected void deserializeProperties(JsonElement json) {
        super.deserializeProperties(json);
        JsonObject jsonObject = json.getAsJsonObject();

        if (!isDir) {
            genre = jsonObject.get("genre").getAsString();
            artist = jsonObject.get("artist").getAsString();
            album = jsonObject.get("album").getAsString();
            discNum = jsonObject.get("discNum").getAsInt();
            trackNum = jsonObject.get("trackNum").getAsInt();
        }
    }

    @Override
    protected boolean equalProps(AbstractContentNode other) {
        if (!other.getClass().equals(this.getClass())) {
            return false;
        }
        if (!super.equalProps(other)) {
            return false;
        }

        FSAudioContentNode fsaOther = (FSAudioContentNode) other;
        if (!isDir && (!genre.equals(fsaOther.genre) ||
                !artist.equals(fsaOther.artist) ||
                !album.equals(fsaOther.album) ||
                !title.equals(fsaOther.title) ||
                discNum != fsaOther.discNum ||
                trackNum != fsaOther.trackNum)) {
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        AbstractContentNode root = (new FSAudioContentDevice()).readDeviceContent("/mnt/Data/workspace/testdata/Music");

        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(root.getClass(), root);
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        AbstractContentNode deserialized = gson.fromJson(gson.toJson(root), root.getClass());
//        System.out.println(gson.toJson(root));
    }
}