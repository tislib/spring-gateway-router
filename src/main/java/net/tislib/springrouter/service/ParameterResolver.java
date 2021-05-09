package net.tislib.springrouter.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Log4j2
public class ParameterResolver {

    @Value("${application-service}")
    private String applicationService;

    @Value("${person-service}")
    private String personService;

    @Value("${common-service}")
    private String commonService;

    public String resolvePlaceHolders(String value, Map<String, Object> parameters) {
        parameters.put("application-service", applicationService);
        parameters.put("person-service", personService);
        parameters.put("common-service", commonService);

        log.debug("placeholder parameters: {}", parameters);

        String result = format(value, parameters);

        log.debug("placeholder resolver formatted from '{}' to '{}'", value, result);

        return result;
    }

    private static String format(String str, Map<String, Object> values) {

        StringBuilder builder = new StringBuilder(str);

        for (Map.Entry<String, Object> entry : values.entrySet()) {

            int start;
            String pattern = "{" + entry.getKey() + "}";
            String value = entry.getValue().toString();

            // Replace every occurence of %(key) with value
            while ((start = builder.indexOf(pattern)) != -1) {
                builder.replace(start, start + pattern.length(), value);
            }
        }

        return builder.toString();
    }
}
