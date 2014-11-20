package com.github.ramilexe;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ramilexe.app.Application;
import com.github.ramilexe.app.Player;
import com.github.ramilexe.protocol.LoginCredential;
import com.github.ramilexe.protocol.Message;
import com.github.ramilexe.protocol.MessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import java.io.IOException;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;
import static io.netty.handler.codec.http.HttpMethod.*;

public class WebSocketHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketServerHandshaker handshaker;

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //ctx.channel().id()
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame)frame.retain());
            return;
        }

        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
                    .getName()));
        }

        String request = ((TextWebSocketFrame)frame).text();

        ObjectMapper mapper = new ObjectMapper();
        Message m;

        try {
            m = mapper.readValue(request, Message.class);
            //Authorizing
            if (m.type == MessageType.LOGIN) {
                //extract login
                LoginCredential l = mapper.convertValue(m.payload, LoginCredential.class);
                //create new player
                Player player = new Player(l.login, ctx.channel());
                //add to list
                Application.getInstance().addPlayer(player);
                //answer
                ctx.channel().writeAndFlush(Message.messageToFrame(MessageType.LOGIN_SUCCESS, player));
            }

        } catch (IOException e) {
            e.printStackTrace();
            //close channel
            ctx.channel().close();
        }

        //ctx.channel().write(new TextWebSocketFrame("Response from server: " + request));
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (!request.getDecoderResult().isSuccess()) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        if (request.getMethod() != GET) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        //allow only / uri
        if (!request.getUri().equals("/")) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
            return;
        }

        WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(getWebSocketLocation(request), null, false);
        handshaker = factory.newHandshaker(request);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
            return;
        }
        handshaker.handshake(ctx.channel(), request);
    }

    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        int code = res.getStatus().code();
        if (code != 200) {
            String errorMessage = "<h1>" + res.getStatus().reasonPhrase() + " "  + Integer.toString(code) + "</h1>";
            ByteBuf buf = Unpooled.copiedBuffer(errorMessage, CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpHeaders.setContentLength(res, res.content().readableBytes());
        }

        ChannelFuture f = ctx.channel().writeAndFlush(res);

        if (!HttpHeaders.isKeepAlive(request) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("Exception: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        return "ws://" + req.headers().get(HOST);
    }
}
