package com.github.ramilexe.app;

import java.util.concurrent.atomic.AtomicInteger;

public class UniqueGenerator {
    private final AtomicInteger counter = new AtomicInteger(1);

    public int next() {
        return counter.getAndIncrement();
    }
}
