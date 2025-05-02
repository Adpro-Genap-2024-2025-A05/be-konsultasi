package id.ac.ui.cs.advprog.bekonsultasi.service.factory;

import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleFactoryTest {
    @Test
    void testScheduleFactoryInterface() {
        ScheduleFactory factory = new CaregiverScheduleFactory();
        CreateScheduleDto dto = new CreateScheduleDto();
        dto.setDay(DayOfWeek.MONDAY);
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(10, 0));

        Schedule schedule = factory.createSchedule(dto, UUID.randomUUID());

        assertNotNull(schedule);
        assertEquals(DayOfWeek.MONDAY, schedule.getDay());
    }
}