package com.miolean.arena.framework;

public interface Perpetual {
    void tick(Object... args);
    void pause();
    void start();
    void resume();
    void stop();
}
