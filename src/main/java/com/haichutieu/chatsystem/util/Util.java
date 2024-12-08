package com.haichutieu.chatsystem.util;

import com.fasterxml.jackson.core.JsonProcessingException;
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

    public static <T> T deserializeObject(String data, Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(data, clazz);
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
