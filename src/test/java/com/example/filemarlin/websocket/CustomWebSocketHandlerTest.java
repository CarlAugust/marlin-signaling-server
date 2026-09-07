package com.example.filemarlin.websocket;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/*
    Im just writing some stuff to myself since this was mostly set up with the help of AI

    Captor is magic to get the result in this case, i should probobly research more into it.
    Create mock functions to create mock objects since i cant simply create a WebSocketsessions for example
    If you are dealing with multible elements use a set, especially if the ordering might be random
*/

@ExtendWith(MockitoExtension.class)
public class CustomWebSocketHandlerTest {

    private CustomWebSocketHandler handler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        handler = new CustomWebSocketHandler();
    }

    private WebSocketSession createMockSession(String username) {
        WebSocketSession session = mock(WebSocketSession.class);
        Principal principal = mock(Principal.class);
        Map<String, Object> attributes = new HashMap<>();

        when(session.getAttributes()).thenReturn(attributes);
        when(session.getPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn(username);

        return session;
    }


    @Test
    @DisplayName("Should give list of only one connected client to name box cat")
    void handleTextMessageGetClients_Alone() throws Exception {
        WebSocketSession session = createMockSession("box cat");
        handler.afterConnectionEstablished(session);

        // In case other clients gets other names i suppose
        handler.afterConnectionEstablished(createMockSession("Evilcat"));
        handler.afterConnectionEstablished(createMockSession("Evilcat"));
        handler.afterConnectionEstablished(createMockSession("More evil cat"));

        String json = objectMapper.writeValueAsString(Map.of("type", "get-clients"));
        TextMessage requestMessage = new TextMessage(json);

        handler.handleTextMessage(session, requestMessage);

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, times(1)).sendMessage(captor.capture());

        String responseJson = captor.getValue().getPayload();

        JsonNode responsePayload = objectMapper.readTree(responseJson);
        assertEquals("get-clients", responsePayload.get("type").asString());

        JsonNode clientArray = responsePayload.get("clients");
        assertTrue(clientArray.isArray());
        assertEquals(1, clientArray.size());

        String expectedSessionId = (String) session.getAttributes().get("sessionId");
        assertEquals(expectedSessionId, clientArray.get(0).asString());
    }

    @Test
    @DisplayName("Should give list of all connected clients")
    void handleTextMessageGetClients_Multible() throws Exception {

        WebSocketSession session1 = createMockSession("box cat");
        WebSocketSession session2 = createMockSession("box cat");
        WebSocketSession session3 = createMockSession("box cat");
        handler.afterConnectionEstablished(session1);
        handler.afterConnectionEstablished(session2);
        handler.afterConnectionEstablished(session3);

        String json = objectMapper.writeValueAsString(Map.of("type", "get-clients"));
        TextMessage requestMessage = new TextMessage(json);

        handler.handleTextMessage(session1, requestMessage);

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session1, times(1)).sendMessage(captor.capture());

        String responseJson = captor.getValue().getPayload();

        JsonNode responsePayload = objectMapper.readTree(responseJson);
        assertEquals("get-clients", responsePayload.get("type").asString());

        JsonNode clientArray = responsePayload.get("clients");
        assertTrue(clientArray.isArray());
        assertEquals(3, clientArray.size());

        Set<String> expectedIds = Set.of(
            (String) session1.getAttributes().get("sessionId"),
            (String) session2.getAttributes().get("sessionId"),
            (String) session3.getAttributes().get("sessionId")
        );

        Set<String> actualIds = new HashSet<>();
        clientArray.forEach(node -> actualIds.add(node.asString()));

        assertEquals(expectedIds, actualIds);
    }

    @Test
    @DisplayName("Test sending signal to another client with same username")
    void handleTextMessageSignal() throws Exception {

        WebSocketSession session1 = createMockSession("box cat");
        WebSocketSession session2 = createMockSession("box cat");
        handler.afterConnectionEstablished(session1);
        handler.afterConnectionEstablished(session2);

        String json = objectMapper.writeValueAsString(Map.of(
            "type", "webrtc-signal",
            "data", Map.of(
                        "targetId", (String) session2.getAttributes().get("sessionId"),
                        "message", "meow"
                )
        ));
        TextMessage signalMessage = new TextMessage(json);

        handler.handleTextMessage(session1, signalMessage);

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session2, times(1)).sendMessage(captor.capture());

        String responseJson = captor.getValue().getPayload();
        JsonNode responsePayload = objectMapper.readTree(responseJson);
        assertEquals("webrtc-signal", responsePayload.get("type").asString());
        assertEquals("meow", responsePayload.get("data").get("message").asString());
        assertEquals( (String) session2.getAttributes().get("sessionId"), responsePayload.get("data").get("targetId").asString());
    }
}
