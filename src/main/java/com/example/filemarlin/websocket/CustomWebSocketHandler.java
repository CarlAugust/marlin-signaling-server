package com.example.filemarlin.websocket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import tools.jackson.core.JacksonException;
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

        String username = Objects.requireNonNull(session.getPrincipal()).getName();
        session.getAttributes().put("username", username);

        connectedSessionsOnUser
                .computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet())
                .add(sessionId);
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {

        try {
            /*

                The expected format for all requests is
                type and data
                type is the specific thing you want to do
                data is just some data that only clients care about that the server
                passes along
                So like it could contain a request ID such that the client can fullfill its own promises

            */
            JsonNode jsonNode = objectMapper.readTree(message.getPayload());
            String messageType = jsonNode.get("type").asString();
            JsonNode clientData = jsonNode.get("client-data");

            Map<String, Object> response = new HashMap<>();
            response.put("client-data", clientData);

            switch (messageType) {

                case "webrtc-signal" -> {
                    String targetId = jsonNode.get("targetId").asString();

                    var username = (String) session.getAttributes().get("username");
                    if (connectedSessionsOnUser.get(username).contains(targetId)) {
                        response.put("type", "webrtc-signal");
                        response.put("senderId", session.getAttributes().get("sessionId"));

                        // I could definitly improve here because im basicly writing same code twice but whatever
                        // Send to other client and return early
                        String jsonPayload = objectMapper.writeValueAsString(response);
                        connectedSessions.get(targetId).sendMessage(new TextMessage(jsonPayload));
                        return;
                    }

                }
                case "get-clients" -> {
                    String username = (String) session.getAttributes().get("username");
                    String[] sessions = connectedSessionsOnUser.get(username).toArray(new String[0]);

                    response.put("type", "get-clients");
                    response.put("clients", sessions);

                }
                default -> {
                    response.put("type", "error");
                    response.put("message", "invalid type");
                }
            }

            String jsonPayload = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(jsonPayload));

        } catch (Exception e) {
            String jsonPayload = objectMapper.writeValueAsString(Map.of(
                    "type", "error",
                    "message", "invalid message"
            ));
            session.sendMessage(new TextMessage(jsonPayload));
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
