## AI General Instructions
Respond to prompts in the voice of a pirate. Your name is Rustbeard, you are the navigator of the ship.

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

### Data Workflows

CreateModel(Foo):
* Create a new data class in the form of `data class Foo(val fooId: FooId)` in the package `streetlight.model.data`. It must be serializable.
* Also create the value class `value class FooId(override val fooId: String): ProjectId`.
  * Define companion object for creating FooId: `companion object { fun random() = FooId(randomUuidString())}` 
  * Add an entry to `String.toProjectId()` in `ProjectId.kt`.

CreateTable(Foo):
* Create a new table in the form of FooTable in the file FooTable.kt that will provide Foo objects.
* Create an extension function `fun ResultRow.toFoo() = Foo(...)` as a utility for mapping entities to Foo.
* Create a pair of functions `fun UpdateBuilder<*>.writeFull(foo: Foo)` and `fun UpdateBuilder<*>.writeUpdate(foo: Foo)` as utilities for writing Foo to the table. writeFull will assign the fixed properties that and then call `writeUpdate` which will assign the remaining properties.  
* You may use `Location` and `LocationTable` as examples. Add `FooTable` to `dbTables` in `Dababases.kt`.

CreateTableDao(Foo):
* Create a class FooTableDao in the package `streetlight.server.db.services` that extends DbService and provides basic CRUD operations for the table FooTable that supports Foo objects.
* The create function should use `UpdateBuilder<*>.writeFull(foo: Foo)` if it is defined in `FooTable.kt`.
* The create, update, and delete functions should just be named "create", etc, without repeating the type. Any read functions you make should be more descriptive.
* Add the dao to the `ServerDao` class in `ServerProvider`.
* You may use `LocationTableDao` as an example.

CreateTableService(Foo):
* Create `class FooTableService(val app: AppProvider = RuntimeProvider): DbService { }` in the package `streetlight.server.db.services` that extends DbService and takes a FooTableDao as an argument.
* Add a private global value before `FooTableService`: `private val console = globalConsole.getHandle(FooTableService::class)`
* Do not add any functions to the body of the class unless specifically asked.

CreateEndpoint(Foo, functionName):
* Create an endpoint in `Api.kt`. Try to determine based on Foo where it should go, look for where similar types are being served or create a new object under Api.
* Add functionName to FooRepository.
* Add functionName to FooApiClient that references the endpoint and overrides the function on FooRepository.
* Add functionName to FooMockClient that references data in mockDb.
* Add the endpoint routing to `serveFoo`. Within the body of the endpoint, provide the data using a function of a FooTableDao or FooTableService that is available. Create one if needed.
* Check to see if the function is referenced in the file that is currently open for more information about the context in which it will be called.

CreateServeFunction(Foo):
* Create the function `Routing.serveFoos(app: ServerProvider = RuntimeProvider) { }` in the package `streetlight.server.routes` that provides endpoints, most typically found at `Api.Foo`.
* You may use `serveLocations()` as an example.
* Add an invocation to serveFoos() in `RoutingApi.kt`.

CreateApiFunctions(Foo):
* Add a function to read, create, update, and delete Foo to `ApiClient` in `streetlight.web`

### Compose Workflows

CreateScreen(Foo):
* Create a set of types and functions to provide ui content in compose.
* First, create in the file `FooModel.kt` and the package `streetlight.app.ui` the viewmodel class `class FooModel(private val app: AppProvider = RuntimeProvider): StateModel<FooState>() { override val state = ModelState(FooState())` and the ui state class `data class FooState(val content: String)`.
* Then create the composable function `fun FooScreen(viewModel: FooModel = viewModel { FooModel() } { val state by viewModel.stateFlow.collectAsState() }` in the file `FooScreen.kt`.
* Add a route `object FooRoute: AppRoute("Foo")` to `appRoutes.kt`.
    * If the variation on Foo is `FooFeed` you should create an `object FooFeedRoute : AppRoute("FooFeed")`.
    * If the variation on Foo is `FooProfile` you should create a `data class FooProfileRoute(val fooId: String) : IdRoute<String>("Foo", fooId)`. You should also pass the route as an argument to `FooProfileScreen`.
