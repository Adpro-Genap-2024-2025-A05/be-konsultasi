package id.ac.ui.cs.advprog.bekonsultasi.service.factory;

import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateOneTimeScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleFactoryTest {

    private ScheduleFactory scheduleFactory;
    private UUID caregiverId;

    @BeforeEach
    void setUp() {
        scheduleFactory = new CaregiverScheduleFactory();
        caregiverId = UUID.randomUUID();
    }

    @Test
    void testCreateRecurringSchedule() {
        DayOfWeek day = DayOfWeek.MONDAY;
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);

        CreateScheduleDto dto = CreateScheduleDto.builder()
                .day(day)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        Schedule schedule = scheduleFactory.createSchedule(dto, caregiverId);

        assertNotNull(schedule);
        assertEquals(caregiverId, schedule.getCaregiverId());
        assertEquals(day, schedule.getDay());
        assertEquals(startTime, schedule.getStartTime());
        assertEquals(endTime, schedule.getEndTime());
        assertFalse(schedule.isOneTime());
        assertNull(schedule.getSpecificDate());
    }

    @Test
    void testCreateOneTimeSchedule() {
        LocalDate specificDate = LocalDate.of(2025, 6, 2);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);

        CreateOneTimeScheduleDto dto = CreateOneTimeScheduleDto.builder()
                .specificDate(specificDate)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        Schedule schedule = scheduleFactory.createOneTimeSchedule(dto, caregiverId);

        assertNotNull(schedule);
        assertEquals(caregiverId, schedule.getCaregiverId());
        assertEquals(specificDate.getDayOfWeek(), schedule.getDay());
        assertEquals(startTime, schedule.getStartTime());
        assertEquals(endTime, schedule.getEndTime());
        assertTrue(schedule.isOneTime());
        assertEquals(specificDate, schedule.getSpecificDate());
    }
}