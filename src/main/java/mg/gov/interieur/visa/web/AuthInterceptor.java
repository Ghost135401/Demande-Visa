package mg.gov.interieur.visa.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

@Component
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        String path = uri;
        if (context != null && !context.isEmpty() && uri.startsWith(context)) {
            path = uri.substring(context.length());
        }
        log.debug("AuthInterceptor: uri='{}' context='{}' path='{}'", uri, context, path);
        // Exclude auth endpoints and static resources (compare against path without context)
        if (path.startsWith("/auth") || path.startsWith("/css") || path.startsWith("/js") || path.startsWith("/uploads") || path.startsWith("/favicon.ico") || path.startsWith("/backoffice/login")) {
            log.debug("AuthInterceptor: allowing public path {}", path);
            return true;
        }

        HttpSession s = request.getSession(false);
        boolean logged = s != null && s.getAttribute("currentUser") != null;
        if (logged) return true;

        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            try (PrintWriter w = response.getWriter()) {
                w.write("{\"success\":false,\"message\":\"Authentification requise\"}");
            }
            return false;
        }

        // Redirect to login page for browser requests
        response.sendRedirect(request.getContextPath() + "/backoffice/login");
        return false;
    }
}
