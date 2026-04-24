# Smart Campus API

## Project Structure
smart-campus-api/
│
├── src/
│ └── main/
│ └── java/
│ └── com.smartcampus/
│ ├── model/
│ │ ├── Room.java
│ │ ├── Sensor.java
│ │ └── SensorReading.java
│ │
│ ├── resource/
│ │ ├── RoomResource.java
│ │ ├── SensorResource.java
│ │ └── SensorReadingResource.java
│ │
│ ├── exception/
│ │ ├── RoomNotEmptyException.java
│ │ ├── LinkedResourceNotFoundException.java
│ │ └── SensorUnavailableException.java
│ │
│ ├── mapper/
│ │ ├── RoomNotEmptyExceptionMapper.java
│ │ ├── LinkedResourceNotFoundExceptionMapper.java
│ │ ├── SensorUnavailableExceptionMapper.java
│ │ └── GlobalExceptionMapper.java
│ │
│ ├── filter/
│ │ └── LoggingFilter.java
│ │
│ ├── repository/
│ │ └── DataStore.java
│ │
│ └── Main.java
│
├── pom.xml
└── README.md


---

## API Design Overview

The Smart Campus API is a RESTful web service built using Java and JAX-RS. It manages rooms, sensors, and sensor readings for a campus environment.

The API is organised around these main resources:

- Rooms: manage campus rooms and their assigned sensors  
- Sensors: register sensors and link them to rooms  
- Sensor Readings: store historical readings for each sensor  

The API uses a versioned base path:


http://localhost:8081/api/v1


The service uses in-memory data structures such as maps and lists instead of a database.

---

## How to Build and Launch the Server

### 1. Clone the GitHub repository

