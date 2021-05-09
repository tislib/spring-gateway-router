package net.tislib.springrouter.service;

import kong.unirest.HttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import net.tislib.springrouter.base.handlers.ProxyRouterHandler;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProxyRouterHandlerService {

    private final ParameterResolver parameterResolver;
    private final ProxyBackendService proxyBackendService;

    @SneakyThrows
    public void handle(ProxyRouterHandler handler, HttpServletRequest request, HttpServletResponse response) {
        try {
            Map<String, Object> parameters = new HashMap<>();

            Map<?, ?> requestVars = (Map<?, ?>) request.getAttribute(
                    HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

            log.debug("extracted request vars: {}", requestVars);

            requestVars.keySet().forEach(item -> parameters.put("url." + item, requestVars.get(item)));

            try (ByteArrayOutputStream boas = new ByteArrayOutputStream()) {
                IOUtils.copy(request.getInputStream(), boas);

                byte[] body = boas.toByteArray();

                HttpResponse<String> result = proxyBackendService.handleDestination(handler.getDestination(), request, parameters, body);

                result.getHeaders().all().forEach(header -> {
                    response.setHeader(header.getName(), header.getValue());
                });

                response.setCharacterEncoding("UTF-8");
                response.setStatus(result.getStatus());
                response.getWriter().write(result.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(502);
            response.getWriter().write(e.getMessage());
        } finally {
            response.flushBuffer();
            response.getWriter().close();
        }
    }

}
