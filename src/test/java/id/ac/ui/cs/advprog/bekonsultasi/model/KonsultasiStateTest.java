package id.ac.ui.cs.advprog.bekonsultasi.model;

import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState.CancelledState;
import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState.ConfirmedState;
import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState.DoneState;
import id.ac.ui.cs.advprog.bekonsultasi.model.KonsultasiState.RequestedState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class KonsultasiStateTest {

    private Konsultasi konsultasi;
    private String paciliansId;
    private String careGiverId;
    private LocalDateTime futureDate;
    private LocalDateTime nearFutureDate;

    @BeforeEach
    public void setUp() {
        paciliansId = "P001";
        careGiverId = "C001";

        futureDate = LocalDateTime.now().plusDays(2);
        nearFutureDate = LocalDateTime.now().plusHours(12);

        konsultasi = new Konsultasi(paciliansId, careGiverId, futureDate, "Test konsultasi");
    }

    @Test
    public void testInitialState() {
        assertTrue(konsultasi.getState() instanceof RequestedState);
        assertEquals("REQUESTED", konsultasi.getStateValue());
    }

    @Test
    public void testConfirmConsultation() {
        konsultasi.confirm();
        assertTrue(konsultasi.getState() instanceof ConfirmedState);
        assertEquals("CONFIRMED", konsultasi.getStateValue());

        assertEquals(1, konsultasi.getHistoryList().size());
        assertEquals("Konsultasi dikonfirmasi", konsultasi.getHistoryList().get(0).getDescription());
    }

    @Test
    public void testCancelConfirmedConsultation() {
        konsultasi.confirm();
        konsultasi.cancel();

        assertTrue(konsultasi.getState() instanceof CancelledState);
        assertEquals("CANCELLED", konsultasi.getStateValue());

        assertEquals(2, konsultasi.getHistoryList().size());
        assertEquals("Konsultasi dibatalkan", konsultasi.getHistoryList().get(1).getDescription());
    }

    @Test
    public void testCancelRequestedConsultation() {
        konsultasi.cancel();

        assertTrue(konsultasi.getState() instanceof CancelledState);
        assertEquals("CANCELLED", konsultasi.getStateValue());

        assertEquals(1, konsultasi.getHistoryList().size());
        assertEquals("Konsultasi dibatalkan saat masih dalam status permintaan",
                konsultasi.getHistoryList().get(0).getDescription());
    }

    @Test
    public void testCompleteConfirmedConsultation() {
        konsultasi.confirm();

        konsultasi.complete();

        assertTrue(konsultasi.getState() instanceof DoneState);
        assertEquals("DONE", konsultasi.getStateValue());

        assertEquals(2, konsultasi.getHistoryList().size());
        assertEquals("Konsultasi telah selesai",
                konsultasi.getHistoryList().get(1).getDescription());
    }

    @Test
    public void testCannotCompleteRequestedConsultation() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            konsultasi.complete();
        });

        assertEquals("Tidak dapat menyelesaikan konsultasi yang belum dikonfirmasi",
                exception.getMessage());

        assertTrue(konsultasi.getState() instanceof RequestedState);
    }

    @Test
    public void testCannotCancelConfirmedConsultationWithin24Hours() {
        konsultasi.setSchedule(nearFutureDate);
        konsultasi.confirm();

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            konsultasi.cancel();
        });

        assertEquals("Tidak dapat membatalkan konsultasi kurang dari 24 jam sebelum jadwal",
                exception.getMessage());

        assertTrue(konsultasi.getState() instanceof ConfirmedState);
    }

    @Test
    public void testCannotRescheduleConfirmedConsultationWithin24Hours() {
        konsultasi.setSchedule(nearFutureDate);
        konsultasi.confirm();

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            konsultasi.reschedule(futureDate.plusDays(1));
        });

        assertEquals("Tidak dapat mengubah jadwal konsultasi kurang dari 24 jam sebelum jadwal",
                exception.getMessage());

        assertEquals(nearFutureDate, konsultasi.getSchedule());
    }

    @Test
    public void testRescheduleRequestedConsultation() {
        LocalDateTime newSchedule = futureDate.plusDays(3);

        konsultasi.reschedule(newSchedule);

        assertEquals(newSchedule, konsultasi.getSchedule());

        assertTrue(konsultasi.getState() instanceof RequestedState);

        assertEquals(1, konsultasi.getHistoryList().size());
        assertEquals("Jadwal konsultasi diubah saat masih dalam status permintaan",
                konsultasi.getHistoryList().get(0).getDescription());
    }

    @Test
    public void testCannotEditCancelledConsultation() {
        konsultasi.cancel();

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            konsultasi.confirm();
        });

        assertEquals("Konsultasi yang dibatalkan tidak dapat dikonfirmasi",
                exception.getMessage());
    }

    @Test
    public void testCannotConfirmDoneConsultation() {
        konsultasi.confirm();
        konsultasi.complete();

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            konsultasi.confirm();
        });

        assertEquals("Konsultasi yang sudah selesai tidak dapat dikonfirmasi",
                exception.getMessage());
    }

    @Test
    public void testCannotRescheduleCancelledConsultation() {
        konsultasi.cancel();

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            konsultasi.reschedule(futureDate.plusDays(1));
        });

        assertEquals("Konsultasi yang dibatalkan tidak dapat dijadwalkan ulang",
                exception.getMessage());
    }

    @Test
    public void testCannotCancelDoneConsultation() {
        konsultasi.confirm();
        konsultasi.complete();

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            konsultasi.cancel();
        });

        assertEquals("Konsultasi yang sudah selesai tidak dapat dibatalkan",
                exception.getMessage());
    }
}
