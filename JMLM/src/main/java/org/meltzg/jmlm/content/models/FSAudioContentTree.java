package org.meltzg.jmlm.content.models;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
	
}
