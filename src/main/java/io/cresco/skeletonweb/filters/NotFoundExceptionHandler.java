package io.cresco.skeletonweb.filters;


import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
public class NotFoundExceptionHandler implements ExceptionMapper<NotFoundException> {
    @Context
    HttpHeaders headers;

    public Response toResponse(NotFoundException ex) {
        try {
            String responseString = "Sorry, the page was not found";

            return Response.status(404).entity(responseString).build();
        }  catch (Exception e) {
            return Response.ok("IOException: " + e.getMessage()).build();
        }
    }
}