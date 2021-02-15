package com.sinhro.songturn.backend.repository

import com.sinhro.songturn.backend.jooq.CustomSQLExceptionTranslator
import com.sinhro.songturn.backend.tables.ConfirmationToken
import com.sinhro.songturn.backend.tables.Role
import com.sinhro.songturn.backend.tables.Users
import com.sinhro.songturn.backend.tables.records.UsersRecord
import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonException
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneOffset
import com.sinhro.songturn.backend.tables.pojos.ConfirmationToken as ConfirmationTokenPojo
import com.sinhro.songturn.backend.tables.pojos.Role as RolePojo
import com.sinhro.songturn.backend.tables.pojos.Users as UserPojo

@Component
class UserRepository @Autowired constructor(
        val dsl: DSLContext
) {
    init {
        dsl.configuration().set(CustomSQLExceptionTranslator())
    }

    private val tableUsers: Users = Users.USERS
    private val tableRole: Role = Role.ROLE
    private val tableConfirmation: ConfirmationToken = ConfirmationToken.CONFIRMATION_TOKEN

    fun saveUser(userPojo: UserPojo): UserPojo {

        val usersRecord = dsl.newRecord(tableUsers)
        updateUserRecordFieldsFromPojo(usersRecord, userPojo)
        usersRecord.store()
        return usersRecord.into(UserPojo::class.java)
    }

    fun removeUser(userPojo: UserPojo): UserPojo? {
        return dsl.deleteFrom(tableUsers)
                .where(tableUsers.ID.eq(userPojo.id))
                .returning()
                .fetchOne()
                ?.into(UserPojo::class.java)
    }

    fun defaultRole(): RolePojo {
        return dsl.selectFrom(tableRole)
                .where(tableRole.NAME.eq("ROLE_USER"))
                .fetchOne()
                ?.into(RolePojo::class.java)
                ?: throw CommonException(
                        CommonError(ErrorCodes.INTERNAL_SERVER_EXC),
                        "default role, ROLE_USER, not found"
                )
    }

    fun saveConfirmationToken(confirmationTokenPojo: ConfirmationTokenPojo):
            ConfirmationTokenPojo {
        val confTokenRecord = dsl.newRecord(tableConfirmation)
        confTokenRecord.from(confirmationTokenPojo)
        confTokenRecord.store()
        return confTokenRecord.into(ConfirmationTokenPojo::class.java)
    }

    fun removeConfirmationToken(confirmationTokenPojo: ConfirmationTokenPojo)
            : ConfirmationTokenPojo? {
        return dsl.deleteFrom(tableConfirmation)
                .where(tableConfirmation.ID.eq(confirmationTokenPojo.id))
                .returning()
                .fetchOne()
                ?.into(ConfirmationTokenPojo::class.java)
    }


    fun getConfirmationToken(user: UserPojo): ConfirmationTokenPojo? {
        return dsl.select(tableConfirmation.TOKEN)
                .from(tableConfirmation)
                .where(tableConfirmation.USER_ID.eq(user.id))
                .fetchOne()
                ?.into(ConfirmationTokenPojo::class.java)
    }

    fun setUserVerified(up: UserPojo, isVerified: Boolean = true): UserPojo? {
        /*  ###     OLD version
            ###     why by email I dont remember
            val user = userService.findByEmailIgnoreCase(token.userEntity?.email)
            user.isVerified = true
            if (userService.setVerified(user).isVerified) {
                confirmationTokenService.removeByConfirmationToken(confirmationToken)
                ResponseEntity.ok("account verified")
            } else ResponseEntity.badRequest().body("Account not verified")
        */

        return dsl.update(tableUsers)
                .set(tableUsers.IS_VERIFIED, isVerified)
                .where(tableUsers.ID.eq(up.id))
                .returning()
                .fetchOne()
                ?.into(UserPojo::class.java)
    }

    fun findUserByConfirmationToken(confirmationToken: String): UserPojo? {
        val userId = dsl.select(tableConfirmation.USER_ID)
                .from(tableConfirmation)
                .where(tableConfirmation.TOKEN.eq(confirmationToken))
                .fetchOne()
                ?.get(tableConfirmation.USER_ID)
        userId?.let {
            return dsl.selectFrom(tableUsers)
                    .where(tableUsers.ID.eq(it))
                    .fetchOne()
                    ?.into(UserPojo::class.java)
        }
        return null
    }

    fun findUserByLogin(login: String): UserPojo? {
        return dsl.selectFrom(tableUsers)
                .where(tableUsers.LOGIN.eq(login))
                .fetchOne()
                ?.into(UserPojo::class.java)
    }

    fun findUserByEmail(email: String): UserPojo? {
        return dsl.selectFrom(tableUsers)
                .where(tableUsers.EMAIL.eq(email))
                .fetchOne()
                ?.into(UserPojo::class.java)
    }

    fun users(): MutableList<UserPojo> {
        return dsl.select(tableUsers.ID,
                tableUsers.NICKNAME,
                tableUsers.EMAIL,
                tableUsers.LOGIN,
                tableUsers.PASSWORD,
                tableUsers.ROLE_ID,
                tableUsers.FIRST_NAME,
                tableUsers.LAST_NAME,
                tableUsers.IS_VERIFIED)
                .from(tableUsers)
                .fetch()
                .into(UserPojo::class.java)
    }

    fun getRoleByRoleId(roleID: Int): RolePojo? {
        return dsl.select(tableRole.ID, tableRole.NAME)
                .from(tableRole)
                .where(tableRole.ID.eq(roleID))
                .fetchOne()
                ?.into(RolePojo::class.java)
    }

    fun deleteUserById(id: Int) {
        dsl.deleteFrom(tableUsers)
                .where(tableUsers.ID.eq(id))
    }

    fun findUserById(id: Int): UserPojo? {
        return dsl.selectFrom(tableUsers)
                .where(tableUsers.ID.eq(id))
                .fetchOne()
                ?.into(UserPojo::class.java)
    }

    fun setUserInRoom(userId: Int, roomId: Int): UserPojo? {
        return dsl.update(tableUsers)
                .set(
                        tableUsers.ROOM_ID,
                        roomId)
                .where(tableUsers.ID.eq(userId))
                .returning()
                .fetchOne()
                ?.into(UserPojo::class.java)
    }

    fun getRoomInWhichUserIn(userId: Int): Int? {
        return dsl.select(tableUsers.ROOM_ID)
                .from(tableUsers)
                .where(tableUsers.ID.eq(userId))
                .fetchOne()
                ?.into(Int::class.java)
    }

    fun setUserNotInRoom(userId: Int): UserPojo? {
        return dsl.update(tableUsers)
                .setNull(tableUsers.ROOM_ID)
                .where(tableUsers.ID.eq(userId))
                .returning()
                .fetchOne()
                ?.into(UserPojo::class.java)
    }

    fun getUsersInRoom(roomId: Int): List<UserPojo> {
        return dsl.selectFrom(tableUsers)
                .where(tableUsers.ROOM_ID.eq(roomId))
                .fetch()

                .into(UserPojo::class.java)
    }

    fun updateUser(oldUserPojo: UserPojo, userPojo: UserPojo): UserPojo? {

        val userRecord = dsl.selectFrom(tableUsers)
                .where(tableUsers.ID.eq(oldUserPojo.id))
                .fetchOne() ?: return null
        updateUserRecordFieldsFromPojo(userRecord, userPojo)
        userRecord.store()
        return userRecord.into(UserPojo::class.java)
    }

    private fun updateUserRecordFieldsFromPojo(userRecord: UsersRecord, userPojo: UserPojo) {
        userPojo.nickname?.let(userRecord::setNickname)
        userPojo.login?.let(userRecord::setLogin)
        userPojo.firstName?.let(userRecord::setFirstName)
        userPojo.lastName?.let(userRecord::setLastName)
        userPojo.email?.let(userRecord::setEmail)
        userPojo.password?.let(userRecord::setPassword)
        userPojo.roleId?.let(userRecord::setRoleId)
        userPojo.roomId?.let(userRecord::setRoomId)
        userPojo.isVerified?.let(userRecord::setIsVerified)
        userRecord.store()
    }

    fun updateLastOnline(user: UserPojo): UserPojo? {
        return dsl.update(tableUsers)
                .set(tableUsers.LAST_ONLINE, OffsetDateTime.now(ZoneOffset.UTC))
                .where(tableUsers.ID.eq(user.id))
                .returning()
                .fetchOne()
                ?.into(UserPojo::class.java)
    }

    fun removeDemoUsersNotOnlineFrom(dt: OffsetDateTime): MutableList<UserPojo> {
        return dsl.deleteFrom(tableUsers)
                .where(
                        tableUsers.LAST_ONLINE.lessThan(dt)
                                .and(
                                        tableUsers.PASSWORD.isNull
//                                                .or(
//                                                    tableUsers.PASSWORD.like("")
//                                                )
                                )
                )
                .returning()
                .fetch()
                .into(UserPojo::class.java)
    }
}