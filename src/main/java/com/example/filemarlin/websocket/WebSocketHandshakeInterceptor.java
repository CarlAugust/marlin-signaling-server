package com.example.filemarlin.websocket;

import org.jspecify.annotations.Nullable;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;


/*
    This file might not be needed, so just consider it decrapted unless you actully need it CARL
*/
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        // TODO: Is there no need to authenticate this? Will the token always be valid if they reach this point

        String query = request.getURI().getQuery();
        System.out.println("Hello");
        // EVERYTHING UNDER HERE DOESNT WORK ATM
        if (query != null && query.contains("token=")) {

            // TODO: This just feels wrong to be honest
            String token = query.split("token=")[1].split("&")[0];
            System.out.println(token);
            attributes.put("token", token);
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, @Nullable Exception exception) {
        if (exception != null) {
            System.out.print("Expection error during WebSocket handshake:\n" + exception);
        }
    }
}
