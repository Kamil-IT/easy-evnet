package com.example.easyevnet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TypeUtil {

    public static boolean isCommonJavaType(Object ins) {
        return ins instanceof String ||
                ins instanceof Integer ||
                ins instanceof Long ||
                ins instanceof Float ||
                ins instanceof Double;
    }

    public static ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }
}
