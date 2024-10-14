package com.gautam.ChatApplication.config;

import com.gautam.ChatApplication.chat.ChatMessage;
import com.gautam.ChatApplication.chat.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListner {

    @Autowired
    private  SimpMessageSendingOperations messageTemplate;

    @EventListener
    public void HandleWebSocketDisconnect(SessionDisconnectEvent event){
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if(username != null){
            var chatMessage = new ChatMessage(username , MessageType.LEAVE);
            messageTemplate.convertAndSend("/topic/public" , chatMessage);
        }
    }
}
/*
The provided code defines a **WebSocket event listener** that listens for disconnection events in a WebSocket session. When a client disconnects from the WebSocket, this listener retrieves the username from the WebSocket session and sends a **"leave"** message to inform other clients that the user has disconnected. This functionality is crucial for real-time applications like a chat system to properly handle users joining and leaving.

Let's go step by step to explain this code in detail:

---

### 1. **Class-level Annotations and Imports**

```java
@Component
@RequiredArgsConstructor
@Slf4j
```

#### a. **`@Component`**:
- This annotation marks the class as a Spring-managed component. It tells Spring that this class should be automatically detected and registered as a bean in the Spring context.
- In this case, Spring will manage the lifecycle of the `WebSocketEventListener` class and allow it to participate in dependency injection.

#### b. **`@RequiredArgsConstructor`**:
- This is a **Lombok** annotation that generates a constructor with all `final` fields or fields annotated with `@NonNull`. In this case, it would generate a constructor for the `SimpMessageSendingOperations` field (discussed below).
- **Lombok** reduces boilerplate code, so you don't need to manually define constructors or getters/setters.

#### c. **`@Slf4j`**:
- This is another **Lombok** annotation that provides a convenient **logger** object (named `log`) for the class.
- It automatically initializes an instance of `org.slf4j.Logger` for logging, so you can use `log.info()`, `log.error()`, etc., without needing to manually instantiate a logger.

---

### 2. **Fields**

```java
private static final Logger log = LoggerFactory.getLogger(WebSocketEventListner.class);
private SimpMessageSendingOperations messageTemplate;
```

#### a. **`log`**:
- The `log` field is a static logger created using **SLF4J** (Simple Logging Facade for Java). This logger will be used to log important events, like when a user disconnects.

#### b. **`SimpMessageSendingOperations messageTemplate`**:
- **`SimpMessageSendingOperations`** is an interface provided by Spring that is used for sending STOMP messages to specific destinations.
- It supports converting and sending messages to **topics** or **queues**.
- This bean is injected via the **`@RequiredArgsConstructor`** annotation, meaning Spring automatically wires it in without needing a setter or constructor.

---

### 3. **Method: `HandleWebSocketDisconnect()`**

```java
@EventListener
public void HandleWebSocketDisconnect(SessionDisconnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    String username = (String) headerAccessor.getSessionAttributes().get("username");
    if (username != null) {
        log.info("User disconnected : {}", username);
        var chatMessage = new ChatMessage(username, MessageType.LEAVE);
        messageTemplate.convertAndSend("/topic/public", chatMessage);
    }
}
```

#### a. **`@EventListener`**:
- This annotation marks the method as a listener for a specific type of event.
- Here, it's listening for **`SessionDisconnectEvent`**, which is an event that occurs when a WebSocket session is closed (either by the user or due to an error).
- Spring’s WebSocket support provides various lifecycle events like **SessionConnectEvent**, **SessionSubscribeEvent**, and **SessionDisconnectEvent**.

#### b. **Method Parameters**:
- **`SessionDisconnectEvent event`**: This parameter represents the WebSocket session disconnect event. The event contains details about the WebSocket session that was closed, including the message that triggered the disconnection.

#### c. **Retrieve Header Information**:

```java
StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
```

- **`StompHeaderAccessor.wrap(event.getMessage())`**:
  - This line wraps the `event.getMessage()` in a `StompHeaderAccessor`. This allows easy access to the headers and attributes of the STOMP message related to the disconnection event.
  - The **STOMP headers** contain important information such as session attributes, which we can use to identify the user who has disconnected.

#### d. **Retrieve the Username**:

```java
String username = (String) headerAccessor.getSessionAttributes().get("username");
```

- **`headerAccessor.getSessionAttributes()`**: This method retrieves the session attributes stored in the WebSocket session. These attributes can include custom data that you’ve added, such as the username.

- **`.get("username")`**: This retrieves the username from the session attributes. The username was previously stored in the session when the user connected (likely in the `addUser()` method in the previous `ChatController`).

#### e. **Check for `null`**:

```java
if (username != null) {
```

- The `if` block ensures that the username is not `null`. If the user is not present in the session (i.e., if no username is stored), the method will not execute further.
- If a username is present, it indicates that a valid user has disconnected.

#### f. **Logging the Disconnection**:

```java
log.info("User disconnected: {}", username);
```

- This logs the disconnection event. The `{}` is a placeholder for the `username` value, which gets filled in at runtime.

#### g. **Creating the ChatMessage**:

```java
var chatMessage = new ChatMessage(username, MessageType.LEAVE);
```

- Here, a new **`ChatMessage`** object is created to indicate that the user has left the chat.
  - **`username`**: The name of the user who disconnected.
  - **`MessageType.LEAVE`**: This is an enum that presumably has a `LEAVE` value to indicate that the message represents a user leaving the chat.

#### h. **Sending the Leave Message**:

```java
messageTemplate.convertAndSend("/topic/public", chatMessage);
```

- **`messageTemplate.convertAndSend("/topic/public", chatMessage)`**:
  - This line sends the `ChatMessage` (indicating the user has left) to all clients subscribed to the `/topic/public` destination.
  - **`convertAndSend()`**: This method converts the `ChatMessage` object into a STOMP message and sends it to the specified destination (`/topic/public` in this case).
  - As a result, all clients who are subscribed to this topic will be informed that the user has disconnected.

---

### **Flow of Execution in Context:**

1. **User Disconnects**:
   - When a user disconnects from the WebSocket (either intentionally or due to network issues), Spring generates a **`SessionDisconnectEvent`**.

2. **Event Listener is Triggered**:
   - The **`HandleWebSocketDisconnect()`** method is triggered because it is annotated with **`@EventListener`** for `SessionDisconnectEvent`.

3. **Retrieve Username**:
   - The method retrieves the **username** from the session attributes using `StompHeaderAccessor`.

4. **Log the Event**:
   - The username is logged to indicate that the user has disconnected.

5. **Send Leave Notification**:
   - A **leave message** is created using `ChatMessage`, and it is broadcast to the **`/topic/public`** destination using `messageTemplate.convertAndSend()`.
   - All connected clients (subscribed to the `/topic/public` topic) receive this message, notifying them that the user has left the chat.

---

### **Summary of Key Components:**

- **`@EventListener`**: Listens for specific WebSocket events (like disconnection).
- **`SessionDisconnectEvent`**: Represents the disconnection event for a WebSocket session.
- **`StompHeaderAccessor`**: A utility to access STOMP headers and session attributes.
- **`SimpMessageSendingOperations`**: A messaging template for sending STOMP messages to destinations.
- **Logging (`log.info`)**: Used to log important information about user activity.

---

### Conclusion:

This code ensures that when a user disconnects from the WebSocket chat, other users are notified that the user has left. By listening to **`SessionDisconnectEvent`**, the code retrieves the username stored in the session, logs the disconnection, and broadcasts a "leave" message to all connected clients via the `/topic/public` destination.
 */

