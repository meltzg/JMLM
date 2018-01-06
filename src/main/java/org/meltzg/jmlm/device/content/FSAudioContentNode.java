package org.meltzg.jmlm.device.content;

import java.io.File;
import java.io.IOException;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

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
	protected boolean isValid;

	/** Creates a node that can be used as the root of a FSAudio device content */
	public FSAudioContentNode() {
		super(ROOT_ID);
	}
	
	/**
	 * Creates a new FSAudioContentNode from the given ID
	 * This ID should be the path for the content that this node represents
	 * 
	 * If the underlying path does not exist, the node will be isValid := false
	 * If the underlying path is a file, it's info is automatically scraped.
	 * If it is not an audio file, isValid := false
	 * @param id The path this node will represent (will become an absolute path)
	 */
	public FSAudioContentNode(String id) {
		super(id);

		File content = new File(id);
		this.id = content.getAbsolutePath();
		this.pId = content.getParentFile().getAbsolutePath();
		this.origName = content.getName();
		this.isDir = content.isDirectory();
		this.size = !this.isDir ? content.length() : 0;
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

	/** @return the song's genre */
	public String getGenre() {
		return genre;
	}
	/** @return the song's artist */
	public String getArtist() {
		return artist;
	}
	/** @return the song's album */
	public String getAlbum() {
		return album;
	}
	/** @return the song's title */
	public String getTitle() {
		return title;
	}
	/** @return the song's discNum */
	public int getDiscNum() {
		return discNum;
	}
	/** @return the song's trackNum */
	public int getTrackNum() {
		return trackNum;
	}
	/** @return the node's validity */
	public boolean isValid() {
		return isValid;
	}
}