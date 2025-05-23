package id.ac.ui.cs.advprog.bekonsultasi.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonCreator;

@Getter
public enum Speciality {
    DOKTER_UMUM("Dokter Umum"),
    SPESIALIS_ANAK("Spesialis Anak"),
    SPESIALIS_KULIT("Spesialis Kulit"),
    SPESIALIS_PENYAKIT_DALAM("Spesialis Penyakit Dalam"),
    SPESIALIS_THT("Spesialis THT"),
    SPESIALIS_KANDUNGAN("Spesialis Kandungan"),
    KESEHATAN_PARU("Kesehatan Paru"),
    PSIKIATER("Psikiater"),
    DOKTER_HEWAN("Dokter Hewan"),
    PSIKOLOG_KLINIS("Psikolog Klinis"),
    SPESIALIS_MATA("Spesialis Mata"),
    SEKSOLOGI_REPRODUKSI_PRIA("Seksologi & Spesialis Reproduksi Pria"),
    SPESIALIS_GIZI_KLINIK("Spesialis Gizi Klinik"),
    DOKTER_GIGI("Dokter Gigi"),
    SPESIALIS_SARAF("Spesialis Saraf"),
    SPESIALIS_BEDAH("Spesialis Bedah"),
    PERAWATAN_RAMBUT("Perawatan Rambut"),
    BIDANKU("Bidanku"),
    SPESIALIS_JANTUNG("Spesialis Jantung"),
    TALK_THERAPY_CLINIC("Talk Therapy Clinic"),
    DOKTER_KONSULEN("Dokter Konsulen"),
    LAKTASI("Laktasi"),
    PROGRAM_HAMIL("Program Hamil"),
    FISIOTERAPI_REHABILITASI("Fisioterapi & Rehabilitasi"),
    MEDIKOLEGAL_HUKUM_KESEHATAN("Medikolegal & Hukum Kesehatan"),
    PEMERIKSAAN_LAB("Pemeriksaan Lab"),
    LAYANAN_KONTRASEPSI("Layanan Kontrasepsi"),
    SPESIALISASI_LAINNYA("Spesialisasi Lainnya");
    
    private final String displayName;
    
    private Speciality(String displayName) {
        this.displayName = displayName;
    }
    
    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
    
    public static boolean contains(String param) {
        if (param == null || param.trim().isEmpty()) {
            return false;
        }
        
        for (Speciality speciality : Speciality.values()) {
            if (speciality.getDisplayName().equals(param)) {
                return true;
            }
        }
        return false;
    }

    public static void validatespeciality(Speciality speciality) {
        if (speciality == null) {
            throw new IllegalArgumentException("speciality cannot be null");
        }

        boolean isValid = Arrays.stream(Speciality.values())
                .anyMatch(spec -> spec.equals(speciality));
        
        if (!isValid) {
            throw new IllegalArgumentException("Invalid speciality: " + speciality + 
                ". Must be one of: " + Arrays.toString(Speciality.values()));
        }
    }

    @JsonCreator
    public static Speciality fromDisplayName(String displayName) {
        for (Speciality speciality : Speciality.values()) {
            if (speciality.getDisplayName().equalsIgnoreCase(displayName)) {
                return speciality;
            }
        }
        throw new IllegalArgumentException("No speciality found for display name: " + displayName);
    }
}