package com.sinhro.songturn.backend.providers

import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonException
import io.jsonwebtoken.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import javax.annotation.PostConstruct
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


@Component
class JwtAuthProvider {

    @Value("\${jwt.auth_secret}")
    private lateinit var jwtSecret: String

    @Value("\${jwt.auth_expiration_days:30}")
    private var expirationDays: Long = -1

    private lateinit var secretKey : SecretKey

    @PostConstruct
    fun customInit(){
        secretKey = generateSecretKey()
    }

    fun generateToken(id: Int): String {
        val date: Date = Date.from(
                OffsetDateTime
                        .now(ZoneOffset.UTC)
                        .plusDays(expirationDays)
                        .toInstant()
        )

        return Jwts.builder()
                .setSubject(id.toString())
                .setExpiration(date)
                .signWith(secretKey)
                .compact()
    }


    fun validateToken(token: String?): Boolean {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
            return true
        } catch (expEx: ExpiredJwtException) {
            throw CommonException(CommonError(ErrorCodes.AUTH_JWT_EXPIRED),expEx)
        } catch (unsEx: UnsupportedJwtException) {
            throw CommonException(
                    CommonError(ErrorCodes.AUTH_JWT_INVALID,
                            "Unsupported jwt"),
                    unsEx
            )
        } catch (mjEx: MalformedJwtException) {
            throw CommonException(
                    CommonError(ErrorCodes.AUTH_JWT_INVALID,
                            "Malformed jwt"),
                    mjEx
            )
        } catch (e: Exception) {
            throw CommonException(CommonError(ErrorCodes.AUTH_JWT_INVALID), e)
        }
    }

    fun getIdFromToken(token: String?): Int {
        val claims: Claims = Jwts
                .parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
        return claims.subject.toInt()
    }

    private fun generateSecretKey(): SecretKey {
        return SecretKeySpec(
                //DatatypeConverter.parseBase64Binary(jwtSecret),
                MessageDigest
                        .getInstance("SHA-256")
                        .digest(jwtSecret.encodeToByteArray())
                ,
                SignatureAlgorithm.HS256.jcaName)
    }

//    class TokenExpiredException : AuthenticationException {
//        constructor(msg: String?, t: Throwable?) : super(msg, t) {}
//        constructor(msg: String?) : super(msg) {}
//    }
//
//    class InvalidTokenException : AuthenticationException {
//        constructor(msg: String?, t: Throwable?) : super(msg, t) {}
//        constructor(msg: String?) : super(msg) {}
//    }
}