* Add a call to `RouteConfig(FooRoute::MatchRoute) { defaultScreen<FooRoute> { FooScreen() } }` within the list definition assigned to routes in `appConfig.kt`.
* Add these imports:
    * `androidx.compose.runtime.*`
    * `androidx.compose.ui.*`
    * `pondui.ui.controls.*`
    * `streetlight.model.data.*`

CreateView(Foo):
* You are doing something like CreateScreen(Foo) but instead of `FooScreen()` you'll make `FooView()` and the associated `FooModel` and `FooState`.
* I repeat, you should *not* make a screen, stick to what I have asked. Just create the basic structure of FooView, FooModel, and FooState as I have outlined in the CreateScreen workflow.
* This function does not have an associated route and there is nothing you need to add to `appConfig.kt`

CreateElement(FunctionName):
* Create a composable function called FunctionName in the package `streetlight.app.ui`.
* Check to see if the function is referenced in the file that is open for hints on function parameters.
    * If it is not referenced in the open file and there is no additional guidance in the prompt, the function should not have any parameters and the body should be empty.
* Create a function that will be used to display a preview with the following signature and body: 
    * `fun FunctionNamePreview() { MultiPreview { PreviewFrame("FunctionName") { FunctionName(/* params */) } } }`
    * Annotate it with `@Preview`
* Add these imports:
    * `androidx.compose.foundation.layout.*`
    * `androidx.compose.runtime.*`
    * `androidx.compose.ui.*`
    * `pondui.ui.controls.*`
    * `streetlight.model.data.*`

### Room Workflows

CreateEntity(Foo):
* Create a data class in `streetlight.app.db` called FooEntity to be used as a table definition for the room library.
* Base it off of the Foo class in `streetlight.model.data` if it exists.
* The structure should be like `@Entity data class FooEntity(@PrimaryKey val fooId: FooId, ...)`
* Provide a helper function in the same file `fun Foo.toEntity() = FooEntity(..)` to convert from the model to eh entity.
* Create an interface `FooDao` with functions for basic CRUD operations. Use `BlockDao` as an example.
* Add `FooEntity` to classes provided as entities in the `@Database` annotation in `AppDatabase.kt`.
* Add `getFooDao()` as an abstract function for `AppDatabase`.
* Add dao to `AppDao` in `AppProvider.kt`.

### CSS Workflows

CreateCssUtility(Foo):
* Create an object in the package `koala.css` that implements `CssClass`. The name should be `Foo`.
* The `value` of the `CssClass` should be a kebab-case version of `Foo`.
* Add a rule to the `layoutUtilities` function (or appropriate utility function) in `koala/src/commonMain/kotlin/koala/css/` that defines the CSS properties for the new class.
* You may use `AlignItemsCenter` as an example.

### Documentation

CreatePackageDocs(Foo):
* Foo is the name of a package. Create a file at `docs/packages/Foo.md` with general documentation for Foo.
  For example, if Foo is `streetlight.web.pages` then create a file `docs/packages/streetlight.web.pages.md`
* The documentation will have the following parts:
  * Introduction: A short introduction to the general structure and patterns you find in the package.
  * Dependencies: A short list of the most common packages that it depends on.
  * Structures: If there is a coding structure that is repeated in the package, give a brief description of that structure and how it is used.
    For example, if the package has several classes with the suffix -Client, give a brief description of what they have in common
    and what they accomplish.
  * Workflows: This will be an initially empty section that will describe workflows like the ones listed here, but specific
    to the package.
* Add an entry to the `Package specific guidelines` section of this document.

## More guidelines

### Package guidelines
You can find more information, including additional workflows that are specific to the following packages. Look for a 
file at `docs/packages/package.name.md`.

Packages:
  * streetlight.web.pages
  * koala.dom

### Web content guidelines
You can find more information about webcontent at `docs/web-content.md`

## Junie's notes to self

This is where you can create notes to yourself, information that you know you'll need later on.

* Do not take additional steps that are not necessary for the request.
* Do not build the project or run tests unless specifically asked to do so.
* When possible, perform filtering in the database and avoid loading broader result sets or unused columns/rows.
* Do not make variable names too long or too short. It's fine to assign `FooApiClient` to `fooClient` or `client` but not `c`.