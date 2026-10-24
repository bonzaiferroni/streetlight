package streetlight.compose.model

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** The app's services and view models. */
val appModule = module {
    viewModelOf(::EventFeed)
}
