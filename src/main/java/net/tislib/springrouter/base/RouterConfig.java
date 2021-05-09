package net.tislib.springrouter.base;

import lombok.Data;
import net.tislib.springrouter.YamlPropertySourceFactory;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Data
public class RouterConfig {
    private List<Mapping> mappings;
}
