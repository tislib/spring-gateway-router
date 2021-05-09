package net.tislib.springrouter.service;

import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@Log4j2
@RequiredArgsConstructor
public class ProxyBackendService {

    private final ParameterResolver parameterResolver;

    public HttpResponse<String> handleDestination(String destination,
                                                  HttpServletRequest request,
                                                  Map<String, Object> parameters) {
        return handleDestination(destination, request, parameters, new byte[]{});
    }

    public HttpResponse<String> handleDestination(String destination,
                                                  HttpServletRequest request,
                                                  Map<String, Object> parameters,
                                                  byte[] body) {
        String url = parameterResolver.resolvePlaceHolders(destination, parameters);

        log.debug("begin destination => method: {}; destination/url placeholder: {}; actual url: {}", request.getMethod(), destination, url);

        HttpRequestWithBody forwardRequestBuilder = Unirest.request(request.getMethod(), url);

        Set<String> ignoredHeaders = new HashSet<>();
        ignoredHeaders.add("host");
        ignoredHeaders.add("authorization");
        ignoredHeaders.add("content-length");

        request.getHeaderNames().asIterator().forEachRemaining(item -> {
            if (ignoredHeaders.contains(item.toLowerCase(Locale.ROOT))) {
                return;
            }
            forwardRequestBuilder.header(item, request.getHeader(item));
        });

        log.debug("request headers: " + forwardRequestBuilder.getHeaders());

        HttpResponse<String> result;

        if (body.length > 0) {
            result = forwardRequestBuilder.body(body).asString();
            log.trace("request body: {}", new String(body));
        } else {
            result = forwardRequestBuilder
                    .asString();
        }
        log.debug("response headers: {}", result.getHeaders());
        log.trace("response body: {}", result.getBody());
        log.debug("end destination");

        return result;
    }
}
