package com.yammer.dropwizard.scopes.flash;

import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

public class FlashScopeBundle<T> implements ConfiguredBundle<T> {

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        environment.getJerseyEnvironment().addProvider(FlashScopeResourceMethodDispatchAdapter.class);
        environment.getJerseyEnvironment().addProvider(FlashScopeInjectableProvider.class);
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        // nothing
    }
}
