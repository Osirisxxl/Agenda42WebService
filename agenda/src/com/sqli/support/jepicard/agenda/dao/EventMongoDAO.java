package com.sqli.support.jepicard.agenda.dao;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import org.apache.commons.lang3.text.WordUtils;
import org.bson.types.ObjectId;


import com.sqli.support.jepicard.agenda.model.Answer;
import com.sqli.support.jepicard.agenda.model.Event;
import com.sqli.support.jepicard.agenda.model.PersonInvited;

public class EventMongoDAO {
	// Begin date columns name for DB
	private static final String MINUTE = "minute";
	private static final String HOUR = "hour";
	private static final String DAY = "day";
	private static final String MONTH = "month";
	private static final String YEAR = "year";
	// Other columns name for Event object in DB
	private static final String NAME = "name";
	private static final String OWNER_ID = "owner_id";
	private static final String DURATION = "duration";
	private static final String ID = "_id";
	private static final String DESCRIPTION = "description";
	private static final String PLACE = "place";
	private static final String INVITED = "invited";
	private static final String ALERT = "alert";

	private static final String DB_DATE_PATTERN = "yyyy-MM-dd-hh:mm";
	private static final String DB_NAME = "agenda";
	private static final String COLLECTION_NAME = "agenda";

	private static volatile EventMongoDAO instance = null;
	private MongoClient dbClient;
	private DBCollection dbCollection;
	private DB db;

	// Date formatter utilities
	private SimpleDateFormat day;
	private SimpleDateFormat month;
	private SimpleDateFormat year;
	private SimpleDateFormat hour;
	private SimpleDateFormat minute;

	private EventMongoDAO() throws UnknownHostException {
		super();
		dbClient = new MongoClient();
		db = dbClient.getDB(DB_NAME);
		dbCollection = db.getCollection(COLLECTION_NAME);

		minute = new SimpleDateFormat("m");
		hour = new SimpleDateFormat("HH");
		day = new SimpleDateFormat("d");
		month = new SimpleDateFormat("M");
		year = new SimpleDateFormat("yyyy");

	}

	public static EventMongoDAO getInstance() throws UnknownHostException {
		if (EventMongoDAO.instance == null) {
			synchronized (EventMongoDAO.class) {
				if (EventMongoDAO.instance == null) {
					EventMongoDAO.instance = new EventMongoDAO();
				}
			}
		}
		return EventMongoDAO.instance;
	}

	public boolean exists(String eventID) {
		DBObject finder = createDBEventFinderFromID(eventID);
		DBObject result = dbCollection.findOne(finder);
		if (result == null)
			return false;
		return true;
	}

	public Event find(String ownerID, String name, Date beginDate) {
		DBObject finder = createDBEventFinderFromNameAndDate(ownerID, name,
				beginDate);
		return findOne(finder);
	}

	public Event find(String id) {
		DBObject finder = createDBEventFinderFromId(id);
		return findOne(finder);
	}

	public List<Event> find(String ownerID, Date date) {
		DBObject finder = createDBEventFinderByMonth(ownerID, date);
		return findMany(finder);
	}

	public List<Event> findAll(String ownerID) {
		BasicDBObject criteria = new BasicDBObject();
		criteria.append(OWNER_ID, ownerID);
		return findMany(criteria);
	}

	private List<Event> findMany(DBObject criteria) {
		return findMany(dbCollection.find(criteria));
	}

