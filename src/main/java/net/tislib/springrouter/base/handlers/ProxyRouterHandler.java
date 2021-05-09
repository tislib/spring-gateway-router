package net.tislib.springrouter.base.handlers;

import lombok.Data;
import net.tislib.springrouter.base.RouterHandler;

import java.util.List;

@Data
public class ProxyRouterHandler implements RouterHandler {
    private String destination;
    private List<String> params;
}
