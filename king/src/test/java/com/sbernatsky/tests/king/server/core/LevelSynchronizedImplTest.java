package com.sbernatsky.tests.king.server.core;


import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class LevelSynchronizedImplTest {
    private LevelSynchronizedImpl tested;

    @Before
    public void setUp() {
        tested = new LevelSynchronizedImpl();
    }

    @Test
    public void testAddScore() {
        assertEquals(0, tested.getScores().size());

        User u1 = new User(123);
        tested.addScore(u1, 1);
        assertEquals(1, tested.getScores().size());
        assertEquals(u1.getId(), tested.getScores().get(0).getUserId());
        assertEquals(1, tested.getScores().get(0).getScore());

        tested.addScore(u1, 2);
        assertEquals(1, tested.getScores().size());
        assertEquals(u1.getId(), tested.getScores().get(0).getUserId());
        assertEquals(2, tested.getScores().get(0).getScore());

        for (int i = 32; i < 128; i++) {
            tested.addScore(new User(i), i);
        }
        assertEquals(15, tested.getScores().size());
        for (int i = 0; i < 15; i++) {
            assertEquals(127 - i, tested.getScores().get(i).getUserId());
            assertEquals(127 - i, tested.getScores().get(i).getScore());
        }

        tested.addScore(u1, 255);
        assertEquals(15, tested.getScores().size());
        assertEquals(u1.getId(), tested.getScores().get(0).getUserId());
        assertEquals(255, tested.getScores().get(0).getScore());
    }

}
