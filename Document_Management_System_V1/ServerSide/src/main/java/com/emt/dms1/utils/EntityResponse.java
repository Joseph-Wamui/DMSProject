package com.emt.dms1.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EntityResponse<T> {
    private String message;
    private T entity;
    private List<T> entities; // Change to hold a list of documents
    private Integer statusCode;
    private HttpHeaders headers;
    private long contentLength;
    private Resource body;

    public void setStatus(int value) {
    }
}
