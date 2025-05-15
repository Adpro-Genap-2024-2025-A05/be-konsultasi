# Deployment

Link: [BE-Konsultasi Deployment](http://52.202.150.103/)

# Schedule Component - BE-Konsultasi Service

## Overview
- Dokter dapat membuat jadwal (hari dan jam) mingguan baru dengan status available. (C)
- Dokter dapat melihat daftar permintaan konsultasi (berhubungan dengan use case 3). (R)
- Dokter dapat menerima, menolak, atau mengubah jadwal sesuai permintaan (perlu konfirmasi dari pasien). (U)
- Jika disetujui, status jadwal konsultasi akan menjadi disetujui dan pengguna akan dapat melaksanakan konsultasi di jadwal yang disetujui. (U)
- Jika ditolak, status jadwal konsultasi akan menjadi ditolak, dan pengguna dapat membuat jadwal konsultasi baru lagi. (U)


Komponen Schedule bertanggung jawab untuk mengelola jadwal ketersediaan dokter/caregiver dalam sistem manajemen konsultasi BeKonsultasi. Komponen ini mengimplementasikan beberapa design pattern untuk menangani complex state transitions dan proses pembuatan objek yang terlibat dalam pengelolaan jadwal.

## Key Features
- Membuat available time slots untuk dokter
- Melihat jadwal tersedia untuk dokter tertentu
- Memperbarui status jadwal (Available, Booked, Unavailable)
- Melacak transisi status jadwal

## Implemented Design Patterns

### 1. State Pattern
Komponen Schedule menggunakan State Pattern untuk mengelola berbagai status jadwal:
- Interface `ScheduleState` mendefinisikan contract untuk semua state classes
- `AvailableState`, `BookedState`, dan `UnavailableState` mengimplementasikan behavior spesifik untuk masing-masing status
- Setiap status meng-encapsulate behavior yang sesuai
- State menangani transisi antar status

State Pattern dipilih karena jadwal konsultasi memiliki beberapa status yang berbeda (Available, Booked, Unavailable) dengan perilaku yang berbeda-beda. Dengan menggunakan pattern ini, kita dapat menghindari penggunaan if-else atau switch statements yang panjang dan kompleks. Pattern ini memungkinkan kita untuk dengan mudah menambahkan status baru tanpa mengubah kode yang sudah ada, sehingga meningkatkan maintainability dan extensibility dari sistem.

### 2. Factory Pattern
Factory Pattern digunakan untuk membuat instance Schedule:
- Interface `ScheduleFactory` mendefinisikan creation methods
- `ScheduleFactoryImpl` menangani inisialisasi jadwal dengan status yang benar
- Memastikan jadwal dibuat secara konsisten
- Memusatkan creation logic dalam satu komponen

Factory Pattern diimplementasikan karena pembuatan objek Schedule memerlukan beberapa langkah, terutama untuk menetapkan state yang sesuai. Dengan menggunakan pattern ini, kita dapat menyembunyikan kompleksitas pembuatan objek dari client dan memastikan bahwa setiap jadwal dibuat dengan state yang tepat. Pattern ini juga memungkinkan kita untuk memusatkan logika pembuatan objek di satu tempat, sehingga jika ada perubahan dalam cara membuat Schedule, kita hanya perlu mengubah kode di satu tempat.

### 3. Builder Pattern
Builder Pattern (melalui anotasi Lombok `@Builder`) digunakan untuk membangun objek Schedule:
- Memungkinkan pembuatan complex objects dengan multiple fields
- Meningkatkan code readability dan maintainability
- Mendukung immutability jika diperlukan
- Menyederhanakan pembuatan objek saat testing

Builder Pattern dipilih karena objek Schedule memiliki beberapa field yang harus diinisialisasi. Pattern ini membuat kode lebih bersih dan mudah dibaca dengan memungkinkan pembuatan objek secara step-by-step. Dengan menggunakan Lombok `@Builder`, kita dapat mengurangi boilerplate code yang diperlukan untuk implementasi builder. Pattern ini sangat membantu saat membuat objek untuk unit testing, di mana kita perlu membuat berbagai objek Schedule dengan nilai yang berbeda-beda. Selain itu, pattern ini memungkinkan kita untuk menciptakan objek yang immutable, yang meningkatkan thread safety dan mengurangi bug yang terkait dengan perubahan state yang tidak diinginkan.

## Project Structure (for now)

### Model Layer
- `Schedule.java`: Main data model yang menyimpan informasi jadwal
- `ScheduleState.java`: Interface untuk State Pattern
- `AvailableState.java`: Available status
- `BookedState.java`: Booked status
- `UnavailableState.java`: Unavailable status

### Repository Layer
- `ScheduleRepository.java`: Menangani penyimpanan dan pengambilan data jadwal

### Factory Layer
- `ScheduleFactory.java`: Interface untuk Factory Pattern
- `ScheduleFactoryImpl.java`: Implementasi factory untuk membuat Schedule objects

### Service Layer
- `ScheduleService.java`: Interface yang mendefinisikan business operations
- `ScheduleServiceImpl.java`: Service implementation untuk mengelola jadwal  

- `ScheduleServiceImpl.java`: Service implementation untuk mengelola jadwal

# Konsultasi Component - BE-Konsultasi Service

## Overview
- Pasien dapat membuat permintaan konsultasi dengan dokter pada jadwal yang tersedia.
- Pasien dan dokter dapat melihat detail dan riwayat konsultasi.
- Dokter dapat mengkonfirmasi atau membatalkan permintaan konsultasi.
- Pasien dapat membatalkan konsultasi (dengan batasan waktu 24 jam sebelum konsultasi).
- Dokter dapat menandai konsultasi sebagai "telah selesai".
- Status konsultasi mencatat seluruh siklus konsultasi, mulai dari permintaan (_requested_) hingga selesai (_done_).

## Key Features
- Mengelola permintaan konsultasi antara pasien dan dokter.
- Menangani transisi status konsultasi.
- Mencatat riwayat perubahan status konsultasi.
- Menerapkan fitur pembatalan H-1.

## Implemented Design Patterns - State Pattern

### Komponen Struktur:

- **Interface KonsultasiState**: Mendefinisikan _blueprint_ untuk semua state 

  Metode: `confirm(), cancel(), complete(), reschedule(), getStateName()`


- **Concrete States**:

  - `RequestedState`: Menangani konsultasi yang baru dibuat
  - `ConfirmedState`: Menerapkan validasi 24 jam untuk pembatalan
  - `CancelledState`: Mencegah modifikasi pada konsultasi yang dibatalkan
  - `DoneState`: Status final setelah konsultasi selesai


- **Context (Konsultasi)**:

  - Menyimpan referensi ke objek state saat ini
  - Mendelegasikan permintaan ke state
  - Memungkinkan perubahan state saat runtime

### Keunggulan Implementasi:

- Menghilangkan kompleksitas kondisional (if-else chains)
- Perilaku spesifik state terenkapsulasi dalam kelas terpisah 
- Memudahkan penambahan state baru (Open/Closed Principle)
- Transisi state menjadi eksplisit dan terdokumentasi 
- Meningkatkan testability karena setiap state dapat diuji secara terpisah

### Transisi State:

- REQUESTED → CONFIRMED (dokter menyetujui)
- REQUESTED → CANCELLED (pasien/dokter membatalkan)
- CONFIRMED → DONE (konsultasi selesai)
- CONFIRMED → CANCELLED (dibatalkan, >24 jam sebelum jadwal)


## Project Structure

### Model Layer

- `Konsultasi.java`: Model utama yang menyimpan data konsultasi dan state saat ini
- `state/KonsultasiState.java`: Interface untuk State Pattern 
- `state/RequestedState.java`: Status permintaan awal 
- `state/ConfirmedState.java`: Status setelah dikonfirmasi dokter 
- `state/CancelledState.java`: Status ketika dibatalkan 
- `state/DoneState.java`: Status setelah konsultasi selesai

### Repository Layer

- `KonsultasiRepository.java`: Menangani penyimpanan dan pengambilan data konsultasi 

### Service Layer

- `KonsultasiService.java`: Interface yang mendefinisikan operasi bisnis
- `KonsultasiServiceImpl.java`: Implementasi service untuk mengelola konsultasi