package com.mesosphere.dcos.cassandra.scheduler.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import com.mesosphere.dcos.cassandra.common.backup.RestoreContext;
import com.mesosphere.dcos.cassandra.scheduler.backup.RestoreManager;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1/restore")
public class RestoreResource {
    private final static String STATUS_STARTED = "started";
    private final static String MESSAGE_STARTED = "Started restore from snapshot";

    private final static String STATUS_ALREADY_RUNNING = "already_running";
    private final static String MESSAGE_ALREADY_RUNNING = "An existing restore is already in progress";

    private RestoreManager manager;

    @Inject
    public RestoreResource(RestoreManager manager) {
        this.manager = manager;
    }

    @PUT
    @Timed
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response start(StartRestoreRequest request) {
        if (manager.canStartRestore()) {
            final RestoreContext context = from(request);
            manager.startRestore(context);
            final StartRestoreResponse response = new StartRestoreResponse(STATUS_STARTED, MESSAGE_STARTED);
            return Response.ok(response).build();
        } else {
            // Send error back
            return Response.status(502).
                    entity(new StartRestoreResponse(STATUS_ALREADY_RUNNING, MESSAGE_ALREADY_RUNNING))
                    .build();
        }
    }

    public static RestoreContext from(StartRestoreRequest request) {
        final RestoreContext context =
                new RestoreContext();
        context.setName(request.getName());
        context.setExternalLocation(request.getExternalLocation());
        context.setS3AccessKey(request.getS3AccessKey());
        context.setS3SecretKey(request.getS3SecretKey());

        return context;
    }
}