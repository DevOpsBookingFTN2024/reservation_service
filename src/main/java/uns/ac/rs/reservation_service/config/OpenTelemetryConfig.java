package uns.ac.rs.reservation_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;

@Configuration
public class OpenTelemetryConfig {
    @Bean
    public OtlpGrpcSpanExporter otlpHttpSpanExporter(@Value("${otel.exporter.otlp.endpoint}") String endpoint) {
        return OtlpGrpcSpanExporter.builder()
                .setEndpoint(endpoint)
                .build();
    }
}