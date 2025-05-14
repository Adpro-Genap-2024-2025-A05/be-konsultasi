package id.ac.ui.cs.advprog.bekonsultasi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponseDto<T> {
    private int status;
    private String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Jakarta")
    private Date timestamp;

    private T data;

    public static <T> BaseResponseDto<T> success(T data) {
        return BaseResponseDto.<T>builder()
                .status(200)
                .message("Success")
                .timestamp(new Date())
                .data(data)
                .build();
    }

    public static <T> BaseResponseDto<T> success(T data, String message) {
        return BaseResponseDto.<T>builder()
                .status(200)
                .message(message)
                .timestamp(new Date())
                .data(data)
                .build();
    }

    public static <T> BaseResponseDto<T> created(T data) {
        return BaseResponseDto.<T>builder()
                .status(201)
                .message("Created successfully")
                .timestamp(new Date())
                .data(data)
                .build();
    }

    public static <T> BaseResponseDto<T> error(int statusCode, String message) {
        return BaseResponseDto.<T>builder()
                .status(statusCode)
                .message(message)
                .timestamp(new Date())
                .data(null)
                .build();
    }

    public static <T> BaseResponseDto<T> errorWithData(int statusCode, String message, T data) {
        return BaseResponseDto.<T>builder()
                .status(statusCode)
                .message(message)
                .timestamp(new Date())
                .data(data)
                .build();
    }
}