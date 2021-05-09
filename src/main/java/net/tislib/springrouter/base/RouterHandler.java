package net.tislib.springrouter.base;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import net.tislib.springrouter.base.handlers.MergeCombineRouterHandler;
import net.tislib.springrouter.base.handlers.MergeFlatMapRouterHandler;
import net.tislib.springrouter.base.handlers.ProxyRouterHandler;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "name")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ProxyRouterHandler.class, name = "proxy-handler"),
        @JsonSubTypes.Type(value = MergeFlatMapRouterHandler.class, name = "merge-flat-map-handler"),
        @JsonSubTypes.Type(value = MergeCombineRouterHandler.class, name = "merge-combine-handler"),
})
public interface RouterHandler {
}
