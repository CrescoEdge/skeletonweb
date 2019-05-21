package io.cresco.skeletonweb.controllers;

import io.cresco.library.plugin.PluginBuilder;
import io.cresco.library.utilities.CLogger;
import io.cresco.skeletonweb.Plugin;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/")
public class Example {
    private PluginBuilder plugin;
    private CLogger logger;

    public Example() {

        if(plugin == null) {
            if(Plugin.pluginBuilder != null) {
                plugin = Plugin.pluginBuilder;
                logger = plugin.getLogger(Example.class.getName(), CLogger.Level.Info);
            }
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response index() {
        try {

            String responseString = "Hello World!";

            return Response.ok(responseString).build();

        }  catch (Exception e) {
            e.printStackTrace();
            return Response.ok("Server error: " + e.getMessage()).build();
        }
    }



}
