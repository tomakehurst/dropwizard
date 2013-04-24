package com.yammer.dropwizard.scopes.flash;

import com.sun.jersey.spi.container.ResourceMethodDispatchAdapter;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;

import javax.ws.rs.ext.Provider;

@Provider
public class FlashScopeResourceMethodDispatchAdapter implements ResourceMethodDispatchAdapter {

    @Override
    public ResourceMethodDispatchProvider adapt(ResourceMethodDispatchProvider provider) {
        return new FlashScopeResourceMethodDispatchProvider(provider);
    }
}
