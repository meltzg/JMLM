package org.meltzg.jmlm.content.models;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

public class FSAudioContentTree extends AbstractContentTree {

	private String path;
	private String genre;
	private String artist;
	private String album;
	private String title;
	private int discNum;
	private int trackNum;

	public FSAudioContentTree(String parentId, String origName, String path, BigInteger size) {
		
		UUID id = UUID.randomUUID();
		init(id.toString(), parentId, origName, path, size, BigInteger.ZERO, new ArrayList<AbstractContentTree>());
	}

	public FSAudioContentTree(FSAudioContentTree other) {
		init(other.id, other.parentId, other.origName, other.path, other.size, other.capacity, other.children);
	}

	protected void init(String id, String parentId, String origName, String path, BigInteger size, BigInteger capacity,
			List<AbstractContentTree> children) {
		super.init(id, parentId, origName, size, capacity, children);
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getDiscNum() {
		return discNum;
	}

	public void setDiscNum(int discNum) {
		this.discNum = discNum;
	}

	public int getTrackNum() {
		return trackNum;
	}

	public void setTrackNum(int trackNum) {
		this.trackNum = trackNum;
	}

	@Override
	protected String toString(boolean includeChildren) {
		String str = "FSAudioContentTree [path=" + path + ", genre=" + genre + ", artist=" + artist + ", album=" + album
				+ ", title=" + title + ", discNum=" + discNum + ", trackNum=" + trackNum + ", id=" + id + ", parentId="
				+ parentId + ", origName=" + origName + ", size=" + size + ", capacity=" + capacity;
		
		if (includeChildren) {
			str += ", children=" + children;
		}
		
		str += "]";
		
		return str;
	}
	
	public static FSAudioContentTree createNode(String pId, File file) {
		FSAudioContentTree cNode = new FSAudioContentTree(pId, file.getName(), file.getAbsolutePath(),
				BigInteger.ZERO);

		boolean isValid = true;
		if (!file.isDirectory()) {
			try {
				AudioFile af = AudioFileIO.read(file);
				Tag tag = af.getTag();
				cNode.setAlbum(tag.getFirst(FieldKey.ALBUM));
				cNode.setArtist(tag.getFirst(FieldKey.ARTIST));
				String strDiscNum = tag.getFirst(FieldKey.DISC_NO);
				cNode.setDiscNum(strDiscNum.length() > 0 ? Integer.parseInt(strDiscNum) : 1);
				cNode.setGenre(tag.getFirst(FieldKey.GENRE));
				cNode.setTitle(tag.getFirst(FieldKey.TITLE));
				cNode.setTrackNum(Integer.parseInt(tag.getFirst(FieldKey.TRACK)));
			} catch (CannotReadException | IOException | TagException | ReadOnlyFileException
					| InvalidAudioFrameException e) {
				isValid = false;
				System.err.println(file.getAbsolutePath());
			}
			cNode.setSize(BigInteger.valueOf(file.length()));
		}
		
		return isValid ? cNode : null;
	}
}
