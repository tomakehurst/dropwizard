package com.yammer.dropwizard.scopes.flash;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Predicate;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jersey.flashscope.Flash;
import com.yammer.dropwizard.jersey.flashscope.FlashScope;
import com.yammer.dropwizard.jersey.flashscope.FlashScopeInjectableProvider;
import com.yammer.dropwizard.testing.junit.DropwizardServiceRule;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URLEncoder;
import java.util.List;

import static com.google.common.collect.Iterables.find;
import static com.yammer.dropwizard.jersey.flashscope.Flash.FLASH_COOKIE_NAME;
import static com.yammer.dropwizard.jersey.flashscope.Flash.flashScope;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class FlashScopeTest {

    @ClassRule
    public static DropwizardServiceRule<FlashScopeTestConfig> RULE =
            new DropwizardServiceRule<FlashScopeTestConfig>(FlashScopeTestService.class,
                                                     resourceFilePath("flash.yml"));

    static Client client = new Client();

    @Test
    public void setsFlashScopeContents() {
        ClientResponse response = client
                .resource(fullUrl("/flash-test"))
                .post(ClientResponse.class);

        assertThat(cookie(response.getCookies(), FLASH_COOKIE_NAME).getName(), is(not("not_found")));
        assertThat(response.getCookies(), hasItem(withName(FLASH_COOKIE_NAME)));
    }

    private Matcher<NewCookie> withName(final String cookieName) {
        return new TypeSafeDiagnosingMatcher<NewCookie>() {
            @Override
            protected boolean matchesSafely(NewCookie newCookie, Description description) {
                boolean matched = newCookie.getName().equals(cookieName);

                if (!matched) {
                    description.appendText("cookie name is not " + cookieName);
                }

                return matched;
            }

            @Override
            public void describeTo(Description description) {
            }
        };
    }

    @Test
    public void retrievesFlashScopeContents() throws Exception {
        String message = client
                .resource(fullUrl("/flash-return"))
                .cookie(new NewCookie(FLASH_COOKIE_NAME,
                        URLEncoder.encode("{\"actionMessage\":\"Flash aaahhh-ahhhhh\"}", "utf-8")))
                .get(String.class);

        assertThat(message, is("Flash aaahhh-ahhhhh"));
    }



    static NewCookie cookie(List<NewCookie> cookies, final String name) {
        return find(cookies, new Predicate<NewCookie>() {
            public boolean apply(NewCookie newCookie) {
                return newCookie.getName().equals(name);
            }
        },
        new NewCookie("not_found", "not found"));
    }

    private String fullUrl(String relativeUrl) {
        return "http://localhost:" + RULE.getLocalPort() + relativeUrl;
    }

    public static class FlashScopeTestService extends Service<FlashScopeTestConfig> {

        @Override
        public void initialize(Bootstrap<FlashScopeTestConfig> bootstrap) {
        }

        @Override
        public void run(FlashScopeTestConfig configuration, Environment environment) throws Exception {
            environment.getJerseyEnvironment().addResource(new FlashScopeTestResource());
            environment.getJerseyEnvironment().addProvider(FlashScopeInjectableProvider.class);
        }
    }

    @Path("/")
    public static class FlashScopeTestResource {

        @Path("/flash-test")
        @POST
        public Response doSomething() {
            return Response.ok()
                    .cookie(flashScope().put("actionMessage", "It worked").build())
                    .build();
        }

        @Path("/flash-return")
        @GET
        @Produces("text/plain")
        public String getResult(@FlashScope Flash flash) {
            return flash.get("actionMessage");
        }

    }

    public static class FlashScopeTestConfig extends Configuration {
        @JsonProperty
        private String saying;
    }


    public static String resourceFilePath(String resourceClassPathLocation) {
        try {
            return new File(Resources.getResource(resourceClassPathLocation).toURI()).getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
