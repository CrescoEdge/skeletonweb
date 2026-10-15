package io.cresco.skeletonweb.controllers;

import io.cresco.library.plugin.PluginBuilder;
import io.cresco.skeletonweb.Plugin;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** Example JAX-RS resource. Jersey creates one instance per request. */
@Path("/")
public class Example {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response index() {
        return Response.ok("Hello World!").build();
    }

    /** Identity of the plugin serving this request: {"region":..,"agent":..,"plugin":..}. */
    @GET
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON)
    public Response info() {
        PluginBuilder plugin = Plugin.pluginBuilder;
        if (plugin == null) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("{\"status\":\"starting\"}").build();
        }
        String json = "{\"region\":\"" + plugin.getRegion() + "\",\"agent\":\"" + plugin.getAgent()
                + "\",\"plugin\":\"" + plugin.getPluginID() + "\"}";
        return Response.ok(json).build();
    }
}
