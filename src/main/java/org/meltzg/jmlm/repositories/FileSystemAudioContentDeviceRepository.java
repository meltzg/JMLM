package org.meltzg.jmlm.repositories;

import org.meltzg.jmlm.device.FileSystemAudioContentDevice;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

public interface FileSystemAudioContentDeviceRepository extends CrudRepository<FileSystemAudioContentDevice, Long> {
}