```bash
git clone <your-github-repository-url>
2. Open the project in NetBeans

Open NetBeans and select:

File → Open Project

Then choose the smart-campus-api project folder.

3. Build the project

In NetBeans:

Right click project → Clean and Build
4. Run the server

In NetBeans:

Right click project → Run
5. Confirm the server is running

Open this URL in a browser:

http://localhost:8081/api/v1/

You should see the API discovery response.

Sample cURL Commands
1. Get API Discovery Information
curl -X GET http://localhost:8081/api/v1/
2. Create a Room
curl -X POST http://localhost:8081/api/v1/rooms \
-H "Content-Type: application/json" \
-d "{\"id\":\"LIB-301\",\"name\":\"Library Quiet Study\",\"capacity\":40}"
3. Get All Rooms
curl -X GET http://localhost:8081/api/v1/rooms
4. Create a Sensor Linked to a Room
curl -X POST http://localhost:8081/api/v1/sensors \
-H "Content-Type: application/json" \
-d "{\"id\":\"TEMP-001\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":22.5,\"roomId\":\"LIB-301\"}"
5. Filter Sensors by Type
curl -X GET "http://localhost:8081/api/v1/sensors?type=Temperature"
6. Add a Sensor Reading
curl -X POST http://localhost:8081/api/v1/sensors/TEMP-001/readings \
-H "Content-Type: application/json" \
-d "{\"timestamp\":\"2026-04-23T15:00:00\",\"value\":23.8}"
7. Get Sensor Reading History
curl -X GET http://localhost:8081/api/v1/sensors/TEMP-001/readings


##Report Answers
Question: In your report, explain the default lifecycle of a JAX-RS Resource class. Is a
new instance instantiated for every incoming request, or does the runtime treat it as a
singleton? Elaborate on how this architectural decision impacts the way you manage and
synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

Answer: JAX-RS resource classes are typically request-scoped in terms of instantiation; this implies that a new instance of the resource class will be created for each request, and the same object will not be used to serve multiple requests. This will help avoid any accidental use of per-request data across requests.
In my project this implies that I will not store any persistent application data within instance variables of RoomResource, SensorResource, and SensorReadingResource, since these instances will change from request to request. In order to make this data persist between requests, I save it in some common structure within my DataStore class.
These structures can be accessed concurrently, which means that certain precautions should be made with regard to concurrent access. If standard collections that are not thread-safe were to be used improperly, there could be race condition issues or incorrect modifications of the data.

Question: Why is the provision of “Hypermedia” (links and navigation within responses)
considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach
benefit client developers compared to static documentation?
Answer: Hypermedia means the API response will have links to other resources and actions. For instance, the response will include URLs that will indicate to the client the next steps to take.
This is why hypermedia is regarded as the standard of advanced RESTful architecture because:
•	The API is self-descriptive.
•	Clients are able to learn about available actions dynamically.
•	The API is easy to navigate without relying on any other source.
In contrast to static documentation, hypermedia is advantageous for:
•	The client does not have to hard-code URLs.
•	Even when the API undergoes changes, the client can use the URLs given.
•	It makes the client less dependent on API documentation.


Question: When returning a list of rooms, what are the implications of returning only
IDs versus returning the full room objects? Consider network bandwidth and client side
processing.
Answer: The usage of only room IDs will require less network bandwidth because the reply will be smaller. Thus, the requests can be quicker, particularly in case there are numerous rooms. Nevertheless, additional requests from the client's side to retrieve the complete information about each room will be required. 
The transfer of the whole object room contains additional information to be transferred by the client, thus, the usage of network resources increases. Still, on the other hand, the convenience of client operation improves, as everything is available at once without additional queries.
Thus, the return of only IDs is preferable when minimalization of the reply content is crucial, while the return of room objects is preferable when client friendliness matters more.

Question: Is the DELETE operation idempotent in your implementation? Provide a detailed
justification by describing what happens if a client mistakenly sends the exact same DELETE
Request for a room multiple times.
Answer: Yes, it is idempotent on the Delete operation.
It is possible to consider an operation to be idempotent, i.e. the effect of submitting the same request multiple times is the same final condition of the system after the first successful submission. The initial successful DELETE /rooms/{roomId} will remove the room in this specific API. Sending the same DELETE request many times will cause the same status to be returned, 404 Not Found, as the room will no longer exist. 
Nevertheless, the final position of the system will be the same: the room will not exist. Therefore, there will be no alterations in the state of the server, although variations in responses may occur.

Question: We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in
a different format, such as text/plain or application/xml. How does JAX-RS handle this
mismatch?

Answer: The @Consumes(MediaType.APPLICATION_JSON) annotation informs the API that the method accepts only requests of the type of a JSON.
Assuming a client uploads data in other format, such as:

•	text/plain
•	application/xml

Then the request will not be processed by JAX-RS. Rather it automatically re-emits:

415 Unsupported Media Type

This occurs since it does not have a comparable method that could consume the content type given.This behaviour ensures:

The API does not support other input formats.
Checks against bad or unjustified data formats.
Enhances data integrity and reliability.

In a nutshell, JAX-RS imposes the content type with the help of the annotation of @Consumes, and rejects any request that is not in the expected format.


Question: You implemented this filtering using @QueryParam. Contrast this with an alternative
design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why
is the query parameter approach generally considered superior for filtering and searching
collections?
Answer: Use of @QueryParam is recommended in case of applying the filter because the set of sensors is still considered to be the main resource.
/api/v1/sensors
In addition, a type attribute is a filter that can be omitted:
/api/v1/sensors?type=CO2
This is better than putting the type of the path such as:
	/api/v1/sensors/type/CO2
As opposed to the previous URL structure, use of @PathParam is not desirable since path parameters tend to denote a specific resource as compared to query parameters, which denote search, filtering, sorting, and paging operations.
The usage of the query parameters in the current case is more flexible. In the future, the API may introduce another filter like:
/api/v1/sensors?type=CO2&status=ACTIVE
This way will be better than adding numerous different paths.
Therefore, use of @QueryParam is preferred.

Question: Discuss the architectural benefits of the Sub-Resource Locator pattern. How
does delegating logic to separate classes help manage complexity in large APIs compared
to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller
class?

Answer: One way to keep REST APIs neat and organized is through Sub-Resource Locator, where nested resources' operations are encapsulated in a separate class.
In the present API project, SensorResource deals with the major sensors operations, and SensorReadingResource deals with nested operation readings:
/api/v1/sensors/{sensorId}/readings
It is much better than placing all endpoint operations in a single bulky controller, since:
•	Each class will have a single responsibility
•	code will be cleaner and maintainable
•	A nested resource will be easier to extend at a future time
•	Testing will be easier
•	API will reflect how sensors and readings are related to each other in real life
Putting everything in one huge controller will make its code very messy and hard to maintain over time. Delegating operations related to reading to a separate class will create a much more professional API design.

Question: Why is HTTP 422 often considered more semantically accurate than a standard
404 when the issue is a missing reference inside a valid JSON payload?
Answer: The 422 status code is more appropriate since the request itself is valid JSON, but the information contained in it is erroneous. This means that the request structure is correct, but it has a roomId that doesn’t exist.
The 404 status code is only meant to show that the endpoint/resource cannot be found; it is not meant for the erroneous input data in the request body.
Therefore, the 422 status code indicates that:
•	The request is correct
•	But the information is not correct

Question: From a cybersecurity standpoint, explain the risks associated with exposing
internal Java stack traces to external API consumers. What specific information could an
attacker gather from such a trace?
Answer: In case an API is leaking the stack trace, it might expose sensitive internal information that includes:
•	the file system path
•	class name and method name
•	information about any framework/library used
•	application internals
The attacker may use this information to:
•	Find possible weaknesses in the program
•	figure out the program structure
•	attack a particular area of the program
Therefore, all APIs need to display generic error messages only.

Question: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like
logging, rather than manually inserting Logger.info() statements inside every single resource
method?
Answer: Filters in JAX-RS can centralize logging, meaning that logging code does not need to be written within each individual method.
Benefits:
•	more organized code
•	less repetition
•	easier to manage and modify
•	automatically logs all requests and responses
When logging is done manually for each resource method:
•	repetitive code
•	more difficult to manage
•	higher possibility of logging being missed in certain methods
Therefore, filters should be preferred since they efficiently deal with crosscutting concerns such as logging.
