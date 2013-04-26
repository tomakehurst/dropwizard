package com.yammer.dropwizard.scopes.flash;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;

import javax.ws.rs.core.NewCookie;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class FlashOut extends Flash {

    private static final int DEFAULT_MAX_AGE = 5;

    public FlashOut() {
        super(Maps.<String, Object>newLinkedHashMap());
    }

    public NewCookie build() {
        try {
            String unencodedJson = objectMapper.writeValueAsString(attributes);
            return newCookie(unencodedJson, DEFAULT_MAX_AGE);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize flash attributes", e);
        }
    }

    public static NewCookie expireImmediately() {
        return newCookie("{}", 0);
    }

    private static NewCookie newCookie(String content, int maxAge) {
        try {
            return new NewCookie(FlashScope.COOKIE_NAME,
                    URLEncoder.encode(content, "utf-8"),
                    "/",
                    null,
                    "",
                    maxAge,
                    false);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
