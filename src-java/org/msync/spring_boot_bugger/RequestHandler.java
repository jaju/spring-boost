package org.msync.spring_boot_bugger;

import clojure.lang.Keyword;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import static org.msync.spring_boot_bugger.Utils.*;

public class RequestHandler {

    private final String rootPath;
    private static final Logger logger = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(String rootPath) {
        this.rootPath = rootPath;
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

    /**
     * @param request - ServerRequest object as initialized by Spring
     * @return - The response
     */
    public Mono<ServerResponse> clojureHandler(ServerRequest request) {
        String uri = prunePath(request.path());
        Mono<MultiValueMap<String, String>> formData = request.formData();

        final var clojureRequest = (Map<Keyword, Object>) toRingSpecFn.invoke(uri, request);

        if (httpMethodsWithBody.contains(request.method())) {
            var headers = (Map<String, String>) clojureRequest.get(keyword("headers"));
            var contentType = headers.get("content-type");
            var inferredClass = contentTypeToJavaType(contentType);
            var parsed = request.bodyToMono(inferredClass);
            return parsed.flatMap(b -> {
                logger.info("We have a body: " + b);
                var updatedRequest = assocFn.invoke(clojureRequest, keyword("body"), b);
                Map<Keyword, Object> response = (Map<Keyword, Object>) rootHandlerFn.invoke(updatedRequest);
                return updateResponse(response);
            });
        }
        Map<Keyword, Object> response = (Map<Keyword, Object>) rootHandlerFn.invoke(clojureRequest);
        return updateResponse(response);
    }

    private Mono<ServerResponse> updateResponse(Map<Keyword, Object> clojureResponse) {

        logger.info(() -> "We have a response: " + clojureResponse);

        Long status = (Long) clojureResponse.get(keyword("status"));
        var headers = (Map<Object, String>) clojureResponse.get(keyword("headers"));
        Object body = clojureResponse.get(keyword("body"));

        logger.info(() -> "Response Status = " + status);
        logger.info(() -> "Response Headers = " + headers);
        logger.info(() -> "Response Body = " + body);
        return ServerResponse.status(status.intValue())
            .headers(h -> {
                for (var key : headers.keySet()) {
                    h.add(name(key), headers.get(key));
                }
            })
            .bodyValue(stringifyKeysFn.invoke(body));
    }
}
