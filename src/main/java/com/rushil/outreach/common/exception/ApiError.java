package com.rushil.outreach.common.exception;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    /** Populated for validation errors — one entry per invalid field. */
    private List<String> details;
}
