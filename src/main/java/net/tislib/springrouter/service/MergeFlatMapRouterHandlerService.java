package net.tislib.springrouter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import net.tislib.springrouter.base.handlers.MergeFlatMapRouterHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class MergeFlatMapRouterHandlerService {

    private final ObjectMapper objectMapper;
    private final ProxyBackendService proxyBackendService;

    @SneakyThrows
    public void handle(MergeFlatMapRouterHandler handler, HttpServletRequest request, HttpServletResponse response) {
        try {
            Map<String, Object> parameters = new HashMap<>();

            Map<?, ?> requestVars = (Map<?, ?>) request.getAttribute(
                    HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

            requestVars.keySet().forEach(item -> parameters.put("url." + item, requestVars.get(item)));

            log.debug("extracted request vars: {}", requestVars);

            HttpResponse<String> mainResult = proxyBackendService.handleDestination(handler.getMainDestination().getDestination(), request, parameters);

            response.setCharacterEncoding("UTF-8");
            mainResult.getHeaders().all().forEach(header -> {
                response.setHeader(header.getName(), header.getValue());
            });

            if (!mainResult.isSuccess()) {
                response.setStatus(mainResult.getStatus());
                response.getWriter().write(mainResult.getBody());
                return;
            }

            // parsing main result
            Object mainResultData = objectMapper.readValue(mainResult.getBody(), Object.class);

            Object result = iterateOver(mainResultData, handler.getRecordsPath(), item -> {
                Map<String, Object> data = (Map<String, Object>) item;
                try {
                    // resolve mappings
                    Map<String, Object> flatRequestParameters = prepareParameters(parameters, data, handler);

                    HttpResponse<String> mapResult = proxyBackendService.handleDestination(handler.getMapDestination().getDestination(), request, flatRequestParameters);
                    if (!mapResult.isSuccess()) {
                        data.put("ERROR", mapResult.getBody());
                        data.put("ERROR_STATUS", mapResult.getStatus());
                    }
                    Object parsedMapData = objectMapper.readValue(mapResult.getBody(), Object.class);

                    data.put(handler.getMapDestination().getWrapper(), parsedMapData);
                } catch (Exception e) {
                    e.printStackTrace();
                    data.put("EXCEPTION_CLASS", e.getClass().getSimpleName());
                    data.put("EXCEPTION", e.getMessage());
                }

                return data;
            });

            response.setStatus(mainResult.getStatus());
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

    Map<String, Object> prepareParameters(Map<String, Object> parameters, Map<?, ?> data, MergeFlatMapRouterHandler handler) {
        Map<String, Object> newParameters = new HashMap<>(parameters);

        handler.getMappings().forEach(mapping -> {
            newParameters.put(mapping.getKey(), data.get(mapping.getPath()));
        });

        return newParameters;
    }

    private Object iterateOver(Object mainResultData, String recordsPath, Function<Object, Object> mapper) {
        if (StringUtils.isBlank(recordsPath)) {
            return iterateOverList((List<?>) mainResultData, mapper);
        } else {
            Map<String, Object> data = new HashMap<>((Map<String, Object>) mainResultData);
            Object array = data.get(recordsPath);
            Object result = iterateOverList((List<?>) array, mapper);
            data.put(recordsPath, result);
            return data;
        }
    }

    private Object iterateOverList(List<?> mainResultData, Function<Object, Object> mapper) {
        return mainResultData.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

}
