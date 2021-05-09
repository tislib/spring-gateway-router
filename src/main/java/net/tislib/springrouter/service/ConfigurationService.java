package net.tislib.springrouter.service;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import net.tislib.springrouter.base.Mapping;
import net.tislib.springrouter.base.RouteRequestHandler;
import net.tislib.springrouter.base.RouterConfig;
import net.tislib.springrouter.base.handlers.MergeCombineRouterHandler;
import net.tislib.springrouter.base.handlers.MergeFlatMapRouterHandler;
import net.tislib.springrouter.base.handlers.ProxyRouterHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
@Log4j2
public class ConfigurationService {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final ProxyRouterHandlerService proxyRouterHandlerService;
    private final MergeFlatMapRouterHandlerService mergeFlatMapRouterHandlerService;
    private final MergeCombineRouterHandlerService mergeCombineRouterHandlerService;
    private final RequestTrackIdService requestTrackIdService;

    public ConfigurationService(RouterConfig routerConfig,
                                RequestMappingHandlerMapping requestMappingHandlerMapping,
                                ProxyRouterHandlerService proxyRouterHandlerService,
                                MergeFlatMapRouterHandlerService mergeFlatMapRouterHandlerService,
                                MergeCombineRouterHandlerService mergeCombineRouterHandlerService,
                                RequestTrackIdService requestTrackIdService) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.proxyRouterHandlerService = proxyRouterHandlerService;
        this.mergeFlatMapRouterHandlerService = mergeFlatMapRouterHandlerService;
        this.mergeCombineRouterHandlerService = mergeCombineRouterHandlerService;
        this.requestTrackIdService = requestTrackIdService;

        routerConfig.getMappings().forEach(this::prepareRouting);
    }

    @SneakyThrows
    private void prepareRouting(final Mapping item) {
        log.info("preparing router for item: {}", item);
        RouteRequestHandler requestHandler = (request, response) -> handleInternal(item, request, response);

        RequestMappingInfo routeParams = RequestMappingInfo.paths(item.getRoute()).methods(item.getMethod()).build();

        requestMappingHandlerMapping.registerMapping(routeParams, requestHandler, RouteRequestHandler.class.getDeclaredMethod("handle", HttpServletRequest.class, HttpServletResponse.class));
    }

    private void handleInternal(Mapping mapping, HttpServletRequest request, HttpServletResponse response) {
        requestTrackIdService.runWithRequestTrackId(() -> {
            log.trace("handling request for mapping: {}", mapping.footPrint(request));

            if (mapping.getHandler() instanceof ProxyRouterHandler) {
                proxyRouterHandlerService.handle((ProxyRouterHandler) mapping.getHandler(), request, response);
                return;
            } else if (mapping.getHandler() instanceof MergeFlatMapRouterHandler) {
                mergeFlatMapRouterHandlerService.handle((MergeFlatMapRouterHandler) mapping.getHandler(), request, response);
                return;
            } else if (mapping.getHandler() instanceof MergeCombineRouterHandler) {
                mergeCombineRouterHandlerService.handle((MergeCombineRouterHandler) mapping.getHandler(), request, response);
                return;
            }

            throw new UnsupportedOperationException("handler is not supported");
        });
    }

}
