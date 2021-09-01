package org.msync.spring_boot_bugger;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
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

public class RequestHandler {

    private final String rootPath;

    private static final IFn assoc;
    private static final IFn nameFn;
    private static final IFn toRingSpec;
    private static final IFn rootHandler;
    private static final IFn stringifyKeys;

    private static final Logger logger = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(String rootPath) {
        this.rootPath = rootPath;
    }

    static {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("org.msync.spring-boot-bugger"));
        require.invoke(Clojure.read("org.msync.spring-boot-bugger.ring"));
        require.invoke(Clojure.read("clojure.walk"));

        assoc = Clojure.var("clojure.core", "assoc");
        nameFn = Clojure.var("clojure.core", "name");
        toRingSpec = Clojure.var("org.msync.spring-boot-bugger.ring", "to-ring-spec");
        rootHandler = Clojure.var("org.msync.spring-boot-bugger", "-root-handler");
        stringifyKeys = Clojure.var("clojure.walk", "stringify-keys");
    }

    private String prunePath(String path) {
        return path.substring(rootPath.length());
    }

    static private Map<MediaType, Class> mediaTypeToClass = Map.of(
        MediaType.APPLICATION_JSON, Map.class,
        MediaType.APPLICATION_FORM_URLENCODED, MultiValueMap.class
    );

    private static Class contentTypeToJavaType(String contentType) {
        MediaType mt = MediaType.valueOf(contentType);
        Class known = mediaTypeToClass.get(mt);

        if (Objects.isNull(known)) {
            known = String.class;
        }

        return known;
    }

    private static final Set<HttpMethod> httpMethodsWithBody = Set.of(
      HttpMethod.POST,
      HttpMethod.PUT,
      HttpMethod.PATCH,
      HttpMethod.DELETE
    );

    /**
     * @param request - Self-evident. Contains all the required information for handling
     *                a request
     * @return - The response
     */
    public Mono<ServerResponse> clojureHandler(ServerRequest request) {
        String uri = prunePath(request.path());
        request.formData();

        final var clojureRequest = (Map<Keyword, Object>) toRingSpec.invoke(uri, request);

        if (httpMethodsWithBody.contains(request.method())) {
            var headers = (Map<String, String>) clojureRequest.get(Keyword.intern("headers"));
            var contentType = headers.get("content-type");
            var inferredClass = contentTypeToJavaType(contentType);
            var parsed = request.bodyToMono(inferredClass);
            return parsed.flatMap(b -> {
                var updatedRequest = (Map<Keyword, Object>) assoc.invoke(clojureRequest, Keyword.intern("body"), b);
                Map<Keyword, Object> response = (Map<Keyword, Object>) rootHandler.invoke(updatedRequest);
                return updateResponse(response);
            });
        }
        Map<Keyword, Object> response = (Map<Keyword, Object>) rootHandler.invoke(clojureRequest);
        return updateResponse(response);
    }

    private Mono<ServerResponse> updateResponse(Map<Keyword, Object> clojureResponse) {
        Long status = (Long) clojureResponse.get(Keyword.intern("status"));
        var headers = (Map<String, String>) clojureResponse.get(Keyword.intern("headers"));
        Object body = clojureResponse.get(Keyword.intern("body"));

        logger.info(() -> "Response Status = " + status);
        logger.info(() -> "Response Headers = " + headers);
        logger.info(() -> "Response Body = " + body);
        return ServerResponse.status(status.intValue())
            .headers(h -> {
                for (var key : headers.keySet()) {
                    h.add((String) nameFn.invoke(key), headers.get(key));
                }
            })
            .bodyValue(stringifyKeys.invoke(body));
    }
}
