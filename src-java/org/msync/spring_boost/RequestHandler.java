package org.msync.spring_boost;

import clojure.lang.Keyword;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.msync.spring_boost.Utils.*;

public class RequestHandler {

    private final String rootPath;
    private final Boost boost;
    private static final Logger logger = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(String rootPath, Boost boost) {
        this.rootPath = rootPath;
        this.boost = boost;
    }

    private String prunePath(String path) {
        return path.substring(rootPath.length());
    }

    static private Map<MediaType, Class<?>> mediaTypeToClass = Map.of(
        MediaType.APPLICATION_JSON, Map.class,
        MediaType.APPLICATION_FORM_URLENCODED, MultiValueMap.class
    );

    private static Class<?> contentTypeToJavaType(String contentType) {
        MediaType mt = MediaType.valueOf(contentType);
        Class<?> known = mediaTypeToClass.get(mt);

        if (Objects.isNull(known)) {
            known = String.class;
        }

        return known;
    }

    private static final Set<HttpMethod> httpMethodsWithBody = Set.of(
        HttpMethod.POST,
        HttpMethod.PUT,
        HttpMethod.PATCH
    );

    private Mono<ServerResponse> updateResponse(Map<Keyword, Object> clojureResponse) {

        Long status = (Long) clojureResponse.get(keyword("status"));
        var headers = (Map<Object, String>) clojureResponse.get(keyword("headers"));
        Object body = clojureResponse.get(keyword("body"));

        logger.log(Level.FINE, () -> "Response Status = " + status);
        logger.log(Level.FINE, () -> "Response Headers = " + headers);
        logger.log(Level.FINE, () -> "Response Body = " + body);
        return ServerResponse.status(status.intValue())
            .headers(h -> {
                for (var key : headers.keySet()) {
                    h.add(name(key), headers.get(key));
                }
            })
            .bodyValue(stringifyKeysFn.invoke(body));
    }

    /**
     * Endpoint to request starting of the nrepl-server
     *
     * @param request - The request object
     * @return void
     */
    public Mono<ServerResponse> startNreplHandler(ServerRequest request) {
        try {
            boost.startNrepl();
            return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", "started"));
        } catch (Exception e) {
            return ServerResponse
                .status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", "error"));
        }
    }

    /**
     * Endpoint to request stopping of the nrepl-server
     *
     * @param request - The request object
     * @return void
     */
    public Mono<ServerResponse> stopNreplHandler(ServerRequest request) {
        try {
            boost.stopNrepl();
            return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", "stopped"));
        } catch (Exception e) {
            return ServerResponse
                .status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", "error"));
        }
    }


    /**
     * @param request - ServerRequest object as initialized by Spring
     * @return - The response
     */
    public Mono<ServerResponse> httpRequestHandler(ServerRequest request) {
        String uri = prunePath(request.path());
        Mono<MultiValueMap<String, String>> formData = request.formData();

        final var clojureRequest = (Map<Keyword, Object>) toRingSpecFn.invoke(uri, request);

        if (httpMethodsWithBody.contains(request.method())) {
            var headers = (Map<String, String>) clojureRequest.get(keyword("headers"));
            var contentType = headers.get("content-type");
            var inferredClass = contentTypeToJavaType(contentType);
            var parsed = request.bodyToMono(inferredClass);
            return parsed.flatMap(b -> {
                logger.log(Level.FINER, () -> "We have a body: " + b);
                var updatedRequest = assocFn.invoke(clojureRequest, keyword("body"), b);
                Map<Keyword, Object> response = (Map<Keyword, Object>) httpHandlerFn.invoke(updatedRequest);
                return updateResponse(response);
            });
        }
        Map<Keyword, Object> response = (Map<Keyword, Object>) httpHandlerFn.invoke(clojureRequest);
        return updateResponse(response);
    }

    // Websockets

    private WebSocketMessage webSocketMessageHandler(WebSocketMessage message) {

        DataBuffer dataBuffer = message.getPayload();
        DataBufferFactory dataBufferFactory = dataBuffer.factory();

        System.out.println("Received message of type: " + message.getType());

        String payload = dataBuffer.toString(StandardCharsets.UTF_8);
        payload = "Hello, " + payload;
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        var processed = dataBufferFactory.wrap(bytes);
        return new WebSocketMessage(WebSocketMessage.Type.BINARY, processed);
    }

    public WebSocketHandler webSocketSessionHandler() {
        return (WebSocketSession wsSession) -> {
            Flux<WebSocketMessage> stringFlux =
                (Flux<WebSocketMessage>) websocketHandlerFn.invoke(wsSession);
            return wsSession.send(stringFlux);
        };
    }

}
