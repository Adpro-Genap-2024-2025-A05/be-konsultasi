package id.ac.ui.cs.advprog.bekonsultasi.model.ScheduleState;

import id.ac.ui.cs.advprog.bekonsultasi.model.Schedule;

import java.util.UUID;

public class AvailableState implements ScheduleState {
    @Override
    public String getStatus() {
        return "AVAILABLE";
    }

    @Override
    public void approve(Schedule schedule) {
        throw new IllegalStateException("Cannot approve a schedule that hasn't been requested yet");
    }

    @Override
    public void reject(Schedule schedule) {
        throw new IllegalStateException("Cannot reject a schedule that hasn't been requested yet");
    }

    @Override
    public void request(Schedule schedule, UUID patientId) {
        schedule.setPatientId(patientId);
        schedule.setState(new RequestedState());
    }
}