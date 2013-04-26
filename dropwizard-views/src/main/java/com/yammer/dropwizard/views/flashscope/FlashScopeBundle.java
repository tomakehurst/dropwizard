package com.yammer.dropwizard.views.flashscope;

import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

public class FlashScopeBundle<T> implements ConfiguredBundle<T> {

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        environment.getJerseyEnvironment().addProvider(new FlashScopeResourceMethodDispatchAdapter(new FlashScopeConfig()));
        environment.getJerseyEnvironment().addProvider(new FlashScopeInjectableProvider());
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        // nothing
    }
}
