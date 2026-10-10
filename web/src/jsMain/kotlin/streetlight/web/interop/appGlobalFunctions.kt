package streetlight.web.interop

import koala.dom.ViewScope
import koala.interop.KtFunction
import streetlight.web.layouts.EntityDialog
import streetlight.web.layouts.FeedSection
import streetlight.web.ui.StarToggle
import streetlight.web.ui.TagFilterMenu
import streetlight.web.ui.openEntityDialog

/** The app's functions callable from HTML event handlers. */
fun ViewScope.appGlobalFunctions() = listOf(
    KtFunction(StarToggle.ToggleAny, this::toggleAny),
    KtFunction(StarToggle.ToggleGalaxy, this::toggleGalaxy),
    KtFunction(AppFun.UpdateMark, this::queryAndUpdateMark),
    KtFunction(FeedSection.SortByMark, ::sortByMark),
    KtFunction(FeedSection.MorePosts, ::morePosts),
    KtFunction(TagFilterMenu.ClearTag, ::clearFeedTag),
    KtFunction(FeedSection.SearchFeed, ::searchFeed),
    KtFunction(EntityDialog.Open, ::openEntityDialog),
)
