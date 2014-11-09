package com.sbernatsky.tests.king.jmh;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.sbernatsky.tests.king.server.core.LevelScore;

import com.sbernatsky.tests.king.server.core.Level;

import com.sbernatsky.tests.king.server.core.LevelRWLockImpl;
import com.sbernatsky.tests.king.server.core.LevelSynchronizedImpl;
import com.sbernatsky.tests.king.server.core.User;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class JMHLevelSync {
    private static final int USERS_COUNT = 32 * 1024;
    private static final int MAX_SCORE = 32 * 2 * 1024;

    private User[] users;
    private int[] scores;

    @State(Scope.Thread)
    public static class Counter {
        int seed = ThreadLocalRandom.current().nextInt(USERS_COUNT);
    }

    @Setup
    public void setUp() {
        users = new User[USERS_COUNT];
        scores = new int[USERS_COUNT];
        Random random = new Random();
        for (int i = 0; i < USERS_COUNT; i++) {
            users[i] = new User(random.nextInt(USERS_COUNT/16));
            scores[i] = random.nextInt(MAX_SCORE);
        }
    }

    @State(Scope.Thread)
    public static class Plain {
        LevelImpl level = new LevelImpl();

        {
            Random random = new Random();
            for (int i = 0; i < USERS_COUNT; i++) {
                level.addScore(new User(random.nextInt(USERS_COUNT/16)), random.nextInt(MAX_SCORE));
            }
        }
    }

    @Benchmark
    @Group("plain")
    @GroupThreads(3)
    public Object getScoresPlain(Plain s) {
        return s.level.getScores();
    }

    @Benchmark
    @Group("plain")
    @GroupThreads(1)
    public boolean addScorePlain(Plain s, Counter counter) {
        int idx = (counter.seed++) % USERS_COUNT;
        return s.level.addScore(users[idx], scores[idx]);
    }

    @State(Scope.Group)
    public static class Sync {
        LevelSynchronizedImpl level = new LevelSynchronizedImpl();
    }

    @Benchmark
    @Group("synchronized")
    @GroupThreads(3)
    public Object getScoresSync(Sync s) {
        return s.level.getScores();
    }

    @Benchmark
    @Group("synchronized")
    @GroupThreads(1)
    public boolean addScoreSync(Sync s, Counter counter) {
        int idx = (counter.seed++) % USERS_COUNT;
        return s.level.addScore(users[idx], scores[idx]);
    }

    @State(Scope.Group)
    public static class RWLock {
        LevelRWLockImpl level = new LevelRWLockImpl();
    }

    @Benchmark
    @Group("rwlock")
    @GroupThreads(3)
    public Object getScoresRWLock(RWLock s) {
        return s.level.getScores();
    }

    @Benchmark
    @Group("rwlock")
    @GroupThreads(1)
    public boolean addScoreRWLock(RWLock s, Counter counter) {
        int idx = (counter.seed++) % USERS_COUNT;
        return s.level.addScore(users[idx], scores[idx]);
    }

    private static class LevelImpl extends Level {

        @Override
        public boolean addScore(User user, int score) {
            return doAddScore(user, score);
        }

        @Override
        public List<LevelScore> getScores() {
            return doGetScores();
        }
        
    }
}