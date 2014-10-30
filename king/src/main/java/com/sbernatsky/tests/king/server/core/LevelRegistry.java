package com.sbernatsky.tests.king.server.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Usually levels are stored somewhere and are loaded through some service. */
public class LevelRegistry {
    private final ConcurrentMap<Integer, Level> levelSynchronizedImpls = new ConcurrentHashMap<>();

    public Level getOrCreateLevel(int levelId) {
        Level level = newLevel();
        Level existing = levelSynchronizedImpls.putIfAbsent(levelId, level);
        return (existing != null) ? existing : level;
    }

    private Level newLevel() {
//        return new LevelSynchronizedImpl();
        return new LevelRWLockImpl();
    }

    public Level getLevel(int levelId) {
        return levelSynchronizedImpls.get(levelId);
    }
}
