package com.example.filemarlin.websocket;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class SignalHandler extends TextWebSocketHandler {

    /*
    Note to self
    Is clientsession nessecary? Rather perhaps it should be replaced by
    something that stores all clients with the same username that is connected.
    Such that its possible to see all of them or something.
     */
    private final Map<String, WebSocketSession> clientSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ArrayList<String> getConnectedClientIds() {
        return new ArrayList<>(clientSessions.keySet());
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {

        UUID uuid = UUID.randomUUID();
        String sessionId = uuid.toString();

        clientSessions.put(sessionId, session);
        session.getAttributes().put("sessionId", sessionId);

        String username = (String) Objects.requireNonNull(session.getPrincipal()).getName();
        session.getAttributes().put("username", username);
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {

        JsonNode jsonNode = objectMapper.readTree(message.getPayload());

        // TODO: Error back to client
        try {
            /*
                Grab target and data
                Then validate data as expected type for target
            */
            String targetId = jsonNode.get("targetId").asString();
            JsonNode data = jsonNode.get("data");


        } catch (NullPointerException e) {
            // Error to client
        }


    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = (String) session.getAttributes().get("sessionId");
        String username = (String) session.getAttributes().get("username");


        clientSessions.remove(sessionId);
    }
}
