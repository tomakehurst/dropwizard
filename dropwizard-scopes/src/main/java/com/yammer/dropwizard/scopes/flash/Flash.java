package com.yammer.dropwizard.scopes.flash;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;

import static com.google.common.collect.Maps.newLinkedHashMap;

public abstract class Flash {

    protected static final ObjectMapper objectMapper = new ObjectMapper();
    protected final LinkedHashMap<String, Object> attributes;

    protected Flash(LinkedHashMap<String, Object> attributes) {
        this.attributes = attributes;
    }
}
