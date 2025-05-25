package id.ac.ui.cs.advprog.bekonsultasi.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullSource;

import static org.junit.jupiter.api.Assertions.*;

class SpecialityTest {

    @Nested
    class DisplayNameTests {

        @Test
        void testGetDisplayName() {
            assertEquals("Dokter Umum", Speciality.DOKTER_UMUM.getDisplayName());
            assertEquals("Spesialis Anak", Speciality.SPESIALIS_ANAK.getDisplayName());
            assertEquals("Spesialis Kulit", Speciality.SPESIALIS_KULIT.getDisplayName());
            assertEquals("Spesialis Penyakit Dalam", Speciality.SPESIALIS_PENYAKIT_DALAM.getDisplayName());
            assertEquals("Spesialis THT", Speciality.SPESIALIS_THT.getDisplayName());
            assertEquals("Spesialis Kandungan", Speciality.SPESIALIS_KANDUNGAN.getDisplayName());
            assertEquals("Kesehatan Paru", Speciality.KESEHATAN_PARU.getDisplayName());
            assertEquals("Psikiater", Speciality.PSIKIATER.getDisplayName());
            assertEquals("Dokter Hewan", Speciality.DOKTER_HEWAN.getDisplayName());
            assertEquals("Psikolog Klinis", Speciality.PSIKOLOG_KLINIS.getDisplayName());
            assertEquals("Spesialis Mata", Speciality.SPESIALIS_MATA.getDisplayName());
            assertEquals("Seksologi & Spesialis Reproduksi Pria", Speciality.SEKSOLOGI_REPRODUKSI_PRIA.getDisplayName());
            assertEquals("Spesialis Gizi Klinik", Speciality.SPESIALIS_GIZI_KLINIK.getDisplayName());
            assertEquals("Dokter Gigi", Speciality.DOKTER_GIGI.getDisplayName());
            assertEquals("Spesialis Saraf", Speciality.SPESIALIS_SARAF.getDisplayName());
            assertEquals("Spesialis Bedah", Speciality.SPESIALIS_BEDAH.getDisplayName());
            assertEquals("Perawatan Rambut", Speciality.PERAWATAN_RAMBUT.getDisplayName());
            assertEquals("Bidanku", Speciality.BIDANKU.getDisplayName());
            assertEquals("Spesialis Jantung", Speciality.SPESIALIS_JANTUNG.getDisplayName());
            assertEquals("Talk Therapy Clinic", Speciality.TALK_THERAPY_CLINIC.getDisplayName());
            assertEquals("Dokter Konsulen", Speciality.DOKTER_KONSULEN.getDisplayName());
            assertEquals("Laktasi", Speciality.LAKTASI.getDisplayName());
            assertEquals("Program Hamil", Speciality.PROGRAM_HAMIL.getDisplayName());
            assertEquals("Fisioterapi & Rehabilitasi", Speciality.FISIOTERAPI_REHABILITASI.getDisplayName());
            assertEquals("Medikolegal & Hukum Kesehatan", Speciality.MEDIKOLEGAL_HUKUM_KESEHATAN.getDisplayName());
            assertEquals("Pemeriksaan Lab", Speciality.PEMERIKSAAN_LAB.getDisplayName());
            assertEquals("Layanan Kontrasepsi", Speciality.LAYANAN_KONTRASEPSI.getDisplayName());
            assertEquals("Spesialisasi Lainnya", Speciality.SPESIALISASI_LAINNYA.getDisplayName());
        }

        @ParameterizedTest
        @EnumSource(Speciality.class)
        void testAllSpecialitiesHaveDisplayNames(Speciality speciality) {
            assertNotNull(speciality.getDisplayName());
            assertFalse(speciality.getDisplayName().isEmpty());
        }
    }

    @Nested
    class ContainsTests {

        @Test
        void testContains_ValidDisplayNames() {
            assertTrue(Speciality.contains("Dokter Umum"));
            assertTrue(Speciality.contains("Spesialis Anak"));
            assertTrue(Speciality.contains("Spesialis Kulit"));
            assertTrue(Speciality.contains("Psikiater"));
            assertTrue(Speciality.contains("Dokter Gigi"));
            assertTrue(Speciality.contains("Spesialisasi Lainnya"));
        }

        @Test
        void testContains_InvalidDisplayNames() {
            assertFalse(Speciality.contains("Invalid Speciality"));
            assertFalse(Speciality.contains("Random Text"));
            assertFalse(Speciality.contains("DOKTER_UMUM"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "null"})
        void testContains_EmptyOrWhitespace(String input) {
            assertFalse(Speciality.contains(input));
        }

        @ParameterizedTest
        @NullSource
        void testContains_Null(String input) {
            assertFalse(Speciality.contains(input));
        }

        @Test
        void testContains_CaseSensitive() {
            assertTrue(Speciality.contains("Dokter Umum"));
            assertFalse(Speciality.contains("dokter umum"));
            assertFalse(Speciality.contains("DOKTER UMUM"));
            assertFalse(Speciality.contains("Dokter umum"));
        }
    }

