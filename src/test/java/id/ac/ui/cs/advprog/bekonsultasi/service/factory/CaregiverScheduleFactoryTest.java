package id.ac.ui.cs.advprog.bekonsultasi.service.factory;

import id.ac.ui.cs.advprog.bekonsultasi.dto.CreateScheduleDto;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState.AvailableState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CaregiverScheduleFactoryTest {
    private CaregiverScheduleFactory factory;
    private CreateScheduleDto dto;
    private UUID caregiverId;

    @BeforeEach
    void setUp() {
        factory = new CaregiverScheduleFactory();
        caregiverId = UUID.randomUUID();

        dto = new CreateScheduleDto();
        dto.setDay(DayOfWeek.MONDAY);
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(10, 0));
    }

    @Test
    void testCreateSchedule() {
        Schedule schedule = factory.createSchedule(dto, caregiverId);

        assertNotNull(schedule);
        assertEquals(caregiverId, schedule.getCaregiverId());
        assertEquals(DayOfWeek.MONDAY, schedule.getDay());
        assertEquals(LocalTime.of(9, 0), schedule.getStartTime());
        assertEquals(LocalTime.of(10, 0), schedule.getEndTime());
        assertEquals("AVAILABLE", schedule.getStatus());
        assertTrue(schedule.getState() instanceof AvailableState);
    }

    @Test
    void testCreateScheduleWithDifferentTimes() {
        dto.setStartTime(LocalTime.of(14, 0));
        dto.setEndTime(LocalTime.of(15, 30));

        Schedule schedule = factory.createSchedule(dto, caregiverId);

        assertEquals(LocalTime.of(14, 0), schedule.getStartTime());
        assertEquals(LocalTime.of(15, 30), schedule.getEndTime());
    }
}