package com.yammer.dropwizard.scopes.flash;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import javax.ws.rs.core.Cookie;

import static com.yammer.dropwizard.jersey.flashscope.Flash.flashScope;

public class FlashScopeInjectableProvider implements InjectableProvider<FlashScope, Parameter> {

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable getInjectable(ComponentContext ic, FlashScope flashScope, Parameter parameter) {
        return new AbstractHttpContextInjectable<Flash>() {
            @Override
            public Flash getValue(HttpContext context) {
                Cookie cookie = context.getRequest().getCookies().get(Flash.FLASH_COOKIE_NAME);
                if (cookie != null) {
                    return Flash.restoreFrom(cookie);
                }

                return flashScope();
            }
        };
    }
}
