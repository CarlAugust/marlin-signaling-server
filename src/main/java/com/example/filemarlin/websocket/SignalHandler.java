package com.example.filemarlin.websocket;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class SignalHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> clientSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ArrayList<String> getConnectedClientIds() {
        return new ArrayList<>(clientSessions.keySet());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        UUID uuid = UUID.randomUUID();
        String id = uuid.toString();

        clientSessions.put(id, session);
        session.getAttributes().put("id", id);

        System.out.println("Client connected");

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(message.getPayload());

        // TODO: Error back to client
        try {
            String targetId = jsonNode.get("targetId").asText();
            WebSocketSession targetSession = clientSessions.get(targetId);
            if (targetSession != null && targetSession.isOpen()) {
                targetSession.sendMessage(message);
            }
        } catch (NullPointerException e) {
            System.err.println("No target ID provided");
        }


    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String id = (String) session.getAttributes().get("id");
        clientSessions.remove(id);
    }
}
