package com.sqli.support.jepicard.agenda.resources;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sqli.support.jepicard.agenda.dao.EventMongoDAO;
import com.sqli.support.jepicard.agenda.model.Event;

@Path("/agenda")
public class AgendaResource {

	private static final String JS_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	private static final String WS_DATE_PATTERN = "yyyy-MM-dd-HH-mm";
	private static final String CORS_CONFIG = "";
	private static final String WS_DATE_PATTERN_DAY = "yyyy-M-d";

	private Response makeCORS(ResponseBuilder responseBuilder,
			String returnMethod) {
		ResponseBuilder rb = responseBuilder.header(
				"Access-Control-Allow-Origin", "*").header(
				"Access-Control-Allow-Methods",
				"GET, POST, PUT, DELETE, OPTIONS").header("Cache-Control", "no-cache");

		if (!"".equals(returnMethod)) {
			rb.header("Access-Control-Allow-Headers", returnMethod);
		}

		return rb.build();
	}

	private Response makeCORS(ResponseBuilder responseBuilder) {
		return makeCORS(responseBuilder, CORS_CONFIG);
	}

	// Necessary for cors
	@OPTIONS
	@Path("/events/{owner}/{name}/{date}")
	public Response corsgetEvent(
			@HeaderParam("Access-Control-Request-Headers") String requestH) {
		return makeCORS(Response.ok(), requestH);
	}
	@GET
	@Path("/events/{owner}/{name}/{date}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEvent(@PathParam("owner") String owner,
			@PathParam("name") String name, @PathParam("date") String date) {
		EventMongoDAO mc = null;
		try {
			mc = EventMongoDAO.getInstance();

		} catch (UnknownHostException e) {
			e.printStackTrace();
			return makeCORS(Response.status(503));
		}
		Event event = null;
		try {
			event = mc.find(owner, name,
					new SimpleDateFormat(WS_DATE_PATTERN).parse(date));
		} catch (ParseException e) {
			try {
				event = mc.find(owner, name,
						new SimpleDateFormat(JS_DATE_FORMAT).parse(date));
			} catch (ParseException e1) {
				e1.printStackTrace();
				return makeCORS(Response.status(503));
			}
		}

		Gson gson = new GsonBuilder().setDateFormat(JS_DATE_FORMAT).create();
		return makeCORS(Response.ok().entity(gson.toJson(event)));

	}
	// Necessary for cors
	@OPTIONS
	@Path("/events/{owner}/{date}")
	public Response corsgetEventsByDay(
			@HeaderParam("Access-Control-Request-Headers") String requestH) {
		return makeCORS(Response.ok(), requestH);
	}
	@GET
	@Path("/events/{owner}/{date}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEventsByDay(@PathParam("owner") String owner,
			@PathParam("date") String date) {
		EventMongoDAO mc = null;
		try {
			mc = EventMongoDAO.getInstance();

		} catch (UnknownHostException e) {
			e.printStackTrace();
			return makeCORS(Response.status(503));
		}

		List<Event> events = null;
		try {
			events = mc.find(owner,
					new SimpleDateFormat(WS_DATE_PATTERN_DAY).parse(date));
		} catch (ParseException e) {
			try {
				events = mc.find(owner,
						new SimpleDateFormat(JS_DATE_FORMAT).parse(date));
			} catch (ParseException e1) {
				e1.printStackTrace();
				return makeCORS(Response.status(503));
			}
		}

		Gson gson = new GsonBuilder().setDateFormat(JS_DATE_FORMAT).create();
		return makeCORS(Response.ok().entity(gson.toJson(events)));
	}

	// Necessary for cors
	@OPTIONS
	@Path("/events/{owner}")
	public Response corsgetEvents(
			@HeaderParam("Access-Control-Request-Headers") String requestH) {
		return makeCORS(Response.ok(), requestH);
	}
	@GET
	@Path("/events/{owner}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEvents(@PathParam("owner") String owner) {
		EventMongoDAO mc = null;
		try {
			mc = EventMongoDAO.getInstance();

		} catch (UnknownHostException e) {
			e.printStackTrace();
			return makeCORS(Response.status(503));
		}

		List<Event> events = new ArrayList<Event>();
		events = mc.findAll(owner);
		Gson gson = new GsonBuilder().setDateFormat(JS_DATE_FORMAT).create();
		if (events.size() != 0) {
			return makeCORS(Response.ok().entity(gson.toJson(events)));
		} else {
			return makeCORS(Response.noContent());
		}
	}

	// Necessary for cors
	@OPTIONS
	@Path("/modifyEvent")
	public Response corsModifyEvent(
			@HeaderParam("Access-Control-Request-Headers") String requestH) {
		return makeCORS(Response.ok(), requestH);
	}

