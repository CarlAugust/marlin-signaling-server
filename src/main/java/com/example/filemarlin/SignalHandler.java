package com.example.filemarlin;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SignalHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> userSession = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UUID uuid = UUID.randomUUID();
        String id = uuid.toString();

        userSession.put(id, session);
        session.getAttributes().put("id", id);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String id = (String) session.getAttributes().get("key");
        userSession.remove(id);
    }
}
