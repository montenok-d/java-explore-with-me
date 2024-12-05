# java-explore-with-me

## КОММЕНТАРИИ К СОБЫТИЯМ
**PULL-REQUEST:** https://github.com/montenok-d/java-explore-with-me/pull/4

### Endpoints

#### 1. PRIVATE
**POST: /users/{userId}/events/{eventId}/comments**
- Add comment by user

**GET: /users/{userId}/comments**
- Get all user's comments sorted by date
- Params: from, end

**PATCH: /users/{userId}/comments/{commentId}**
- Update comment text by user


**DELETE: /users/{userId}/comments/{commentId}**
- Delete comment by user

#### 2. ADMIN
**DELETE: /admin/comments/{commentId}**
- Delete comment by admin

#### 3. PUBLIC
**GET: events/{eventId}/comments**
- Get comments for event sorted by date
- Params: from, end
