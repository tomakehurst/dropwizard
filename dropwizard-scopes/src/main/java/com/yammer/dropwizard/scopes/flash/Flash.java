package com.yammer.dropwizard.scopes.flash;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedHashMap;

import static com.google.common.collect.Maps.newLinkedHashMap;

public class Flash {

    public static final String FLASH_COOKIE_NAME = "DW_FLASH";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final LinkedHashMap<String, Object> attributes;

    private Flash() {
        attributes = newLinkedHashMap();
    }

    private Flash(LinkedHashMap<String, Object> attributes) {
        this.attributes = attributes;

    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) attributes.get(key);
    }

    public <T> Flash put(String key, T value) {
        attributes.put(key, value);
        return this;
    }

    public Flash remove(String key) {
        attributes.remove(key);
        return this;
    }

    public static Flash flashScope() {
        return new Flash();
    }

    public NewCookie build() {
        try {
            String unencodedJson = objectMapper.writeValueAsString(attributes);
            return new NewCookie(FLASH_COOKIE_NAME,
                                 URLEncoder.encode(unencodedJson, "utf-8"),
                                 "/",
                                 null,
                                 "",
                                 5,
                                 false);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize flash attributes", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Flash restoreFrom(Cookie cookie) {
        try {
            String decodedJson = URLDecoder.decode(cookie.getValue(), "utf-8");
            LinkedHashMap<String, Object> attributes = objectMapper.readValue(decodedJson, LinkedHashMap.class);
            return new Flash(attributes);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Bad flash cookie encoding", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error deserializing flash cookie value", e);
        }
    }
}