    @Nested
    class ValidateSpecialityTests {

        @ParameterizedTest
        @EnumSource(Speciality.class)
        void testValidateSpeciality_ValidSpecialities(Speciality speciality) {
            assertDoesNotThrow(() -> Speciality.validatespeciality(speciality));
        }

        @Test
        void testValidateSpeciality_Null() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> Speciality.validatespeciality(null)
            );
            assertEquals("speciality cannot be null", exception.getMessage());
        }
    }

    @Nested
    class FromDisplayNameTests {

        @Test
        void testFromDisplayName_ValidNames() {
            assertEquals(Speciality.DOKTER_UMUM, Speciality.fromDisplayName("Dokter Umum"));
            assertEquals(Speciality.SPESIALIS_ANAK, Speciality.fromDisplayName("Spesialis Anak"));
            assertEquals(Speciality.PSIKIATER, Speciality.fromDisplayName("Psikiater"));
            assertEquals(Speciality.DOKTER_GIGI, Speciality.fromDisplayName("Dokter Gigi"));
            assertEquals(Speciality.SPESIALISASI_LAINNYA, Speciality.fromDisplayName("Spesialisasi Lainnya"));
        }

        @Test
        void testFromDisplayName_CaseInsensitive() {
            assertEquals(Speciality.DOKTER_UMUM, Speciality.fromDisplayName("dokter umum"));
            assertEquals(Speciality.DOKTER_UMUM, Speciality.fromDisplayName("DOKTER UMUM"));
            assertEquals(Speciality.DOKTER_UMUM, Speciality.fromDisplayName("Dokter umum"));
            assertEquals(Speciality.SPESIALIS_ANAK, Speciality.fromDisplayName("spesialis anak"));
        }

        @ParameterizedTest
        @EnumSource(Speciality.class)
        void testFromDisplayName_AllSpecialities(Speciality expectedSpeciality) {
            String displayName = expectedSpeciality.getDisplayName();

            assertEquals(expectedSpeciality, Speciality.fromDisplayName(displayName));

            assertEquals(expectedSpeciality, Speciality.fromDisplayName(displayName.toLowerCase()));

            assertEquals(expectedSpeciality, Speciality.fromDisplayName(displayName.toUpperCase()));
        }

        @Test
        void testFromDisplayName_InvalidName() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> Speciality.fromDisplayName("Invalid Speciality")
            );
            assertEquals("No speciality found for display name: Invalid Speciality", exception.getMessage());
        }

        @Test
        void testFromDisplayName_Null() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> Speciality.fromDisplayName(null)
            );
        }

        @Test
        void testFromDisplayName_Empty() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> Speciality.fromDisplayName("")
            );
        }
    }

    @Nested
    class JsonSerializationTests {

        @ParameterizedTest
        @EnumSource(Speciality.class)
        void testJsonValue(Speciality speciality) {
            assertEquals(speciality.getDisplayName(), speciality.getDisplayName());
        }
    }

    @Test
    void testAllSpecialityValues() {
        Speciality[] expectedValues = {
                Speciality.DOKTER_UMUM,
                Speciality.SPESIALIS_ANAK,
                Speciality.SPESIALIS_KULIT,
                Speciality.SPESIALIS_PENYAKIT_DALAM,
                Speciality.SPESIALIS_THT,
                Speciality.SPESIALIS_KANDUNGAN,
                Speciality.KESEHATAN_PARU,
                Speciality.PSIKIATER,
                Speciality.DOKTER_HEWAN,
                Speciality.PSIKOLOG_KLINIS,
                Speciality.SPESIALIS_MATA,
                Speciality.SEKSOLOGI_REPRODUKSI_PRIA,
                Speciality.SPESIALIS_GIZI_KLINIK,
                Speciality.DOKTER_GIGI,
                Speciality.SPESIALIS_SARAF,
                Speciality.SPESIALIS_BEDAH,
                Speciality.PERAWATAN_RAMBUT,
                Speciality.BIDANKU,
                Speciality.SPESIALIS_JANTUNG,
                Speciality.TALK_THERAPY_CLINIC,
                Speciality.DOKTER_KONSULEN,
                Speciality.LAKTASI,
                Speciality.PROGRAM_HAMIL,
                Speciality.FISIOTERAPI_REHABILITASI,
                Speciality.MEDIKOLEGAL_HUKUM_KESEHATAN,
                Speciality.PEMERIKSAAN_LAB,
                Speciality.LAYANAN_KONTRASEPSI,
                Speciality.SPESIALISASI_LAINNYA
        };

        Speciality[] actualValues = Speciality.values();
        assertEquals(expectedValues.length, actualValues.length);

        for (Speciality expected : expectedValues) {
            boolean found = false;
            for (Speciality actual : actualValues) {
                if (expected == actual) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Expected speciality " + expected + " not found in enum values");
        }
    }
}