package com.example.filemarlin.websocket;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.example.filemarlin.dto.*;
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

    private void sendMessage(WebSocketSession session, Object response) throws IOException {
        JsonNode jsonPayload = objectMapper.valueToTree(response);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(jsonPayload)));
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
            var type = jsonNode.get("type").asString();

            switch (type) {

                case "webrtc-signal" -> {
                    var request = objectMapper.treeToValue(jsonNode, SignalWSRequest.class);

                    var username = session.getAttributes().get("username").toString();
                    if (connectedSessionsOnUser.get(username).contains(request.targetId())) {
                        var response = new SignalWSResponse(request.type(), session.getAttributes().get("sessionId").toString(), request.clientData());
                        sendMessage(connectedSessions.get(request.targetId()), response);
                        return;
                    }

                }
                case "get-clients" -> {
                    String username = (String) session.getAttributes().get("username");
                    String[] clientIds = connectedSessionsOnUser.get(username).toArray(new String[0]);

                    var request = objectMapper.treeToValue(jsonNode, BasicWSRequest.class);
                    var response = new GetClientsWSResponse(request.type(), clientIds, request.clientData());
                    sendMessage(session, response);

                }
                default -> {
                    var response = new ErrorWSResponse("error", "Invalid type");
                    sendMessage(session, response);
                }
            }

        } catch (Exception e) {
            var response = new ErrorWSResponse("error", "Invalid Request or Server Error");
            sendMessage(session, response);
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
