package uns.ac.rs.reservation_service.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class UniqueVisitorTracker {
    private final MeterRegistry meterRegistry;
    private final Set<String> uniqueVisitors = new HashSet<>();

    public UniqueVisitorTracker(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        // Register a gauge to expose the number of unique visitors
        Gauge.builder("unique_visitors_total", uniqueVisitors, Set::size)
                .description("Total number of unique visitors")
                .register(meterRegistry);
    }

    public void trackVisitor(String ipAddress, String userAgent) {
        // Create a unique key combining IP address and user agent
        String visitorKey = ipAddress + "|" + userAgent;
        uniqueVisitors.add(visitorKey);
    }
}
