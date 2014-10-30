package com.sbernatsky.tests.king.jmh;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

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
@State(Scope.Group)
public class JMHLevelSync {
    private static final int USERS_COUNT = 1024;
    private static final int MAX_SCORE = 1024;

    private User[] users;

    @Setup
    public void setUp() {
        users = new User[USERS_COUNT];
        Random random = new Random();
        for (int i = 0; i < USERS_COUNT; i++) {
            users[i] = new User(random.nextInt(USERS_COUNT/2));
        }
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
    public int addScoreSync(Sync s) {
        int userId = ThreadLocalRandom.current().nextInt(USERS_COUNT);
        int score = ThreadLocalRandom.current().nextInt(MAX_SCORE);
        s.level.addScore(users[userId], score);
        return score;
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
    public int addScoreRWLock(RWLock s) {
        int userId = ThreadLocalRandom.current().nextInt(USERS_COUNT);
        int score = ThreadLocalRandom.current().nextInt(MAX_SCORE);
        s.level.addScore(users[userId], score);
        return score;
    }

}