# expensey
A complete (sample) webapp to track expenses.

Expensey is a full stack web application for expense tracking.

It is an example of how to build a webapp using [AngularJS](https://angularjs.org) and [Dropwizard](http://www.dropwizard.io/1.1.0/docs/) (Java).

# Backend

Dropwizard apps start with an Application class, found here in org.ggolden.expensey.dw.Application.

The various methods in here get called to setup our components (services), our database, our REST endpoints, and configure the server.  The config.yml file provides some details.

Our services and registered as singletons.  They provide:
- Authentication - user management and authentication
- Expense - models and persists and applies any business logic to expenses.

# Services

Each service is separated into four concerns:
- API - the methods to work with the service, as a Java interface
- Model - the POJO Java objects that model the information related to the service
- IMPL - the class or classes that implement the API
- Storage - a storage API providing a CRUD+ interface for object persistence, and one or more implementations of the API, each storing objects a different way (in memory, in SQL, etc)

# REST

Our REST classes are also registered as singletons.  Dropwizard (via Jersey) route all incoming REST requests to appropriate methods in the class, based on the annotations such as @GET or @PUT and @Path

Incoming requests are parsed for these methods.  Parameters from the URL are pulled out of the @Path.  Data sent in the request body are parsed from JSON into an appropriate object.  Results are converted into JSON.  See the @Consumes and @Produces annotations.  Jackson is used for the JSON mapping.

REST methods are responsible for
- authorizing the request
- validating the request parameters and data
- implementing the logic that glues together processing the request
- calling on appropriate services to acomplish the processing steps
- returning the appropriate response

Security is completely implemented in the REST methods.  This allows the services to do their work without worrying about authorization - they won't be called (from the REST methods) unless the user is authorized.

# Components and Injection

Dropwizard's HK2 component system is configured in the Application class.  Here is where we specify which Services we will make available, and which Storage implementations will be used by these services.

At the service implementation and REST endpoint class level, we simply declare with the @Inject annotation that the constructor needs to be injected with components registered (and usually created as singletons) by the Application.

The configuration data, which is read from yml into an appropriate class, is also registered and can be injected into any service, storage, or REST endpoint class that needs it.

# DB

SQL access is via the JDBI "convenience library", further wrapped in our own Transactor class to provide easy db transactions and some further convinience methods.  JDBI is part of Dropwizard.  This all makes it pretty easy to write methods to provide CRUD style access to data, augmented with additional special purpose reads (such as reading all items with some criteria).

SQL code is in our service storage implementation classes.  These are called by the API implementations, which provide a higher level abstraction and business rules.

The database used in this sample is an H2 database, an in-memory (file backed) SQL engine that pretty well matches the syntax used by MySQL.  In a real implementation, we would configure SQL connector information in the config and use JDBI to connect.

# Front End

Expensey is a single page web app (SPA) built using AngularJS and Bootstrap.

Dropwizard's embedded Jetty server on the backend is configured to serve all static assets from the "/assets/" path (see Application.java), and defaults to serving "index.html".  Maven is configured to pull all "src/main/resources/" artifacts into the jar, so that's where we put our front end static files.  These are located in the app project.

index.html references all our css and js files, and identifies the Angular app, "Expensey".  It then establishes a basic Bootstrap page, with a div for Angular's routing system "ng-view".  It also adds a standard Bootstrap footer.

expensey.js defines the Angular module.

routes.js define the routes to the two views, login and expenses.  We always start with login.

login.html is the login view, Bootstrap and Angular to present and act on the login UI.  It is backed by the Angular controller in login.js.
- Note the post_login() method, which mediates the login post to the server, and deals with the asynchronous results with promises and callbacks.
- Note also in the login() method the transition to the Angular route "/expenses", and that it replaces the current URL, so movement between login and expenses views is not seen by the browser history.

expenses.html and expenses.js handle the main expenses view.
- get_expenses() and post_expesnese() are the methods that interact with REST calls to the server.  These are like the post_login() method in login.js
- the Bootstrap navigation header includes a "logout" button, which invokes the logout() method in the controller, which alters the Angular route to "/login", logging out the user.

# Tools

You need Java 8 JDK, and Maven to build Expensey.  The Eclipse IDE and Docker are also useful tools to have.

# Running

You can run inside Eclipse.  Import the code as "Existing Maven Projects".  Run the org.ggolden.expensey.dw.Application class as a Java application with the arguments:

> server config.yml

You can build the code into a .jar (the standard delivery format for Dropwizard) using Maven.  From the source root directory,

> mvn clean install

You can run in a Docker image.  After the Maven build, build the Docker image.  From the app directory within the source,

> docker build -t expensey .

Designate a directory on your machine to hold logs and the file backing for the H2 database, such as ~/tmp/expensey.  Then run the docker image:

> docker run -d -p 8080:8080 -v $HOME/tmp/expensey/logs:/logs -v $HOME/tmp/expensey/data:/data -e "DB_URL=/data/expensey" --name expensey expensey

Then visit Expensey at http://localhost:8080.  Use either built-in user (see AuthenticationSericeImpl.java) 'user@mac.com' or 'user@gmail.com' with the configured password.