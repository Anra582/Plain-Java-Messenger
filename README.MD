# Plain Java Messenger
Unix-like terminal messenger. Pure Java without any frameworks.
Messenger uses REDIS as a database and message broker. All data, both users and messages, is stored in the Redis Streams format.
This allows to subscribe to events about the appearance of a new user in the system, as well as to a stream of new messages.
As a result, the application can asynchronously receive information about users and messages sent by them. 
The application implements the exchange of private messages between two users with the possibility of easy upgrade of the interface for creating chat rooms.

# Using
- At start application you need to write username. If user not exist a new one will be created.
- /exit : exit application
- /story_with username : print all messages with username
- /text_to username message : text to username with a message
- /export username filename : export chatting history with username to file
- /users : print list of all users
- /history : get history of commands for current session

## Run
Run Redis container via docker-compose from IDEA or command line via "docker-compose up". So as you wish.
By Gradle plugin run applications: gradle:application::run. You need to run at least two(chatting with yourself not so fun at all).