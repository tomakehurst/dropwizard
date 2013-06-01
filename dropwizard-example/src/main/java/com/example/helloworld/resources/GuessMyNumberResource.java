package com.example.helloworld.resources;

import com.codahale.dropwizard.views.flashscope.Flash;
import com.codahale.dropwizard.views.flashscope.FlashScope;
import com.example.helloworld.views.GuessMyNumberView;
import com.google.common.base.Optional;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.HttpHeaders.LOCATION;

@Path("/guess")
public class GuessMyNumberResource {

    @Path("form")
    @GET
    public GuessMyNumberView showGuessForm(@FlashScope Flash flash) {
        Optional<String> message = flash.get(String.class);
        return new GuessMyNumberView(message.or(""));
    }

    @Path("number")
    @POST
    public Response processGuess(@FlashScope Flash flash, @FormParam("number") String number) {
        if (number.equals("4")) {
            flash.set("Correct!");
        } else {
            flash.set("Wrong!");
        }

        return Response.status(302).header(LOCATION, "/guess/form").build();
    }

}
