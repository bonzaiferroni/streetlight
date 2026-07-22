package streetlight.web.ui

import koala.dom.*
import koala.html.heading1

fun RouteScope.viewSandbox() {
    val model = Sandbox(contentScope, api, toaster)

    console.log("welcome to sandbox")

    filigree {
        heading1("Yer Sandbox")
    }

    row {
        textBlock("What is your name?")
        textField(model.nameField)
    }

     flowBlock(model.nameField) { name ->
         if (name == "wreck") throw SandboxException()
         textBlock("Hello ${name.takeIf { it.isNotBlank() } ?: "Someone"}, welcome to the sandbox.")
     }

    button("Check availability", model::checkAvailability)

    // flowBlock(model.isNameTaken) { isNameTaken ->
    //     when (isNameTaken) {
    //         null -> return@flowBlock
    //         true -> textBlock("That name is taken.")
    //         false -> textBlock("That name is available.")
    //     }
    // }

    // throw SandboxException()
}

class SandboxException : Exception("Arrr sandbox exception")

