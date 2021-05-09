package net.tislib.springrouter.base;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface RouteRequestHandler {
    void handle(HttpServletRequest request, HttpServletResponse response);
}

