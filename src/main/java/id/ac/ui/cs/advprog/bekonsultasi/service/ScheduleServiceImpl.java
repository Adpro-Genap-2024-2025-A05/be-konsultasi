package id.ac.ui.cs.advprog.bekonsultasi.service;

import id.ac.ui.cs.advprog.bekonsultasi.factory.ScheduleFactoryImpl;
import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;
import id.ac.ui.cs.advprog.bekonsultasi.repository.ScheduleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final ScheduleFactoryImpl scheduleFactory;

    public ScheduleServiceImpl(ScheduleRepository scheduleRepository, ScheduleFactoryImpl scheduleFactory) {
        this.scheduleRepository = scheduleRepository;
        this.scheduleFactory = scheduleFactory;
    }

    @Override
    public Schedule createSchedule(UUID caregiverId, String day, String time) {
        Schedule schedule = scheduleFactory.createAvailableSchedule(caregiverId, day, time);
        return scheduleRepository.save(schedule);
    }

    @Override
    public List<Schedule> getAvailableSchedules(UUID caregiverId) {
        return scheduleRepository.findByCaregiverIdAndStatus(caregiverId, "Available");
    }

    @Override
    public Schedule getScheduleById(UUID id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found with id: " + id));
    }

    @Override
    public void updateScheduleStatus(UUID id, String status) {
        Schedule schedule = getScheduleById(id);
        schedule.changeStatus(status);
        scheduleRepository.save(schedule);
    }
}