package demo.travel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final String origin;
    private final Map<String, Window> hits = new ConcurrentHashMap<>();
    private static final class Window { final long startedAt; final AtomicInteger count = new AtomicInteger(); Window(long startedAt) { this.startedAt = startedAt; } }
    public WebConfig(@Value("${travel.frontend-origin:http://localhost:4200}") String origin) { this.origin = origin; }
    @Override public void addCorsMappings(CorsRegistry registry) { registry.addMapping("/api/**").allowedOrigins(origin).allowedMethods("GET", "POST").allowedHeaders("Content-Type"); }
    @Override public void addInterceptors(InterceptorRegistry registry) { registry.addInterceptor(new HandlerInterceptor() {
        @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            if (!"POST".equals(request.getMethod()) || !request.getRequestURI().equals("/api/chat")) return true;
            String ip = request.getRemoteAddr(); long now = System.currentTimeMillis(); boolean[] limited = {false};
            hits.compute(ip, (key, window) -> { if (window == null || now - window.startedAt >= 60_000) window = new Window(now); limited[0] = window.count.incrementAndGet() > 20; return window; });
            if (limited[0]) { response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); response.setContentType("application/json"); response.getWriter().write("{\"message\":\"Please wait a little before sending another message.\"}"); return false; }
            return true;
        }
    }); }
}
