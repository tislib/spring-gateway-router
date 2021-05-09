package net.tislib.springrouter.base.handlers;

import lombok.Data;
import net.tislib.springrouter.base.RouterHandler;

import java.util.List;

@Data
public class MergeFlatMapRouterHandler implements RouterHandler {
    private String recordsPath;
    private List<MergeRouterMapping> mappings;
    private Destination mainDestination;
    private Destination mapDestination;

    @Data
    public static class MergeRouterMapping {
        private String key;
        private String path;
    }
}