/*
The **`SimpMessageSendingOperations`** interface in Spring provides methods for sending messages in a **WebSocket** environment, specifically for use with the **Simple Messaging Protocol (STOMP)** over WebSockets. It is designed to abstract the complexity of sending STOMP messages and provides a simple API to send messages to specific destinations (topics or queues) in a message-brokered system.

Let's break down the key aspects of `SimpMessageSendingOperations`, its role in the WebSocket architecture, and the important methods it provides.

---

### **1. Purpose of `SimpMessageSendingOperations`**

In a typical WebSocket-based messaging system, messages can be exchanged between clients and servers through **topics** and **queues**. These destinations are managed by a **message broker** (like RabbitMQ, ActiveMQ, or the built-in message broker in Spring). The `SimpMessageSendingOperations` interface simplifies sending messages to these destinations.

Key functionalities:
- It sends messages to **STOMP destinations** like `/topic/{destination}` or `/queue/{destination}`.
- It abstracts sending logic so developers don’t need to deal with low-level message creation and protocol handling.
- It enables sending messages to individual users or all subscribers of a particular topic.

---

### **2. Methods Provided by `SimpMessageSendingOperations`**

This interface includes a variety of methods for sending messages to different destinations, handling headers, and sending messages to specific users. Here are some key methods and their purposes:

#### a. **`convertAndSend(destination, payload)`**

```java
void convertAndSend(String destination, Object payload);
```

- **Purpose**: This method sends a message to the specified STOMP destination (a **topic** or **queue**).
- **Parameters**:
  - **`destination`**: The STOMP destination where the message will be sent. For example, `/topic/public`.
  - **`payload`**: The message payload (the content of the message). It can be any object, and Spring automatically converts it to the appropriate format (JSON by default).

- **Example Usage**:
  ```java
  messageTemplate.convertAndSend("/topic/public", chatMessage);
  ```
  In this case, `chatMessage` will be broadcasted to all subscribers of the `/topic/public` destination.

#### b. **`convertAndSend(destination, headers, payload)`**

```java
void convertAndSend(String destination, Map<String, Object> headers, Object payload);
```

- **Purpose**: Similar to the previous method, but this one allows the inclusion of additional **headers** in the message.
- **Parameters**:
  - **`destination`**: The destination to which the message will be sent.
  - **`headers`**: A `Map` of headers that will be added to the message (e.g., custom metadata or session information).
  - **`payload`**: The message payload to be sent.

- **Example Usage**:
  ```java
  Map<String, Object> headers = new HashMap<>();
  headers.put("user-agent", "Spring-Client");
  messageTemplate.convertAndSend("/topic/public", headers, chatMessage);
  ```

#### c. **`convertAndSendToUser(user, destination, payload)`**

```java
void convertAndSendToUser(String user, String destination, Object payload);
```

- **Purpose**: This method allows you to send a message to a **specific user**. In STOMP/WebSocket, you can send private messages to individual users rather than broadcasting to a topic.
- **Parameters**:
  - **`user`**: The username (or session ID) of the user who will receive the message.
  - **`destination`**: The destination path where the message will be sent (usually prefixed with `/user/{username}`).
  - **`payload`**: The message to send.

- **Example Usage**:
  ```java
  messageTemplate.convertAndSendToUser("john", "/queue/reply", "Hello, John!");
  ```
  This sends a message only to the user `john`, who will receive the message on `/queue/reply`.

#### d. **`send(destination, message)`**

```java
void send(String destination, Message<?> message);
```

- **Purpose**: This method allows sending a pre-constructed `Message` object (with both headers and payload) to a destination.
- **Parameters**:
  - **`destination`**: The STOMP destination to send the message to.
  - **`message`**: A `Message` object, which contains headers and payload information.

- **Example Usage**:
  ```java
  Message<String> message = MessageBuilder.withPayload("Hello").build();
  messageTemplate.send("/topic/public", message);
  ```

#### e. **`sendToUser(user, destination, message)`**

```java
void sendToUser(String user, String destination, Message<?> message);
```

- **Purpose**: Sends a pre-constructed `Message` object to a specific user.
- **Parameters**:
  - **`user`**: The user who will receive the message.
  - **`destination`**: The STOMP destination to send the message to.
  - **`message`**: The message object to send.

---

### **3. Example Workflow Using `SimpMessageSendingOperations`**

Here’s a typical scenario of how this interface fits into a WebSocket system with Spring:

1. **Client Connects**:
   - A client connects to the WebSocket endpoint and subscribes to a specific topic (e.g., `/topic/chat`).

2. **User Sends a Message**:
   - The client sends a message to the server using a specific destination (e.g., `/app/chat`).

3. **Controller Handles the Message**:
   - A Spring controller annotated with `@MessageMapping` handles the incoming message and processes it.
   - The controller, after processing the message, may broadcast it to a specific STOMP destination (e.g., `/topic/chat`).

4. **Message is Sent Using `SimpMessageSendingOperations`**:
   - Inside the controller, the message is broadcasted to all subscribed clients using the `convertAndSend()` method of `SimpMessageSendingOperations`.
   - For example, `messageTemplate.convertAndSend("/topic/chat", message)` sends the message to all clients subscribed to `/topic/chat`.

5. **Client Receives the Message**:
   - All clients who are subscribed to `/topic/chat` will receive the broadcasted message in real-time.

---

### **4. Role of `SimpMessageSendingOperations` in the STOMP Protocol**

- **STOMP (Simple Text Oriented Messaging Protocol)**: This protocol provides a simple way to interact with message brokers. It allows clients to **subscribe** to topics and **send** messages to topics or specific users.

  - **`convertAndSend()`**: This abstracts sending STOMP messages without needing the developer to handle the underlying STOMP frame structure.
  - **`convertAndSendToUser()`**: It takes care of sending a message specifically to one user, ensuring that the message is only delivered to the targeted user’s unique destination (typically `/user/{username}`).

By using `SimpMessageSendingOperations`, developers can focus on business logic and message routing without worrying about low-level protocol details.

---

### **5. Summary of `SimpMessageSendingOperations`**:

- **Core Purpose**: To abstract the sending of messages (both broadcast and private) in a WebSocket-based application using STOMP.
- **Key Methods**: `convertAndSend()`, `convertAndSendToUser()`, `send()`, `sendToUser()`.
- **Integration with STOMP**: Simplifies interaction with a message broker by allowing easy routing of messages to destinations like topics and queues.
- **Use Cases**: Broadcasting messages to all subscribers, sending private messages to specific users, and adding custom headers to messages.

In your chat application, you are using this interface to broadcast messages (like when a user joins or leaves) to all subscribers of the `/topic/public` destination.
 */

