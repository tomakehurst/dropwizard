package com.yammer.dropwizard.scopes.flash;

import com.google.common.collect.Maps;

import javax.ws.rs.core.Cookie;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;

public class FlashIn extends Flash {

    FlashIn(LinkedHashMap<String, Object> attributes) {
        super(attributes);
    }

    public FlashIn() {
        super(Maps.<String, Object> newLinkedHashMap());
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) attributes.get(key);
    }

    @SuppressWarnings("unchecked")
    public static FlashIn restoreFrom(Cookie cookie) {
        try {
            String decodedJson = URLDecoder.decode(cookie.getValue(), "utf-8");
            LinkedHashMap<String, Object> attributes = objectMapper.readValue(decodedJson, LinkedHashMap.class);
            return new FlashIn(attributes);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Bad flash cookie encoding", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error deserializing flash cookie value", e);
        }
    }
}
