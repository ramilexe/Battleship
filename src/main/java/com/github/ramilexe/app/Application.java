package com.github.ramilexe.app;

import java.util.HashMap;

/**
 * Application container
 */
public class Application {
    private final static byte version = 1;

    private static Application instance;

    private HashMap<Class, UniqueGenerator> generators;

    private HashMap<Integer, Player> players;

    private Application() {
        generators = new HashMap<Class, UniqueGenerator>();

        generators.put(Player.class, new UniqueGenerator());
        players = new HashMap<Integer, Player>();
    }

    public UniqueGenerator getGenerator(Class c) {
        return generators.get(c);
    }

    public void addPlayer(Player player) {
        players.put(player.getId(), player);
    }

    public static Application getInstance() {
        if (instance == null) {
            instance = new Application();
        }

        return instance;
    }

    public static byte version() {
        return version;
    }
}