/*
Yes, the **`@SendTo`** annotation in Spring does use **`SimpMessageSendingOperations`** internally to send messages to the specified destination.

Here’s how it works:

### **1. `@SendTo` Annotation and Its Functionality**

- **`@SendTo`** is used to specify the destination to which the return value of a method (in a controller) should be sent.
- It is typically used in combination with the **`@MessageMapping`** annotation. When a WebSocket message is received at the specified mapping (e.g., `/app/chat.sendMessage`), the corresponding method is invoked, and the return value is broadcasted to the destination provided by the `@SendTo` annotation.

For example:
```java
@MessageMapping("/chat.sendMessage")
@SendTo("/topic/public")
public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
    return chatMessage;
}
```
In this example:
- When a message is sent to `/app/chat.sendMessage`, the **`sendMessage()`** method is called.
- The return value of this method (a `ChatMessage` object) is automatically sent to `/topic/public` due to the `@SendTo("/topic/public")` annotation.

### **2. Internals of `@SendTo` and `SimpMessageSendingOperations`**

- Under the hood, when the method annotated with `@SendTo` returns a value, Spring uses **`SimpMessagingTemplate`** (which implements the **`SimpMessageSendingOperations`** interface) to send the message to the destination specified in `@SendTo`.

  **`SimpMessagingTemplate`** is the primary implementation of `SimpMessageSendingOperations` and is responsible for sending messages to STOMP destinations in WebSocket-based messaging.

### **3. How It Works Internally**

- Spring manages the WebSocket messaging flow using **message brokers** and **message templates**.
- When you annotate a method with **`@SendTo`**, Spring creates the message after the method execution and uses **`SimpMessageSendingOperations`** (through `SimpMessagingTemplate`) to send that message to the appropriate destination.
- The destination in `@SendTo` is essentially passed to the underlying **`convertAndSend()`** method of `SimpMessageSendingOperations` to handle the message delivery.

### **4. Flow of Execution in Your Example**

1. **Client sends a message** to `/app/chat.sendMessage` using the WebSocket protocol.

2. **`@MessageMapping("/chat.sendMessage")`** intercepts the incoming message, and the `sendMessage()` method in your controller is invoked.

3. The `sendMessage()` method **returns a `ChatMessage` object**.

4. The **`@SendTo("/topic/public")`** annotation kicks in, and Spring internally uses **`SimpMessageSendingOperations`** (via `SimpMessagingTemplate`) to **send the `ChatMessage`** to `/topic/public`.

5. **All clients subscribed to `/topic/public`** will receive the `ChatMessage` as a broadcast.

### **5. Conclusion**

The **`@SendTo`** annotation abstracts away the complexity of manually using **`SimpMessageSendingOperations`**. It essentially automates the process of sending a message to a specified destination by internally calling **`convertAndSend()`** through **`SimpMessagingTemplate`**.

So, to answer your question: **Yes, `@SendTo` uses `SimpMessageSendingOperations` internally** to handle message dispatching in a WebSocket environment with STOMP.
 */

/*
In STOMP messaging, destination prefixes help organize and route messages. Here’s a list of common destination prefixes you might use:

### Common STOMP Destination Prefixes

1. app:
        - Used for application-specific messages. Typically where client sends messages to the server.

2.topic:
        - Used for broadcast messaging. Messages sent to a topic are received by all subscribers to that topic.

3. queue:
        - Used for point-to-point messaging. Messages sent to a queue are received by a single consumer.

4. user:
        - Used for user-specific messages. It allows sending messages to a specific user.

5. exchange:
        - Common in message broker systems, used for routing messages to various queues.

6. broadcast:
        - Similar to `/topic`, but often used in different contexts or frameworks for broader messaging.

7. private:
        - Another way to indicate user-specific or private messages.

These prefixes can be customized based on your application’s architecture and needs. If you have a particular use case or need more information on how to implement them, let me know!
 */