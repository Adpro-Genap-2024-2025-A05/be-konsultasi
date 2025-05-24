package id.ac.ui.cs.advprog.bekonsultasi.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter konsultasiCreatedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("konsultasi.created.total")
                .description("Total number of consultations created")
                .register(meterRegistry);
    }

    @Bean
    public Counter konsultasiConfirmedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("konsultasi.confirmed.total")
                .description("Total number of consultations confirmed")
                .register(meterRegistry);
    }

    @Bean
    public Counter konsultasiCancelledCounter(MeterRegistry meterRegistry) {
        return Counter.builder("konsultasi.cancelled.total")
                .description("Total number of consultations cancelled")
                .register(meterRegistry);
    }

    @Bean
    public Counter konsultasiCompletedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("konsultasi.completed.total")
                .description("Total number of consultations completed")
                .register(meterRegistry);
    }

    @Bean
    public Counter konsultasiRescheduledCounter(MeterRegistry meterRegistry) {
        return Counter.builder("konsultasi.rescheduled.total")
                .description("Total number of consultations rescheduled")
                .register(meterRegistry);
    }

    @Bean
    public Counter konsultasiUpdateRequestCounter(MeterRegistry meterRegistry) {
        return Counter.builder("konsultasi.update.request.total")
                .description("Total number of consultation update requests")
                .register(meterRegistry);
    }

    @Bean
    public Counter konsultasiRescheduleAcceptedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("konsultasi.reschedule.accepted.total")
                .description("Total number of reschedule proposals accepted")
                .register(meterRegistry);
    }

    @Bean
    public Counter konsultasiRescheduleRejectedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("konsultasi.reschedule.rejected.total")
                .description("Total number of reschedule proposals rejected")
                .register(meterRegistry);
    }

    @Bean
    public Counter konsultasiErrorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("konsultasi.errors.total")
                .description("Total number of errors in consultation operations")
                .register(meterRegistry);
    }

    @Bean
    public Counter konsultasiScheduleConflictCounter(MeterRegistry meterRegistry) {
        return Counter.builder("konsultasi.schedule.conflict.total")
                .description("Total number of schedule conflicts")
                .register(meterRegistry);
    }

    @Bean
    public Counter konsultasiStateTransitionErrorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("konsultasi.state.transition.error.total")
                .description("Total number of invalid state transitions")
                .register(meterRegistry);
    }
    
    @Bean
    public Counter userDataRequestCounter(MeterRegistry meterRegistry) {
        return Counter.builder("data.user.request.total")
                .description("Total number of user data requests")
                .register(meterRegistry);
    }

    @Bean
    public Counter caregiverDataRequestCounter(MeterRegistry meterRegistry) {
        return Counter.builder("data.caregiver.request.total")
                .description("Total number of caregiver data requests")
                .register(meterRegistry);
    }

    @Bean
    public Counter pacilianDataRequestCounter(MeterRegistry meterRegistry) {
        return Counter.builder("data.pacilian.request.total")
                .description("Total number of pacilian data requests")
                .register(meterRegistry);
    }

    @Bean
    public Timer userDataFetchTimer(MeterRegistry meterRegistry) {
        return Timer.builder("data.user.fetch.duration")
                .description("Time taken to fetch user data from external service")
                .register(meterRegistry);
    }

    @Bean
    public Counter userDataFetchErrorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("data.user.fetch.error.total")
                .description("Total number of errors fetching user data")
                .register(meterRegistry);
    }

    @Bean
    public Counter userDataFallbackCounter(MeterRegistry meterRegistry) {
        return Counter.builder("data.user.fallback.total")
                .description("Total number of times fallback data was used")
                .register(meterRegistry);
    }

    @Bean
    public Counter availableSchedulesRequestCounter(MeterRegistry meterRegistry) {
        return Counter.builder("data.available.schedules.request.total")
                .description("Total number of available schedules requests")
                .register(meterRegistry);
    }

    @Bean
    public Timer availableSchedulesQueryTimer(MeterRegistry meterRegistry) {
        return Timer.builder("data.available.schedules.query.duration")
                .description("Time taken to query available schedules")
                .register(meterRegistry);
    }
    
    @Bean
    public Counter tokenVerificationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("auth.token.verification.total")
                .description("Total number of token verifications")
                .register(meterRegistry);
    }

    @Bean
    public Counter tokenVerificationFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("auth.token.verification.failure.total")
                .description("Total number of failed token verifications")
                .register(meterRegistry);
    }

    @Bean
    public Timer tokenVerificationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("auth.token.verification.duration")
                .description("Time taken for token verification")
                .register(meterRegistry);
    }

    @Bean
    public Counter authenticationErrorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("auth.authentication.error.total")
                .description("Total number of authentication errors")
                .register(meterRegistry);
    }

    @Bean
    public Counter unauthorizedAccessCounter(MeterRegistry meterRegistry) {
        return Counter.builder("auth.unauthorized.access.total")
                .description("Total number of unauthorized access attempts")
                .register(meterRegistry);
    }
    
    @Bean
    public Timer databaseQueryTimer(MeterRegistry meterRegistry) {
        return Timer.builder("database.query.duration")
                .description("Time taken for database queries")
                .register(meterRegistry);
    }

    @Bean
    public Counter databaseErrorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("database.error.total")
                .description("Total number of database errors")
                .register(meterRegistry);
    }

    @Bean
    public Counter databaseConnectionErrorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("database.connection.error.total")
                .description("Total number of database connection errors")
                .register(meterRegistry);
    }
    
    @Bean
    public Counter httpRequestCounter(MeterRegistry meterRegistry) {
        return Counter.builder("http.requests.total")
                .description("Total number of HTTP requests")
                .register(meterRegistry);
    }

    @Bean
    public Counter httpErrorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("http.errors.total")
                .description("Total number of HTTP errors (4xx, 5xx)")
                .register(meterRegistry);
    }

    @Bean
    public Timer httpRequestTimer(MeterRegistry meterRegistry) {
        return Timer.builder("http.request.duration")
                .description("HTTP request processing time")
                .register(meterRegistry);
    }
    
    @Bean
    public Counter asyncOperationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("async.operation.total")
                .description("Total number of async operations")
                .register(meterRegistry);
    }

    @Bean
    public Counter asyncOperationFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("async.operation.failure.total")
                .description("Total number of failed async operations")
                .register(meterRegistry);
    }

    @Bean
    public Timer asyncOperationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("async.operation.duration")
                .description("Time taken for async operations")
                .register(meterRegistry);
    }
    
    @Bean
    public Counter externalServiceCallCounter(MeterRegistry meterRegistry) {
        return Counter.builder("external.service.call.total")
                .description("Total number of external service calls")
                .register(meterRegistry);
    }

    @Bean
    public Counter externalServiceErrorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("external.service.error.total")
                .description("Total number of external service errors")
                .register(meterRegistry);
    }

    @Bean
    public Timer externalServiceCallTimer(MeterRegistry meterRegistry) {
        return Timer.builder("external.service.call.duration")
                .description("Time taken for external service calls")
                .register(meterRegistry);
    }
}