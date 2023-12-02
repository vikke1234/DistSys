package org.uoh.distributed.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class Food {
    @Getter
    private final int x;
    @Getter
    private final int y;


    static Food generateFood(int maxX, int maxY) {
        int x = ThreadLocalRandom.current().nextInt(0, maxX);
        int y = ThreadLocalRandom.current().nextInt(0, maxY);
        return new Food(x, y);
    }
}
