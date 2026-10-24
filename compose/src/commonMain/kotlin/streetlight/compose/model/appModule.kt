package streetlight.compose.model

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** The app's services and view models. */
val appModule = module {
    singleOf(::ThemeHost)
    viewModelOf(::EventFeed)
}
