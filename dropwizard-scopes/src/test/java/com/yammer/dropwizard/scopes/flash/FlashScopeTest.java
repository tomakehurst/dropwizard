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
import com.yammer.dropwizard.testing.junit.DropwizardServiceRule;
import org.hamcrest.CoreMatchers;
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
import java.net.URLDecoder;
import java.net.URLEncoder;

import static com.google.common.collect.Iterables.find;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class FlashScopeTest {

    @ClassRule
    public static DropwizardServiceRule<TestConfig> RULE =
            new DropwizardServiceRule<TestConfig>(FlashScopeTestService.class,
                                                     resourceFilePath("flash.yml"));

    static Client client = new Client();

    @Test
    public void setsFlashScopeContents() throws Exception {
        ClientResponse response = client
                .resource(fullUrl("/flash-test"))
                .post(ClientResponse.class);

        assertThat(response.getCookies(), hasCookieWithName(FlashScope.COOKIE_NAME));
        String decodedValue = URLDecoder.decode(flashCookieIn(response).getValue(), "utf-8");
        assertThat(decodedValue, containsString("It worked"));
    }

    @Test
    public void doesNotSetFlashCookieIfFlashOutIsEmpty() {
        ClientResponse response = client
                .resource(fullUrl("/flash-empty"))
                .post(ClientResponse.class);

        assertThat(response.getCookies(), not(hasCookieWithName(FlashScope.COOKIE_NAME)));
    }

    @Test
    public void retrievesFlashScopeContents() throws Exception {
        String message = client
                .resource(fullUrl("/flash-return"))
                .cookie(new NewCookie(FlashScope.COOKIE_NAME,
                        URLEncoder.encode("{\"actionMessage\":\"Flash aaahhh-ahhhhh\"}", "utf-8")))
                .get(String.class);

        assertThat(message, is("Flash aaahhh-ahhhhh"));
    }

    @Test
    public void immediatelyExpiresPreviousFlashCookie() throws Exception {
        ClientResponse response = client
                .resource(fullUrl("/flash-return"))
                .cookie(new NewCookie(FlashScope.COOKIE_NAME,
                    URLEncoder.encode("{\"actionMessage\":\"Should not see this\"}", "utf-8")))
                .get(ClientResponse.class);

        assertThat(response.getCookies(), hasCookieWithName(FlashScope.COOKIE_NAME));
        assertThat(flashCookieIn(response).getMaxAge(), is(0));
    }

    private NewCookie flashCookieIn(ClientResponse response) {
        return find(response.getCookies(), new Predicate<NewCookie>() {
            public boolean apply(NewCookie newCookie) {
                return newCookie.getName().equals(FlashScope.COOKIE_NAME);
            }
        });
    }

    private String fullUrl(String relativeUrl) {
        return "http://localhost:" + RULE.getLocalPort() + relativeUrl;
    }

    public static class FlashScopeTestService extends Service<TestConfig> {

        @Override
        public void initialize(Bootstrap<TestConfig> bootstrap) {
            bootstrap.addBundle(new FlashScopeBundle<TestConfig>());
        }

        @Override
        public void run(TestConfig configuration, Environment environment) throws Exception {
            environment.getJerseyEnvironment().addResource(new FlashScopeTestResource());
        }
    }

    @Path("/")
    public static class FlashScopeTestResource {

        @Path("/flash-test")
        @POST
        public Response doSomething(@FlashScope FlashOut flash) {
            flash.put("actionMessage", "It worked");
            return Response.ok()
                    .build();
        }

        @Path("/flash-empty")
        @POST
        public Response doSomethingWithNoFlashOutput(@FlashScope FlashOut flash) {
            return Response.ok()
                    .build();
        }

        @Path("/flash-return")
        @GET
        @Produces("text/plain")
        public String getResult(@FlashScope FlashIn flashIn) {
            return flashIn.get("actionMessage");
        }

    }

    public static class TestConfig extends Configuration {
        @JsonProperty
        private String dummy;
    }


    public static String resourceFilePath(String resourceClassPathLocation) {
        try {
            return new File(Resources.getResource(resourceClassPathLocation).toURI()).getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Matcher<Iterable<? super NewCookie>> hasCookieWithName(String name) {
        return CoreMatchers.hasItem(withName(name));
    }

    private static Matcher<NewCookie> withName(final String cookieName) {
        return new TypeSafeDiagnosingMatcher<NewCookie>() {
            @Override
            protected boolean matchesSafely(NewCookie newCookie, Description description) {
                if (!newCookie.getName().equals(cookieName)) {
                    description.appendText("cookie name is not " + cookieName);
                    return false;
                }

                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a cookie with name " + cookieName);
            }
        };
    }

}
