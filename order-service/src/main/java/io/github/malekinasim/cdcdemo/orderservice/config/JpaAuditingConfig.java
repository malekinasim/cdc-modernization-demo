package io.github.malekinasim.cdcdemo.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Optional;

/**
 * Fills LAST_UPD_BY and LAST_UPD_TS on every write.
 *
 * <p>The actor is taken from the {@code X-Updated-By} request header, so the sync process can
 * identify itself as {@code SYNC}. The middleware then drops CDC events written by the sync and a
 * change is never routed back to the system it came from. Anything else counts as {@code APP}.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware", dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaAuditingConfig {

    public static final String UPDATED_BY_HEADER = "X-Updated-By";
    public static final String DEFAULT_ACTOR = "APP";

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of(currentActor());
    }

    @Bean
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(Instant.now());
    }

    private String currentActor() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return DEFAULT_ACTOR;
        }
        String actor = attributes.getRequest().getHeader(UPDATED_BY_HEADER);
        return (actor == null || actor.isBlank()) ? DEFAULT_ACTOR : actor.trim();
    }
}
