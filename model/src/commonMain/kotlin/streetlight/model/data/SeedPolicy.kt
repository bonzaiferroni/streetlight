package streetlight.model.data

enum class SeedPolicy(
    val policyScope: PolicyScope,
    val policyType: PolicyType,
    val policyTarget: PolicyTarget,
    val isDefault: Boolean,
    val isReportable: Boolean,
    val definition: String,
) {
    // Universe Rules
    IllegalActivity(
        PolicyScope.Universe, PolicyType.Dont, PolicyTarget.Content, true, true,
        """
        Do not use Streetlight to facilitate or coordinate illegal activity.
        """.trimIndent()
    ),
    Doxxing(
        PolicyScope.Universe, PolicyType.Dont, PolicyTarget.Content, true, true,
        """
        Do not post personal or identifying information about another person
        without their explicit consent.
        """.trimIndent()
    ),
    ModerationIntegrity(
        PolicyScope.Universe, PolicyType.Dont, PolicyTarget.Conduct, true, true,
        """
        Do not circumvent moderation decisions by reposting removed content,
        evading bans, or impersonating moderators.
        """.trimIndent()
    ),
    ExplicitMaterial(
        PolicyScope.Universe, PolicyType.Dont, PolicyTarget.Content, true, true,
        """
        Do not post sexually explicit or graphically violent material. 
        """.trimIndent()
    ),
    VoteManipulation(
        PolicyScope.Universe, PolicyType.Dont, PolicyTarget.Conduct, true, true,
        """
        Do not coordinate or solicit votes, mass-upvote or mass-downvote
        another user's contributions, or use multiple accounts to
        manipulate visibility.
        """.trimIndent()
    ),
    Spam(
        PolicyScope.Universe, PolicyType.Dont, PolicyTarget.Content, true, true,
        """
        Do not use Streetlight for unsolicited promotion, commercial spam,
        or astroturfing. Contributions should serve the community, not
        exploit its attention.
        """.trimIndent()
    ),
    UniverseAccuracy(
        PolicyScope.UniverseOnly, PolicyType.Do, PolicyTarget.Content, true, true,
        """
        Use accurate information when describing locations, events, and other content.
        """.trimIndent()
    ),

    // Galaxy Rules
    Civility(
        PolicyScope.Galaxy, PolicyType.Do, PolicyTarget.Content, true, true,
        """
        Communicate with respect for others. Disagreement is welcome;
        contempt is not.
        """.trimIndent()
    ),
    UseFactualTitles(
        PolicyScope.Galaxy, PolicyType.Do, PolicyTarget.Content, false, true,
        """
        Keep titles factual and descriptive. Save your perspective
        for the body of the post.
        """.trimIndent()
    ),
    StayOnTopic(
        PolicyScope.Galaxy, PolicyType.Do, PolicyTarget.Content, true, true,
        """
        Keep contributions relevant to this galaxy's stated purpose.
        """.trimIndent()
    ),
    Balanced(
        PolicyScope.Galaxy, PolicyType.Do, PolicyTarget.Content, false, false,
        """
        Present multiple perspectives on a topic, especially where
        reasonable people disagree.
        """.trimIndent()
    ),
}

enum class PolicyScope { Universe, UniverseOnly, Galaxy }
enum class PolicyType { Dont, Do }
enum class PolicyTarget { Content, Conduct }