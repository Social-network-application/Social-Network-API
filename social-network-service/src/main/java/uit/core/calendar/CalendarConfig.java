package uit.core.calendar;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;
import org.springframework.boot.CommandLineRunner;
import uit.core.dto.request.EventRequest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CalendarConfig implements CommandLineRunner {
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/client_secrets.json";
    private static Calendar service;

    @Override
    public void run(String... args) throws Exception {

    }

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    /**
     * Authorizes the installed application to access user's protected data.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = CalendarConfig.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(4200).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    private static GoogleCredential getGoogleCredential() throws IOException {
        InputStream in = CalendarConfig.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleCredential credential = GoogleCredential.fromStream(in)
                .createScoped(SCOPES);
        return credential;
    }

    public static Event createEvent(EventRequest eventRequest) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getGoogleCredential())
                .setApplicationName(APPLICATION_NAME)
                .build();

        Event event = new Event().setSummary(eventRequest.getSummary())
                .setLocation(eventRequest.getLocation())
                .setDescription(eventRequest.getDescription());

        DateTime startDateTime = new DateTime(eventRequest.getStartDate()); //2020-12-01T09:00:00+07:00"
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Asia/Ho_Chi_Minh");
        event.setStart(start);

        DateTime endDateTime = new DateTime(eventRequest.getEndDate());
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Asia/Ho_Chi_Minh");
        event.setEnd(end);

        List<EventAttendee> attendees = new ArrayList<>();
        eventRequest.getEmails().stream().forEach(email -> {
            attendees.add(new EventAttendee().setEmail(email));
        });


        event.setAttendees(attendees);

        event.setConferenceData(buildConferenceData());

        String calendarId = "primary";
        event = service.events().insert(calendarId, event).setSendNotifications(true).setConferenceDataVersion(1)
                .execute();
        System.out.printf("Event created: %s\n", event.getHtmlLink());
        return event;
    }

    private static ConferenceData buildConferenceData() {
        ConferenceSolutionKey conferenceSKey = new ConferenceSolutionKey();
        conferenceSKey.setType("hangoutsMeet"); // Non-G suite user
        CreateConferenceRequest createConferenceReq = new CreateConferenceRequest();
        createConferenceReq.setRequestId("3whatisup3"); // ID generated by you
        createConferenceReq.setConferenceSolutionKey(conferenceSKey);

        ConferenceData conferenceData = new ConferenceData();
        conferenceData.setCreateRequest(createConferenceReq);
        return conferenceData;
    }

    private static void getEvent(Calendar service) throws IOException {
        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        if (items.isEmpty()) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                System.out.printf("%s (%s)\n", event.getSummary(), start);
            }
        }
    }


}
