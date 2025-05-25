package id.ac.ui.cs.advprog.bekonsultasi.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScheduleMetricsConfig {

    @Bean
    public Counter scheduleCreatedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_created_total")
                .description("Total number of regular schedules created")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleOneTimeCreatedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_onetime_created_total")
                .description("Total number of one-time schedules created")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleUpdatedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_updated_total")
                .description("Total number of schedules updated")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleDeletedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_deleted_total")
                .description("Total number of schedules deleted")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleDeleteAsyncCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_delete_async_total")
                .description("Total number of async schedule deletions")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleAvailabilityCheckCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_availability_check_total")
                .description("Total number of schedule availability checks")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleAvailableTimesRequestCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_available_times_request_total")
                .description("Total number of available times requests")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleCaregiverQueryCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_caregiver_query_total")
                .description("Total number of caregiver schedule queries")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleAllQueryCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_all_query_total")
                .description("Total number of all schedules queries")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleAvailableByIdCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_available_by_id_total")
                .description("Total number of available schedules by ID queries")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleAvailableMultipleCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_available_multiple_total")
                .description("Total number of available schedules for multiple caregivers")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleConflictCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_conflict_total")
                .description("Total number of schedule conflicts detected")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleValidationErrorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_validation_error_total")
                .description("Total number of schedule validation errors")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleAuthorizationErrorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_authorization_error_total")
                .description("Total number of schedule authorization errors")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleNotFoundCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_not_found_total")
                .description("Total number of schedule not found errors")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleTimeValidationErrorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_time_validation_error_total")
                .description("Total number of time validation errors (end before start)")
                .register(meterRegistry);
    }

    @Bean
    public Counter schedulePastDateErrorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_past_date_error_total")
                .description("Total number of past date errors for one-time schedules")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleActiveKonsultasiBlockCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_active_konsultasi_block_total")
                .description("Total number of operations blocked by active consultations")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleDatabaseErrorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_database_error_total")
                .description("Total number of database errors in schedule operations")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleGeneralErrorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_errors_total")
                .description("Total number of general errors in schedule operations")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleOverlapPreventedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_overlap_prevented_total")
                .description("Total number of overlapping schedules prevented")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleWeeklyScheduleCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_weekly_created_total")
                .description("Total number of weekly recurring schedules created")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleFactoryRegularCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_factory_regular_total")
                .description("Total number of regular schedules created via factory")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleFactoryOneTimeCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_factory_onetime_total")
                .description("Total number of one-time schedules created via factory")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleSuccessfulOperationsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_successful_operations_total")
                .description("Total number of successful schedule operations")
                .register(meterRegistry);
    }

    @Bean
    public Counter scheduleFailedOperationsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("schedule_failed_operations_total")
                .description("Total number of failed schedule operations")
                .register(meterRegistry);
    }
}