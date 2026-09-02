package com.devforge.storage;

import java.util.Collection;
import java.util.Optional;

public interface ProjectFileStorage {

    String storageKey(Long projectId, String path);

    void write(String storageKey, String content);

    Optional<String> read(String storageKey);

    void delete(String storageKey);

    void deleteAll(Collection<String> storageKeys);
}
