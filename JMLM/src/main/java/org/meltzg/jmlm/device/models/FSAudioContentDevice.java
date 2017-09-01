package org.meltzg.jmlm.device.models;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Stack;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.meltzg.jmlm.content.models.ContentRoot;
import org.meltzg.jmlm.content.models.FSAudioContentTree;

public class FSAudioContentDevice extends AbstractContentDevice {

	public FSAudioContentDevice(String deviceId, String friendlyName, String description, String manufacturer) {
		super(deviceId, friendlyName, description, manufacturer);

	}

	@Override
	public void buildContentRoot() {
		this.contentRoot = new ContentRoot(new FSAudioContentTree("", "ROOT", "", BigInteger.ZERO));
	}

	@Override
	public void addContentRoot(String rootPath) {
		if (this.contentRoot == null) {
			buildContentRoot();
		}

		File tmp = new File(rootPath);
		if (!tmp.exists()) {
			System.err.println("!!! '" + rootPath + "' does not exist");
		} else if (!tmp.isDirectory()) {
			System.err.println("!!! '" + rootPath + "' is not a directory");
		} else {
			FSAudioContentTree tmpRoot = new FSAudioContentTree(this.contentRoot.getId(), rootPath, rootPath,
					BigInteger.ZERO);

			// walk directory and retrieve nodes
			// Map<String, FSAudioContentTree> idToNodes = new HashMap<String,
			// FSAudioContentTree>();
			Stack<FSAudioContentTree> nodes = new Stack<FSAudioContentTree>();
			nodes.push(tmpRoot);
			while (!nodes.empty()) {
				FSAudioContentTree node = nodes.pop();

				// idToNodes.put(node.getId(), node);

				File[] children = new File(node.getPath()).listFiles();
				if (children == null) {
					continue;
				}

				for (File c : children) {
					FSAudioContentTree cNode = new FSAudioContentTree(node.getId(), c.getName(), c.getAbsolutePath(),
							BigInteger.ZERO);

					boolean isValid = true;
					if (!c.isDirectory()) {
						try {
							AudioFile af = AudioFileIO.read(c);
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
							System.err.println(c.getAbsolutePath());
						}
						cNode.setSize(BigInteger.valueOf(c.length()));
					}

					if (isValid) {
						nodes.push(cNode);
						node.getChildren().add(cNode);
					}
				}

			}

			this.contentRoot.getChildren().add(tmpRoot);
			this.contentRoot.buildRootInfo();
			this.libraryRoots.put(tmpRoot.getId(), new ContentRoot(tmpRoot));
		}
	}
}
