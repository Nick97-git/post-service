# Post service
# Table of Contents
* [Project purpose](#purpose)
* [Technologies stack](#stack)
* [Launch project](#launch)
* [Test project](#test)
* [Author](#author)

# <a name="purpose"></a>Project purpose
Development of post service
<hr>
Without being authenticated you can login. There are specific urls for users with APICALL role.
Here is the list of such urls:
1. POST /api/mails - search for letters. It accepts a string as input, which will be searched for in recipients, subjects and text of the user's emails. Returns up to 20 of the last mails found;
2. POST /api/mail/{mailId} - answer for mail by its Id;
3. POST /api/mail - send new mail;
4. GET /api/users - get list of registered users;
5. GET /api/mails - get last 20 mails of user;
6. DELETE /api/mail/{mailId} - delete mail by its id.
<hr>

# <a name="stack"></a>Technologies stack
* Spring Boot
* Spring Security(Basic Authentication)
* Spring Jdbc
* H2
* Sl4j
* Algolia
* Swagger
* JUnit5
* Mockito 
* Lombok
<hr>

# <a name="launch"></a>Launch project

1. Open the project in your IDE.

2. Add Java SDK 8 in Project Structure.

3. Change a path to log file at src\main\resources\application.properties if you want - logging.file.name.

4. Write next commands in terminal:
 1) mvn clean package
 2) java -jar target/post-0.0.1-SNAPSHOT.jar

# <a name = "test"></a>Test project
For testing API you can download Postman or another such an analogue. There are test data that you can use.
There’s one user already registered with USER and APICALL roles (login = "nick", password = "1234") and two users with USER role (login = "john", password = "1234" and login = "mary", password = "1234"). All new users automatically get USER role. You can change these test data in data.sql if you want.
Also, user 'nick' has two created mails that you can manipulate with. You can change them in InjectDataController class.
All available endpoints you can see on http://localhost:8080/swagger-ui.html. For authorization, you must add a new header, where Authorization is key and Basic token is value, where token - value that you will get after encoding string login:password into Base64.

# <a name="author"></a>Author

Mykyta Arkhanhelskyi: https://github.com/Nick97-git
