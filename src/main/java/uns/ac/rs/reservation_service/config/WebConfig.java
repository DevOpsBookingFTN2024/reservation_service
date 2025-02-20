package uns.ac.rs.reservation_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uns.ac.rs.reservation_service.metrics.UniqueVisitorInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final UniqueVisitorInterceptor uniqueVisitorInterceptor;

    @Autowired
    public WebConfig(UniqueVisitorInterceptor uniqueVisitorInterceptor) {
        this.uniqueVisitorInterceptor = uniqueVisitorInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(uniqueVisitorInterceptor);
    }
}
