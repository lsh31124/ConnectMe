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

/**
 * 등록되지 않은 URL 요청을 처리하는 서블릿 필터
 * Spring Security 필터 체인에서 404 응답을 JSON 형태로 직접 반환하여
 * GlobalExceptionHandler와 동일한 에러 포맷 유지
 */
public class NoHandlerFoundExceptionFilter extends OncePerRequestFilter {

    private final List<HandlerMapping> handlerMappings;

    /**
     * @param handlerMappings 핸들러 매핑 목록 (요청 URL 처리 가능 여부 확인용)
     */
    public NoHandlerFoundExceptionFilter(List<HandlerMapping> handlerMappings) {
        this.handlerMappings = handlerMappings;
    }

    /**
     * 핸들러가 없는 요청에 대해 404 JSON 응답을 즉시 반환
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (isNoHandlerFound(request)) {
            // 등록된 핸들러가 없으면 404 JSON 응답 반환
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"NOT_FOUND\",\"message\":\"요청한 리소스를 찾을 수 없습니다.\",\"data\":null}");
            return;
        }
        chain.doFilter(request, response);
    }

    /**
     * 등록된 핸들러 매핑 중 요청을 처리할 수 있는 핸들러가 없는지 확인
     * @param request HTTP 요청
     * @return 처리 가능한 핸들러가 없으면 true
     */
    private boolean isNoHandlerFound(HttpServletRequest request) {
        for (HandlerMapping handlerMapping : handlerMappings) {
            try {
                HandlerExecutionChain handler = handlerMapping.getHandler(request);
                if (handler != null) {
                    return false;
                }
            } catch (org.springframework.web.HttpRequestMethodNotSupportedException e) {
                // HTTP 메서드 불일치 — URL 자체는 등록된 상태이므로 핸들러 없음 아님
                return false;
            } catch (Exception ignored) {
            }
        }
        return true;
    }
}