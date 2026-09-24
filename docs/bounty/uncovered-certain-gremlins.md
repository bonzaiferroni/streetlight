# Uncovered Certain Gremlins

Found while adding KDoc across the modules. Neither is fixed yet. The password `println` and the unused post light were removed outright.

## Lockdown confirm posts to the wrong endpoint

`renderAccountLockdownConfirm` in `server/.../routes/renderTokenPage.kt` was copied from `renderAccountNotOwnedConfirm`, and its form still posts to `Api.AccountAction.AccountNotOwned` with the label "Remove My Address". That handler looks the lockdown token up as an `AccountNotOwned` token, finds nothing, and reports "This link is not valid." A lockdown link from a password or email change can never be redeemed.

```kotlin
formSubmit("Lock My Account", token, Api.AccountAction.LockdownAccount)
```

## Feed joins every star's lights, not the caller's

`Join.joinCaller` in `server/.../db/tables/GalaxyPostAspect.kt` joins `EventStarTable` and `LocationStarTable` without restricting `starId` to the caller. For a signed-in reader, a post whose event or location was lit by N stars appears N times, which also eats into the page limit, and `isLit` is true when anyone lit it. `EventQuery`, `EventLocationQuery` and `LocationAspect` already use `getConstraint(callerId)`.

```kotlin
.join(EventStarTable, JoinType.LEFT, PostTable.eventId, EventStarTable.eventId,
    additionalConstraint = { EventStarTable.starId.eq(callerId) })
.join(LocationStarTable, JoinType.LEFT, PostTable.locationId, LocationStarTable.locationId,
    additionalConstraint = { PostTable.postType.eq(PostType.Location) and LocationStarTable.starId.eq(callerId) })
```

Bounty:
* ✅ Point the lockdown confirm form at `Api.AccountAction.LockdownAccount`
* ✅ Constrain the caller joins in `joinCaller` to the caller's star id
* Tests, written: `TokenPageTest` submits each token page's form as rendered; `GalaxyFeedTest` reads event and location posts lit by other users
