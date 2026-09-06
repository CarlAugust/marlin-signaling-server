package com.example.filemarlin.websocket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class CustomWebSocketHandler extends TextWebSocketHandler {

    /*
    Note to self
    Is clientsession nessecary? Rather perhaps it should be replaced by
    something that stores all clients with the same username that is connected.
    Such that its possible to see all of them or something.
     */
    private final Map<String, WebSocketSession> connectedSessions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> connectedSessionsOnUser = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {

        UUID uuid = UUID.randomUUID();
        String sessionId = uuid.toString();

        connectedSessions.put(sessionId, session);
        session.getAttributes().put("sessionId", sessionId);

        String username = (String) Objects.requireNonNull(session.getPrincipal()).getName();
        session.getAttributes().put("username", username);

        connectedSessionsOnUser
                .computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet())
                .add(sessionId);
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {

        try {
            /*
                Grab target and data
                Then validate data as expected type for target
            */
            JsonNode jsonNode = objectMapper.readTree(message.getPayload());
            String messageType = jsonNode.get("type").asString();
            Map<String, Object> response = Map.of();

            switch (messageType) {

                case "webrtc-signal":

                    break;
                case "get-clients":

                    String username = (String) session.getAttributes().get("username");
                    String[] sessions = (String[]) connectedSessionsOnUser.get(username).toArray();

                    response = Map.of(
                            "type", "get-clients",
                            "clients", sessions
                    );
                    
                    break;
                default:
                    response = Map.of(
                            "type", "error",
                            "message", "invalid type"
                    );
            }

            String jsonPayload = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(jsonPayload));


        } catch (NullPointerException e) {
            // Error to client
        }


    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = (String) session.getAttributes().get("sessionId");
        String username = (String) session.getAttributes().get("username");


        connectedSessions.remove(sessionId);

        connectedSessionsOnUser.computeIfPresent(username, (k, sessions) -> {
            sessions.remove(sessionId);
            return sessions.isEmpty() ? null : sessions;
        });
    }
}
