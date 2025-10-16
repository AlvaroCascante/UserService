package com.quetoquenana.personservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response wrapper for REST endpoints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {
    private static final String DEFAULT_SUCCESS_MESSAGE = "Success";
    private static final Integer DEFAULT_ERROR_CODE = 0;

    @JsonView(ApiBaseResponseView.Always.class)
    private String message;

    @JsonView(ApiBaseResponseView.Always.class)
    private Integer errorCode;

    @JsonView(ApiBaseResponseView.Always.class)
    private Object data;

    public ApiResponse(String message, Integer errorCode) {
        this.message = message;
        this.errorCode = errorCode;
        this.data = null;
    }


    public ApiResponse(Object data) {
        this.message = DEFAULT_SUCCESS_MESSAGE;
        this.errorCode = DEFAULT_ERROR_CODE;
        this.data = data;
    }
}

