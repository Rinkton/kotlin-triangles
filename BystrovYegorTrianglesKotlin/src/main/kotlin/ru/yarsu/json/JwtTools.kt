package ru.yarsu.json

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import org.http4k.core.Request
import ru.yarsu.classes.User
import ru.yarsu.storages.UserStorage
import java.time.Instant
import java.util.Date
import java.util.UUID

class JwtTools(
    private val secret: String,
    private val issuer: String,
    private val tokenLifetimeMs: Long,
) {
    private val algorithm: Algorithm = Algorithm.HMAC512(secret)

    fun createToken(user: User): String? {
        val now = Instant.now()
        return try {
            JWT
                .create()
                .withIssuer(issuer)
                .withSubject(user.login)
                .withClaim("role", user.role.v)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(tokenLifetimeMs)))
                .sign(algorithm)
        } catch (e: Exception) {
            System.err.println("Can't generate token: %{e.message}")
            null
        }
    }

    fun validateAndExtractUserId(
        token: String,
        userStorage: UserStorage,
    ): UUID? {
        return try {
            val verifier =
                JWT
                    .require(algorithm)
                    .withIssuer(issuer)
                    .build()

            val decodedJWT: DecodedJWT = verifier.verify(token)

            if (decodedJWT.expiresAt.before(Date())) {
                System.err.println("Token expired")
                return null
            }

            val login = decodedJWT.subject
            val user = userStorage.getUserByLogin(login)
            if (user == null) {
                System.err.println("No user with the given login")
                return null
            }
            return user.id
        } catch (ex: JWTVerificationException) {
            System.err.println("Token verification failed: ${ex.message}")
            null
        }
    }

    fun getExtractedUserIdAndValidate(
        request: Request,
        userStorage: UserStorage,
    ): UUID? {
        val token = request.header("Authorization")
        if (token != null) {
            val userId = validateAndExtractUserId(token, userStorage)
            if (userId != null) {
                return userId
            }
        }
        return null
    }
}