	private List<Event> findMany(DBCursor cursor) {
		ArrayList<Event> events = new ArrayList<Event>();
		while (cursor.hasNext()) {
			try {
				events.add(createEventFromDBObject(cursor.next()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		cursor.close();
		return events;
	}

	private Event findOne(DBObject criteria) {
		DBObject result = dbCollection.findOne(criteria);
		if (result == null)
			return null;
		try {
			return createEventFromDBObject(result);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void update(Event event) {
		//Necessary to add the event to actually update the correct event
		BasicDBObject mongoEvent = new BasicDBObject();
		mongoEvent.append(ID, new ObjectId(event.getId()));
		if (exists(event.getId())) {
			dbCollection.update(mongoEvent, createDBObjet(event));
		}
	}

	public Event create(Event event) {
		BasicDBObject mongoEvent = (BasicDBObject) createDBObjet(event);
		mongoEvent.append(ID, new ObjectId());
		dbCollection.insert(mongoEvent);
		event.setId(((ObjectId)mongoEvent.get(ID)).toString());
		return event;
	}

	public void delete(Event event) {
		BasicDBObject mongoEvent = new BasicDBObject();
		mongoEvent.append(ID, new ObjectId(event.getId()));
		if (exists(event.getId())) {
			dbCollection.remove(mongoEvent);
		}
	}
	public void delete(String id) {
		BasicDBObject mongoEvent = new BasicDBObject();
		mongoEvent.append(ID, new ObjectId(id));
		if (exists(id)) {
			dbCollection.remove(mongoEvent);
		}
	}


	/**
	 * 
	 * @return the number of persons in the db
	 */
	public long getCount() {
		return dbCollection.getCount();
	}

	/**
	 * drop the content of the database
	 */
	public void dropDatabase() {
		db.dropDatabase();
	}

	private Event createEventFromDBObject(DBObject obj) throws ParseException {
		Event event = new Event();
		event.setId(obj.get(ID).toString());
		event.setOwnerID(obj.get(OWNER_ID).toString());
		event.setName(obj.get(NAME).toString());
		event.setDescription(WordUtils.capitalizeFully(obj.get(DESCRIPTION)
				.toString(), '.'));
		event.setPlace(obj.get(PLACE).toString());
		event.setDuration((int) Math.floor(Double.parseDouble(obj.get(DURATION)
				.toString())));
		//alert could be null if value is not given when event is created because of serialization not initializing it
		if(obj.get(ALERT) == null){
			event.setAlert(false);
		}else{
			event.setAlert(Boolean.parseBoolean(obj.get(ALERT).toString()));	
		}
		
		ArrayList<PersonInvited> invitedPeople = new ArrayList<PersonInvited>();
		try{
			String rawResult = obj.get(INVITED).toString();
			String[] invites = rawResult.split(";");
			for (String invite : invites) {
				String[] values = invite.split(",");
				PersonInvited p = new PersonInvited();
				p.setFirstName(values[0]);
				p.setLastName(values[1]);
				p.setAnswer(Answer.valueOf("" + values[2]));
				if (!invitedPeople.contains(p)) {
					invitedPeople.add(p);
				}
			};
		}catch(Exception e){};
		event.setInvited(invitedPeople);
		

		StringBuilder builder = new StringBuilder();
		builder.append((int) Math.floor(Double.parseDouble(obj.get(YEAR)
				.toString())));
		builder.append("-");
		builder.append((int) Math.floor(Double.parseDouble(obj.get(MONTH)
				.toString())));
		builder.append("-");
		builder.append((int) Math.floor(Double.parseDouble(obj.get(DAY)
				.toString())));
		builder.append("-");
		builder.append((int) Math.floor(Double.parseDouble(obj.get(HOUR)
				.toString())));
		builder.append(":");
		builder.append((int) Math.floor(Double.parseDouble(obj.get(MINUTE)
				.toString())));
		event.setBeginDate(new SimpleDateFormat(DB_DATE_PATTERN).parse(builder
				.toString()));
		return event;
	}

	private DBObject createDBObjet(Event event) {
		BasicDBObject obj = new BasicDBObject();
		obj.append(NAME, event.getName());
		obj.append(OWNER_ID, event.getOwnerID());
		obj.append(YEAR, Integer.parseInt(year.format(event.getBeginDate())));
		obj.append(MONTH, Integer.parseInt(month.format(event.getBeginDate())));
		obj.append(DAY, Integer.parseInt(day.format(event.getBeginDate())));
		obj.append(HOUR, Integer.parseInt(hour.format(event.getBeginDate())));
		obj.append(MINUTE, Integer.parseInt(minute.format(event.getBeginDate())));
		obj.append(DESCRIPTION, event.getDescription());
		obj.append(DURATION, event.getDuration());
		obj.append(PLACE, event.getPlace());
		obj.append(ALERT, event.getAlert());

		// handling enum serialization
		try{
			String toStore = "";
			for (int i = 0; i < event.getInvited().size(); i++) {
				if (i > 0 && i < event.getInvited().size()) {
					toStore += ";";
				}
				toStore += event.getInvited().get(i).getFirstName() + ","
						+ event.getInvited().get(i).getLastName() + ","
						+ event.getInvited().get(i).getAnswer();
			}
			obj.append(INVITED, toStore);
		}catch(NullPointerException e){
			
		}

		return obj;
	}

	private DBObject createDBEventFinder(Event event) {
		return createDBEventFinderFromNameAndDate(event.getOwnerID(),
				event.getName(), event.getBeginDate());
	}

	private DBObject createDBEventFinderFromId(String id) {
		BasicDBObject obj = new BasicDBObject();
		obj.append(ID, new ObjectId(id));
		return obj;
	}

	private DBObject createDBEventFinderFromNameAndDate(String ownerID,
			String name, Date beginDate) {
		BasicDBObject obj = new BasicDBObject();
		if (ownerID != null && ownerID.length() > 0)
			obj.append(OWNER_ID, ownerID);
		if (name != null && name.length() > 0)
			obj.append(NAME, name);
		if (beginDate != null) {
			if (year.format(beginDate) != null
					&& year.format(beginDate).length() > 0) {
				obj.append(YEAR, Integer.parseInt(year.format(beginDate)));
			}
			if (month.format(beginDate) != null
					&& month.format(beginDate).length() > 0) {
				obj.append(MONTH, Integer.parseInt(month.format(beginDate)));
			}
			if (day.format(beginDate) != null
					&& day.format(beginDate).length() > 0) {
				obj.append(DAY, Integer.parseInt(day.format(beginDate)));
			}
			if (hour.format(beginDate) != null
					&& hour.format(beginDate).length() > 0) {
				obj.append(HOUR, Integer.parseInt(hour.format(beginDate)));
			}
			if (minute.format(beginDate) != null
					&& minute.format(beginDate).length() > 0) {
				obj.append(MINUTE, Integer.parseInt(minute.format(beginDate)));
			}
		}

		return obj;
	}

	private DBObject createDBEventFinderByMonth(String ownerID, Date date) {
		BasicDBObject obj = new BasicDBObject();
		if (ownerID != null && ownerID.length() > 0)
			obj.append(OWNER_ID, ownerID);
		if (date != null) {
			if (year.format(date) != null
					&& year.format(date).length() > 0) {
				obj.append(YEAR, Integer.parseInt(year.format(date)));
			}
			if (month.format(date) != null
					&& month.format(date).length() > 0) {
				obj.append(MONTH, Integer.parseInt(month.format(date)));
			}
			if (day.format(date) != null
					&& day.format(date).length() > 0) {
				obj.append(DAY, Integer.parseInt(day.format(date)));
			}
		}
		return obj;
	}

	private DBObject createDBEventFinderFromID(String eventID) {
		BasicDBObject obj = new BasicDBObject();
		if (eventID != null && eventID.length() > 0)
			obj.append(ID, new ObjectId(eventID));
		return obj;
	}


}