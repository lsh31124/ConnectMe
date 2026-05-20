package hello.connectme.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.List;

public class NoHandlerFoundExceptionFilter extends OncePerRequestFilter {

    private final List<HandlerMapping> handlerMappings;

    public NoHandlerFoundExceptionFilter(List<HandlerMapping> handlerMappings) {
        this.handlerMappings = handlerMappings;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (isNoHandlerFound(request)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"NOT_FOUND\",\"message\":\"요청한 리소스를 찾을 수 없습니다.\",\"data\":null}");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isNoHandlerFound(HttpServletRequest request) {
        for (HandlerMapping handlerMapping : handlerMappings) {
            try {
                HandlerExecutionChain handler = handlerMapping.getHandler(request);
                if (handler != null) {
                    return false;
                }
            } catch (org.springframework.web.HttpRequestMethodNotSupportedException e) {
                return false;
            } catch (Exception ignored) {
            }
        }
        return true;
    }
}