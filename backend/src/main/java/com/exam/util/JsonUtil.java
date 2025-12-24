package com.exam.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public static <T> T readValue(BufferedReader reader, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return mapper.readValue(sb.toString(), clazz);
    }
    
    @SuppressWarnings("unchecked")
    public static Map<String, Object> readValueAsMap(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return mapper.readValue(sb.toString(), new TypeReference<Map<String, Object>>() {});
    }
    
    public static <T> T readValue(String content, Class<T> clazz) throws IOException {
        return mapper.readValue(content, clazz);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T readValue(String content, TypeReference<T> typeRef) throws IOException {
        return mapper.readValue(content, typeRef);
    }
    
    public static String writeValueAsString(Object obj) throws IOException {
        return mapper.writeValueAsString(obj);
    }
}

