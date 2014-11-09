package com.sbernatsky.tests.king.server.core;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LevelRWLockImpl extends Level {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public boolean addScore(User user, int score) {
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            return doAddScore(user, score);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public List<LevelScore> getScores() {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return doGetScores();
        } finally {
            readLock.unlock();
        }
    }
}
