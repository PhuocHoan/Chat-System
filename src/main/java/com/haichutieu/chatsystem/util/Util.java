package com.haichutieu.chatsystem.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Util {
    public static <T> String serializeObject(T data) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // Generic function to deserialize JSON to any type
    public static <T> T deserializeObject(String data, TypeReference<T> typeReference) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(data, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readLine(StringBuilder sb) {
        int index = sb.indexOf("\n");
        if (index != -1) {
            String line = sb.substring(0, index);
            sb.delete(0, index + 1); // Remove the line and the newline character
            return line;
        }
        return null;
    }
}
