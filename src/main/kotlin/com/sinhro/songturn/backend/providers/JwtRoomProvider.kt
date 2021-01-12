package com.sinhro.songturn.backend.providers

import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonException
import io.jsonwebtoken.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.MessageDigest
import javax.annotation.PostConstruct
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Component
class JwtRoomProvider {
    @Value("$(jwt.room_secret)")
    private lateinit var jwtSecret: String

    private lateinit var secretKey : SecretKey

    @PostConstruct
    fun customInit(){
        secretKey = generateSecretKey()
    }

    fun generateToken(title: String?, id: String?): String {
        return Jwts.builder()
                .setSubject(title)
                .setId(id)
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
            //can throw many exceptions. Some of them :
            //ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, etc
        } catch (e: Exception) {
            throw CommonException(CommonError(ErrorCodes.ROOM_NOT_FOUND), e)
        }
    }

    fun getRoomIdFromToken(token: String): String {
        try {
            val claims = Jwts.parserBuilder()
                    .setSigningKey(jwtSecret)
                    .build()
                    .parseClaimsJws(token)
                    .body
            return claims.id
        } catch (e: Exception) {
            throw CommonException(CommonError(ErrorCodes.ROOM_NOT_FOUND), e)
        }
    }

    fun generateSecretKey() : SecretKey{
        return SecretKeySpec(
                //DatatypeConverter.parseBase64Binary(jwtSecret),
                MessageDigest
                        .getInstance("SHA-256")
                        .digest(jwtSecret.encodeToByteArray())
                ,
                SignatureAlgorithm.HS256.jcaName)
    }
}