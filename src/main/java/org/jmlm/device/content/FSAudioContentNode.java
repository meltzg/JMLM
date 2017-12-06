package org.jmlm.device.content;

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

public class FSAudioContentNode extends AbstractContentNode {
	protected static final String ROOT_ID = "DEVICE";

    protected String genre;
	protected String artist;
	protected String album;
	protected String title;
	protected int discNum;
	protected int trackNum;
	protected boolean isValid;

	public FSAudioContentNode() {
		super(ROOT_ID);
	}
	
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

	public String getGenre() {
		return genre;
	}
	public String getArtist() {
		return artist;
	}
	public String getAlbum() {
		return album;
	}
	public String getTitle() {
		return title;
	}
	public int getDiscNum() {
		return discNum;
	}
	public int getTrackNum() {
		return trackNum;
	}
	public boolean isValid() {
		return isValid;
	}
}