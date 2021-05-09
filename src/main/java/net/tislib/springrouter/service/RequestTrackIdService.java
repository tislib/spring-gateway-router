package net.tislib.springrouter.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class RequestTrackIdService {

    private final ThreadLocal<String> requestTrackIdThreadLocal = new ThreadLocal<>();

    public static final char[] REQUEST_TRACK_ID_SYMBOLS = {
            '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
    public static final String REQUEST_TRACK_ID = "requestTrackId";

    public void runWithRequestTrackId(Runnable runnable) {
        try {
            String requestTrackId = RandomStringUtils.random(16, REQUEST_TRACK_ID_SYMBOLS);
            requestTrackIdThreadLocal.set(requestTrackId);
            MDC.put(REQUEST_TRACK_ID, requestTrackId);

            runnable.run();
        } finally {
            requestTrackIdThreadLocal.remove();
            MDC.remove(REQUEST_TRACK_ID);
        }
    }
}
