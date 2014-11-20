package com.github.ramilexe.protocol;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ramilexe.app.Application;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class Message {
    /**
     * Protocol version
     */
    public byte version;

    /**
     * Type of message
     */
    public byte type;

    /**
     * Message payload. Depends on type
     */
    public Object payload;

    public static WebSocketFrame messageToFrame(byte type, Object payload) throws JsonProcessingException {
        Message message = new Message();
        message.version = Application.version();
        message.type = type;
        message.payload = payload;

        return new TextWebSocketFrame(new ObjectMapper().writeValueAsString(message));
    }
}
