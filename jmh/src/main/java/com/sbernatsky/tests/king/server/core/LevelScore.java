package com.sbernatsky.tests.king.server.core;

/** Keeps user score. Implements {@linkplain Comparable} to be sorted in level's score list. */
public class LevelScore implements Comparable<LevelScore> {
    private final User user;
    private final int score;

    public LevelScore(User user, int score) {
        this.user = user;
        this.score = score;
    }

    public int getUserId() {
        return user.getId();
    }

    public int getScore() {
        return score;
    }

    @Override
    public int compareTo(LevelScore o) {
        return Integer.compare(this.score, o.score);
    }

}
