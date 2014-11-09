package com.sbernatsky.tests.king.server.core;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Class encapsulates game level information. Currently it holds only up to 15 of top scores.
 * <p>
 * Current implementation uses exclusive locks when updating and retrieving top scores. Depending on the memory
 * requirements and usage patters (read/write ration) better algorithms and strucuter may be used.<br/>
 * If number of reads is greater then number of writes it is possible to use {@linkplain ReadWriteLock} to synchronize
 * access to level scores.<br/>
 * If we need to keep in memory as much levels as possible it is possible to pack scores lisy into sorted long array
 * where high word is the score and lower word is the user id:
 * 
 * <pre>
 * private final long[] scores = new long[MAX_SCORES_COUNT];
 * 
 * private int getUser(int idx) {
 *     return (int) scores[idx] &amp; 0xFFFFFFFF;
 * }
 * 
 * private int getScore(int idx) {
 *     return (int) (scores[idx] &gt;&gt;&gt; 32) &amp; 0xFFFFFFFF;
 * }
 * </pre>
 * 
 * Such packing scheme would preserve data locality while making possible to use {@linkplain Arrays#sort(long[])} method
 * for sorting/lookup.
 * 
 * </p>
 */
public class LevelSynchronizedImpl extends Level {
    private final Object lock = new Object();

    @Override
    public boolean addScore(User user, int score) {
        // FIXME: not efficient, consider switching to array of level scores or some packing
        // TODO: test performance using jmh
        synchronized (lock) {
            return doAddScore(user, score);
        }
    }

    @Override
    public List<LevelScore> getScores() {
        synchronized (lock) {
            return doGetScores();
        }
    }
}
