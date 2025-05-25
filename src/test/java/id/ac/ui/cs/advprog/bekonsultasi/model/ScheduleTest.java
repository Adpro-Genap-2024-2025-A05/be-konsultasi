package id.ac.ui.cs.advprog.bekonsultasi.model;

import org.junit.jupiter.api.Test;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleTest {

    @Test
    void testBuildSchedule() {
        UUID caregiverId = UUID.randomUUID();
        DayOfWeek day = DayOfWeek.MONDAY;
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);

        Schedule schedule = Schedule.builder()
                .caregiverId(caregiverId)
                .day(day)
                .startTime(startTime)
                .endTime(endTime)
                .oneTime(false)
                .build();

        assertEquals(caregiverId, schedule.getCaregiverId());
        assertEquals(day, schedule.getDay());
        assertEquals(startTime, schedule.getStartTime());
        assertEquals(endTime, schedule.getEndTime());
        assertFalse(schedule.isOneTime());
        assertNull(schedule.getSpecificDate());
    }

    @Test
    void testBuildOneTimeSchedule() {
        UUID caregiverId = UUID.randomUUID();
        DayOfWeek day = DayOfWeek.MONDAY;
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);
        LocalDate specificDate = LocalDate.of(2025, 6, 2);

        Schedule schedule = Schedule.builder()
                .caregiverId(caregiverId)
                .day(day)
                .startTime(startTime)
                .endTime(endTime)
                .specificDate(specificDate)
                .oneTime(true)
                .build();

        assertEquals(caregiverId, schedule.getCaregiverId());
        assertEquals(day, schedule.getDay());
        assertEquals(startTime, schedule.getStartTime());
        assertEquals(endTime, schedule.getEndTime());
        assertTrue(schedule.isOneTime());
        assertEquals(specificDate, schedule.getSpecificDate());
    }

    @Test
    void testSettersAndGetters() {
        Schedule schedule = new Schedule();

        UUID id = UUID.randomUUID();
        UUID caregiverId = UUID.randomUUID();
        DayOfWeek day = DayOfWeek.MONDAY;
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);
        LocalDate specificDate = LocalDate.of(2025, 6, 2);

        schedule.setId(id);
        schedule.setCaregiverId(caregiverId);
        schedule.setDay(day);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setSpecificDate(specificDate);
        schedule.setOneTime(true);

        assertEquals(id, schedule.getId());
        assertEquals(caregiverId, schedule.getCaregiverId());
        assertEquals(day, schedule.getDay());
        assertEquals(startTime, schedule.getStartTime());
        assertEquals(endTime, schedule.getEndTime());
        assertEquals(specificDate, schedule.getSpecificDate());
        assertTrue(schedule.isOneTime());
    }
}