	// Must provide the id to first check existence
	@PUT
	@Path("/modifyEvent")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyEvent(String jsonObject) {
		Gson gson = new GsonBuilder().setDateFormat(WS_DATE_PATTERN).create();

		Event event = null;
		try {
			event = gson.fromJson(jsonObject, Event.class);
		} catch (JsonSyntaxException e1) {
			try {
				gson = new GsonBuilder().setDateFormat(JS_DATE_FORMAT).create();
				event = gson.fromJson(jsonObject, Event.class);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				return makeCORS(Response.status(400));
			}
		}

		EventMongoDAO mc = null;
		try {
			mc = EventMongoDAO.getInstance();

		} catch (UnknownHostException e) {
			e.printStackTrace();
			return makeCORS(Response.status(503));
		}

		if (event.getId() != null ) {
			if(mc.exists(event.getId())){
				mc.update(event);
				String toReturn = gson.toJson(event);
				return makeCORS(Response.ok().entity(toReturn));
			} else {
				return makeCORS(Response.status(204));
			}
		} else {
			return makeCORS(Response.status(400));
		}
	}

	// Necessary for cors
	@OPTIONS
	@Path("/createEvent")
	public Response corsCreateEvent(
			@HeaderParam("Access-Control-Request-Headers") String requestH) {
		return makeCORS(Response.ok(), requestH);
	}


	// Should provide no id. if an id is provided, the request is wrong only if
	// there is already an object in db with that id which would correspond to a
	// wrong post
	// (in that case, the request should either be a delete or a put)
	@POST
	@Path("/createEvent")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createEvent(String jsonObject) {
		Gson gson = new GsonBuilder().setDateFormat(WS_DATE_PATTERN).create();

		Event event = null;
		try {
			event = gson.fromJson(jsonObject, Event.class);
		} catch (JsonSyntaxException e1) {
			try{
				gson = new GsonBuilder().setDateFormat(JS_DATE_FORMAT).create();
				event = gson.fromJson(jsonObject, Event.class);
			}catch (JsonSyntaxException e){
				e.printStackTrace();
				return makeCORS(Response.status(400));
			}
		}

		EventMongoDAO mc = null;
		try {
			mc = EventMongoDAO.getInstance();

		} catch (UnknownHostException e) {
			e.printStackTrace();
			return makeCORS(Response.status(503));
		}
		if (event.getId() != null && mc.exists(event.getId())) {
			return makeCORS(Response.status(400));
		} else {
			Event ev = mc.create(event);
			String toReturn = gson.toJson(ev);
			return makeCORS(Response.ok().entity(toReturn));
		}
	}
	
	// Necessary for cors
	@OPTIONS
	@Path("/deleteEvent")
	public Response corsDeleteEvent(
			@HeaderParam("Access-Control-Request-Headers") String requestH) {
		return makeCORS(Response.ok(), requestH);
	}

	@DELETE
	@Path("/deleteEvent")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteEvent(String jsonObject) {
		Gson gson = new GsonBuilder().setDateFormat(WS_DATE_PATTERN).create();

		Event event = null;
		try {
			event = gson.fromJson(jsonObject, Event.class);
		} catch (JsonSyntaxException e1) {
			try {
				gson = new GsonBuilder().setDateFormat(JS_DATE_FORMAT).create();
				event = gson.fromJson(jsonObject, Event.class);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				return makeCORS(Response.status(400));
			}
		}

		EventMongoDAO mc = null;
		try {
			mc = EventMongoDAO.getInstance();

		} catch (UnknownHostException e) {
			e.printStackTrace();
			return makeCORS(Response.status(503));
		}

		if (event.getId() != null) {
			if(mc.exists(event.getId())){
				mc.delete(event);
				return makeCORS(Response.ok());
			}
			else {
				return makeCORS(Response.status(204));
			}
		} else {
			return makeCORS(Response.status(400));
		}
	}
	
	// Necessary for cors
		@OPTIONS
		@Path("/deleteEvent/{id}")
		public Response corsDeleteEventWithId(
				@HeaderParam("Access-Control-Request-Headers") String requestH) {
			return makeCORS(Response.ok(), requestH);
		}

		@DELETE
		@Path("/deleteEvent/{id}")
		public Response deleteEventWithId(@PathParam("id") String id) {


			EventMongoDAO mc = null;
			try {
				mc = EventMongoDAO.getInstance();

			} catch (UnknownHostException e) {
				e.printStackTrace();
				return makeCORS(Response.status(503));
			}

			if (id != "") {
				if(mc.exists(id)){
					mc.delete(id);
					return makeCORS(Response.ok());
				}
				else {
					return makeCORS(Response.status(204));
				}
			} else {
				return makeCORS(Response.status(400));
			}
		}
}
