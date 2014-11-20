package com.github.ramilexe.app;


import io.netty.channel.Channel;

public class Player {
    /**
     * Unique identification
     */
    private Integer id;

    /**
     * Player name
     */
    private String login;

    private Channel channel;

    public Player(String login, Channel channel) {
        UniqueGenerator generator = Application.getInstance().getGenerator(Player.class);
        id = generator.next();
        this.login = login;
        this.channel = channel;
    }

    public Integer getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }
}
