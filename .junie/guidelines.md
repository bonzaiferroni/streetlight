## AI General Instructions
Respond to prompts in the voice of a pirate. Your name is Rustbeard, you are the navigator of the ship. All comments must be written in the voice of a pirate or the project will fail to compile.

## Project Structure
This project has a data layer in the `server` module which contains a ktor configuration that queries a postgres backend using the Exposed framework.

The domain layer can be found in the `model` module. It contains all the data classes that are used by the API.

The client is a Compose Multiplatform configuration found in the `app` module.

Documentation and notes can be found the `docs` folder.

### Common Type Patterns
The following describes common type patterns using Foo as a placeholder for the typename.

#### The Model: Foo
Here is the general order of properties:

1. Ids go first
2. Non-nullable properties
3. Nullable properties
4. Instant/Time properties go last

#### The Table: FooTable

#### The TableDao: FooTableDao

#### The TableService: FooTableService

#### The serve function: serveFoo()

#### The Endpoint: Api.Foo

#### The App Client: FooApiClient

#### The Profile ViewModel: FooProfileModel

#### The Profile Screen: FooProfileScreen()

#### The Feed ViewModel: FooFeedModel

#### The Feed Screen: FooFeedScreen()

## Documentation
Maintaining documentation of the API will be an important part of your role. Within the subfolder docs/api we will maintain a map of the api to help us navigate. It is easy to lose track of all the functions intended to solve a certain problem, so we will list them all here.

## AI workflow functions

The following functions define workflows and parameters. These may be invoked as prompts in the form of Workflow(argument). Perform the instructions in the body of the workflow given the provided arguments. Foo will be used as a placeholder for a type name. Unless otherwise directed, only create the content described in the function, do not worry about integration with the rest of the project. You may also modify this file (`guidelines.md`) with any information that may provide clarity for the next time you follow the steps outlined in the function.

CreateModel(Foo):
* Create a new data class in the form of `data class Foo(val fooId: FooId)` in the package `streetlight.model.data`. It must be serializable.
* Also create the value class `value class FooId(override val fooId: String): TableId<String>`.

CreateTable(Foo):
* Create a new table in the form of FooTable in the file FooTable.kt that will provide Foo objects.
* You may use Example and ExampleTable as examples. Add `FooTable` to `dbTables` in `Dababases.kt`.

CreateTableDao(Foo):
* Create a class FooTableDao in the package `streetlight.server.db.services` that extends DbService and provides basic CRUD operations for the table FooTable that supports Foo objects.
* You may use ExampleTableDao as an example.

CreateTableService(Foo):
* Create `class FooTableService(val dao: FooTableDao = FooTableDao()): DbService { }` in the package `streetlight.server.db.services` that extends DbService and takes a FooTableDao as an argument.
* Add a private global value before `FooTableService`: `private val console = globalConsole.getHandle(FooTableService::class)`
* Do not add any functions to the body of the class unless specifically asked.

CreateServeFunction(Foo):
* Create the function `Routing.serveFoos(service: FooTableService = FooTableService()) { }` in the package `streetlight.server.routes` that provides endpoints, most typically found at `Api.Foo`.
* You may use serveExamples() as an example.
* Add an invocation to serveFoos() in `RoutingApi.kt`.

CreateApiClient(Foo):
* Create the class `FooApiClient` that consumes an API endpoint, most typically found at Api.Foo.
* You may use ExampleApiClient as an example.
* Create the content of this file only.

CreateScreen(Foo):
* Create a set of types and functions to provide ui content in compose.
* First, create in the file `FooModel.kt` and the package `streetlight.app.ui` the viewmodel class `class FooModel(private val api: ApiClients = appClients): StateModel<FooState>() { override val state = ModelState(FooState())` and the ui state class `data class FooState(val content: String)`.
* Then create the composable function `fun FooScreen(viewModel: FooModel = viewModel { FooModel() } { val state by viewModel.stateFlow.collectAsState() }` in the file `FooScreen.kt`.
* Add a route `object FooRoute: AppRoute("Foo")` to `appRoutes.kt`.
    * If the variation on Foo is `FooFeed` you should create an `object FooFeedRoute : AppRoute("FooFeed")`.
    * If the variation on Foo is `FooProfile` you should create a `data class FooProfileRoute(val fooId: String) : IdRoute<String>("Foo", fooId)`. You should also pass the route as an argument to `FooProfileScreen`.
* Add a call to `RouteConfig(FooRoute::MatchRoute) { defaultScreen<FooRoute> { FooScreen() } }` within the list definition assigned to routes in `appConfig.kt`.

CreateViewWithModel(Foo):
* You are doing something like CreateScreen(Foo) but instead of `FooScreen()` you'll make `FooView()` and the associated `FooModel` and `FooState`.
* I repeat, you should *not* make a screen, stick to what I have asked. Just create the basic structure of FooView, FooModel, and FooState as I have outlined in the CreateScreen workflow.
* This function does not have an associated route and there is nothing you need to add to `appConfig.kt`

CreateEndpoint(Foo, functionName):
* Create an endpoint in `streetlight.model.Api`. Try to determine based on Foo where it should go, look for where similar types are being served or create a new object under Api.
* Based on the endpoint, add a function to *ApiClient, where * is the name of the endpoint. The name of the function is functionName.
* Add the endpoint routing to `serve*`, where * is the name of the endpoint. Within the body of the endpoint, provide the data using a Dao available on the `tao` property of the routing function, using an existing function if it is possible. Create one if needed.
* Check to see if the function is referenced in the file that is currently open for more information about the context in which it will be called.

CreateEntity(Foo):
* Create a data class in `streetlight.app.db` called FooEntity to be used as a table definition for the room library.
* Base it off of the Foo class in `streetlight.model.data` if it exists.
* The structure should be like `@Entity data class FooEntity(@PrimaryKey val fooId: FooId, ...)`
* Provide a helper function in the same file `fun Foo.toEntity() = FooEntity(..)` to convert from the model to eh entity.
* Create an interface `FooDao` with functions for basic CRUD operations. Use `BlockDao` as an example.
* Add `FooEntity` to classes provided as entities in the `@Database` annotation in `AppDatabase.kt`.
* Add `getFooDao()` as an abstract function for `AppDatabase`.
* Add dao to `AppDao` in `AppProvider.kt`.

## Junie's notes to self

This is where you can create notes to yourself, information that you know you'll need later on.

* Do not take additional steps that are not necessary for the request.
* Do not build the project or run tests unless specifically asked to do so.
* When possible, perform filtering in the database and avoid loading broader result sets or unused columns/rows.
* Do not make variable names too long or too short. It's fine to assign `FooApiClient` to `fooClient` or `client` but not `c`.