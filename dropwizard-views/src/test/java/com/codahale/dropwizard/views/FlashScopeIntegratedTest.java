package com.codahale.dropwizard.views;

import com.codahale.dropwizard.Application;
import com.codahale.dropwizard.Configuration;
import com.codahale.dropwizard.client.JerseyClientBuilder;
import com.codahale.dropwizard.client.JerseyClientConfiguration;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.setup.Environment;
import com.codahale.dropwizard.testing.junit.DropwizardAppRule;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.representation.Form;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URI;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FlashScopeIntegratedTest {

    @ClassRule
    public static DropwizardAppRule<TestConfig> RULE =
            new DropwizardAppRule<TestConfig>(FlashScopeTestApp.class,
                                                     resourceFilePath("flash.yml"));

    Client client;

    @Before
    public void init() {
        client = new JerseyClientBuilder(new MetricRegistry())
                .using(RULE.getConfiguration().getJerseyClient())
                .using(newSingleThreadExecutor(), new ObjectMapper())
                .build("test-client");
    }

    @Test
    public void endToEndTest() {
        Form form = new Form();
        form.add("message", "Show this in flash");

        String returnedMessage = client.resource(fullUrl("/action")).post(String.class, form);

        assertThat(returnedMessage, is("Show this in flash"));
    }

    @Test
    public void cookieMaxAgeOverridden() {
        ClientResponse response = client.resource(fullUrl("/action-no-redirect")).post(ClientResponse.class);

        assertThat(TestUtils.findCookie(response, FlashScope.COOKIE_NAME).getMaxAge(), is(20));
    }

    private String fullUrl(String relativeUrl) {
        return "http://localhost:" + RULE.getLocalPort() + relativeUrl;
    }

    public static class FlashScopeTestApp extends Application<TestConfig> {

        @Override
        public void initialize(Bootstrap<TestConfig> bootstrap) {
            bootstrap.addBundle(new FlashScopeBundle<TestConfig>() {
                @Override
                protected FlashScopeConfig getFlashScopeConfig(TestConfig configuration) {
                    return configuration.getFlashScope();
                }
            });
        }

        @Override
        public void run(TestConfig configuration, Environment environment) throws Exception {
            environment.jersey().addResource(new PostRedirectGetResource());
        }
    }

    @Path("/")
    public static class PostRedirectGetResource {

        @Path("action")
        @POST
        public Response doSomething(@FlashScope FlashOut flash, @FormParam("message") String message) {
            flash.put("message", message);
            return Response.seeOther(URI.create("/result")).build();
        }

        @Path("result")
        @GET
        @Produces("text/plain")
        public String getResult(@FlashScope FlashIn flash) {
            return flash.get("message");
        }

        @Path("action-no-redirect")
        @POST
        public Response putSomethingInTheFlash(@FlashScope FlashOut flash) {
            flash.put("something", "anything");
            return Response.ok().build();
        }

    }

    public static class TestConfig extends Configuration {

        @JsonProperty
        private FlashScopeConfig flashScope;

        @JsonProperty
        private JerseyClientConfiguration jerseyClient;

        public FlashScopeConfig getFlashScope() {
            return flashScope;
        }

        public JerseyClientConfiguration getJerseyClient() {
            return jerseyClient;
        }
    }


    public static String resourceFilePath(String resourceClassPathLocation) {
        try {
            return new File(Resources.getResource(resourceClassPathLocation).toURI()).getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
