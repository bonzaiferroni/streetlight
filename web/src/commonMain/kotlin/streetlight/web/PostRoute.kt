package streetlight.web

import kampfire.api.Slug

data class PostRoute(override val slug: Slug): SlugRoute {
    override val screen get() = StreetlightScreen.Post
    override val title get() = "Post"
}

data class CreatePostRoute(override val slug: Slug): SlugRoute {
    override val screen get() = StreetlightScreen.CreatePost
    override val title get() = "Create Post"
}

data class PostUpdateRoute(override val slug: Slug): SlugRoute {
    override val screen get() = StreetlightScreen.UpdatePost
    override val title get() = "Update Post"
}
