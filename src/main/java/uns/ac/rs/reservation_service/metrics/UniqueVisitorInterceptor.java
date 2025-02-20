package uns.ac.rs.reservation_service.metrics;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UniqueVisitorInterceptor implements HandlerInterceptor {
    private final UniqueVisitorTracker uniqueVisitorTracker;

    public UniqueVisitorInterceptor(UniqueVisitorTracker uniqueVisitorTracker) {
        this.uniqueVisitorTracker = uniqueVisitorTracker;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Extract IP address and user agent
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        uniqueVisitorTracker.trackVisitor(ipAddress, userAgent);
        System.out.println("IP: " + ipAddress + ", User-Agent: " + userAgent);
        return true;
    }
}
