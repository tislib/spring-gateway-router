package net.tislib.springrouter.base;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Data
public class Mapping {
    private String route;
    private RequestMethod method;
    private List<String> grants;
    private RouterHandler handler;

    public String footPrint(HttpServletRequest request) {
        return method + " " + route + " => " + request.getRequestURI();
    }
}
