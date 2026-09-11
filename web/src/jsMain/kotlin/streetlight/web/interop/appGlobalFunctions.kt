package streetlight.web.interop

import koala.dom.ViewScope
import koala.interop.KtFunction
import streetlight.web.layouts.FeedSection
import streetlight.web.ui.StarToggle

fun ViewScope.appGlobalFunctions() = listOf(
    KtFunction(StarToggle.ToggleAny, this::toggleAny),
    KtFunction(StarToggle.ToggleGalaxy, this::toggleGalaxy),
    KtFunction(AppFun.UpdateMark, this::queryAndUpdateMark),
    KtFunction(FeedSection.SortByMark, ::sortByMark),
    KtFunction(FeedSection.MorePosts, ::morePosts),
)
