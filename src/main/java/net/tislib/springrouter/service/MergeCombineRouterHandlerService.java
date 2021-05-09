package net.tislib.springrouter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import net.tislib.springrouter.base.handlers.MergeCombineRouterHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class MergeCombineRouterHandlerService {

    private final ObjectMapper objectMapper;
    private final ProxyBackendService proxyBackendService;

    @SneakyThrows
    public void handle(MergeCombineRouterHandler handler, HttpServletRequest request, HttpServletResponse response) {
        try {
            final Map<String, Object> result = new HashMap<>();

            Map<String, Object> parameters = new HashMap<>();
            Map<?, ?> requestVars = (Map<?, ?>) request.getAttribute(
                    HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

            requestVars.keySet().forEach(item -> parameters.put("url." + item, requestVars.get(item)));

            log.debug("extracted request vars: {}", requestVars);

            handler.getDestinations().forEach(item -> {
                HttpResponse<String> itemResult = proxyBackendService.handleDestination(item.getDestination(), request, parameters);

                if (itemResult.isSuccess()) {
                    try {
                        Object itemData = objectMapper.readValue(itemResult.getBody(), Object.class);
                        result.put(item.getWrapper(), itemData);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    response.setStatus(itemResult.getStatus());
                    result.put(item.getWrapper(), itemResult.getBody());
                }
            });

            if (response.getStatus() == 0) {
                response.setStatus(200);
            }

            response.getWriter().write(objectMapper.writeValueAsString(result));
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
