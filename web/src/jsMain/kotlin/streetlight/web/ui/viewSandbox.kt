package streetlight.web.ui

import koala.dom.*
import koala.html.heading1
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

fun ViewScope.viewSandbox() {
    val model = Sandbox(parentScope, api, toaster)

    console.log("welcome to sandbox")

    filigree {
        heading1("Yer Sandbox")
    }

    row {
        textBlock("What is your name?")
        textField("name", model.nameField)
    }

    flowBlock(model.nameField.flow) { name ->
        if (name == "wreck") throw SandboxException()
        textBlock("Hello ${name.takeIf { it.isNotBlank() } ?: "Someone"}, welcome to the sandbox.")
    }

    button("Check availability", model::checkAvailability)

    flowBlock(model.isNameTaken) { isNameTaken ->
        when (isNameTaken) {
            null -> return@flowBlock
            true -> textBlock("That name is taken.")
            false -> textBlock("That name is available.")
        }
    }

    // throw SandboxException()
}

class SandboxException : Exception("Arrr sandbox exception")

