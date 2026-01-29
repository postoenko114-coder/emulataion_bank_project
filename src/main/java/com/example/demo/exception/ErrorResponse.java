package com.example.demo.exception;

import java.time.LocalDateTime;

public class ErrorResponse {
    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
    private String path;

    public ErrorResponse(LocalDateTime timestamp, Integer status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public ErrorResponse(){}

    public String getError() {return error;}

    public void setError(String error) {this.error = error;}

    public String getMessage() {return message;}

    public void setMessage(String message) {this.message = message;}

    public String getPath() {return path;}

    public void setPath(String path) {this.path = path;}

    public Integer getStatus() {return status;}

    public void setStatus(Integer status) {this.status = status;}

    public LocalDateTime getTimestamp() {return timestamp;}

    public void setTimestamp(LocalDateTime timestamp) {this.timestamp = timestamp;}
}
