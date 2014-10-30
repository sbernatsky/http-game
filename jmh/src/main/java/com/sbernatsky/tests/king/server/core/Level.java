package com.sbernatsky.tests.king.server.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Level {

    protected final int MAX_SCORES_COUNT = 15;
    protected final List<LevelScore> scores = new ArrayList<LevelScore>(MAX_SCORES_COUNT + 1);

    public abstract void addScore(User user, int score);

    public abstract List<LevelScore> getScores();

    protected void doAddScore(User user, int score) {
        // list is full and score is smaller then smallest in list
        if (scores.size() == MAX_SCORES_COUNT && scores.get(MAX_SCORES_COUNT - 1).getScore() >= score) {
            return;
        }
    
        for (int i = 0; i < scores.size(); i++) {
            LevelScore tmp = scores.get(i);
            // user score is present and is greater or equals to the passed
            if (tmp.getUserId() == user.getId() && tmp.getScore() >= score) {
                return;
            }
            if (tmp.getUserId() == user.getId()) {
                scores.remove(i);
                break;
            }
        }
        LevelScore levelScore = new LevelScore(user, score);
        scores.add(levelScore);
        Collections.sort(scores, Collections.reverseOrder()); // new array creation happens here
        if (scores.size() > MAX_SCORES_COUNT) {
            scores.remove(scores.size() - 1);
        }
    }

    protected List<LevelScore> doGetScores() {
        return new ArrayList<LevelScore>(scores);
    }

}