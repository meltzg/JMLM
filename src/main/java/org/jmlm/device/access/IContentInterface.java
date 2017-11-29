package org.jmlm.device.access;

import java.io.File;
import java.util.List;
import org.jmlm.device.content.AbstractContentNode;

public interface IContentInterface {
    List<String> getChildIds(String pId);

    AbstractContentNode createDirNode(String pId, String name);
    AbstractContentNode createContentNode(String pId, File file);
    boolean deleteNode(String id);
    boolean retrieveNode(String id, String destFolder); 
    
    AbstractContentNode transferToDevice(String filepath, String destId);
    boolean transferFromDevice(String id, String destFolder);
    boolean removeFromDevice(String id);
    AbstractContentNode moveOnDevice(String id, String destId, String destFolderPath, String tmpFolder);
}