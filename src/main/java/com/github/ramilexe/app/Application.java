package com.github.ramilexe.app;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Application container
 */
public class Application {
    private final static byte version = 1;

    private static Application instance;

    private HashMap<Class, UniqueGenerator> generators;

    private HashMap<Channel, Player> players;

    private Application() {
        generators = new HashMap<Class, UniqueGenerator>();

        generators.put(Player.class, new UniqueGenerator());
        players = new HashMap<Channel, Player>();
    }

    public UniqueGenerator getGenerator(Class c) {
        return generators.get(c);
    }

    public void addPlayer(Channel channel, Player player) {
        players.put(channel, player);
    }

    public void removePlayer(Channel channel) {
        if (channel != null) {
            players.remove(channel);
        }
    }

    public List<Player> getPlayers()
    {
        return new ArrayList<Player>(players.values());
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
