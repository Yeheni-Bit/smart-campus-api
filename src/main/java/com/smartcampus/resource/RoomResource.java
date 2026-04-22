package com.smartcampus.resource;

import com.smartcampus.model.Room;
import com.smartcampus.repository.DataStore;
import java.net.URI;
import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.DELETE;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    @GET
    public Collection<Room> getAllRooms() {
        return DataStore.rooms.values();
    }

    @POST
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Room ID is required")
                    .build();
        }

        if (DataStore.rooms.containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Room with this ID already exists")
                    .build();
        }

        if (room.getSensorIds() == null) {
            room.setSensorIds(new java.util.ArrayList<>());
        }

        DataStore.rooms.put(room.getId(), room);

        return Response.created(URI.create("/rooms/" + room.getId()))
                .entity(room)
                .build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Room not found")
                    .build();
        }

        return Response.ok(room).build();
    }
    
    @DELETE
@Path("/{roomId}")
public Response deleteRoom(@PathParam("roomId") String roomId) {
    Room room = DataStore.rooms.get(roomId);

    // If room doesn't exist
    if (room == null) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity("Room not found")
                .build();
    }

    // Rule: cannot delete if sensors exist
    if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
        return Response.status(Response.Status.CONFLICT)
                .entity("Cannot delete room with assigned sensors")
                .build();
    }

    DataStore.rooms.remove(roomId);

    return Response.noContent().build();
}
}