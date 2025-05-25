package id.ac.ui.cs.advprog.bekonsultasi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseDto<T> {
    private int status;
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime timestamp;

    private T data;

    public static <T> ApiResponseDto<T> success(int status, String message, T data) {
        return ApiResponseDto.<T>builder()
                .status(status)
                .message(message)
                .timestamp(ZonedDateTime.now())
                .data(data)
                .build();
    }

    public static <T> ApiResponseDto<T> error(int status, String message) {
        return ApiResponseDto.<T>builder()
                .status(status)
                .message(message)
                .timestamp(ZonedDateTime.now())
                .build();
    }
}