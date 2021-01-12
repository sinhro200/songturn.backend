package com.sinhro.songturn.backend.repository

import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.backend.jooq.CustomSQLExceptionTranslator
import com.sinhro.songturn.backend.pojos.ConfirmationTokenPojo
import com.sinhro.songturn.backend.pojos.RolePojo
import com.sinhro.songturn.backend.pojos.UserPojo
import com.sinhro.songturn.backend.tables.ConfirmationToken
import com.sinhro.songturn.backend.tables.Role
import com.sinhro.songturn.backend.tables.Users
import com.sinhro.songturn.rest.core.CommonException
import org.jooq.DSLContext
import org.jooq.impl.DSL.row
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

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

    fun validateNewUserData(newUserPojo: UserPojo) {
        newUserPojo.login?.let {
            findUserByLogin(it)?.let { userSameLogAsNewUserLog ->
                if (userSameLogAsNewUserLog.id != newUserPojo.id)
                    throw CommonException(CommonError(ErrorCodes.LOGIN_IS_USED))
            }
        }

        newUserPojo.email?.let { newUserEmail ->
            findUserByEmail(newUserEmail)?.let { userSameEmailAsNewUserEmail ->
                if (userSameEmailAsNewUserEmail.id != newUserPojo.id) {
                    //found another user with email the same as new user email
                    throw CommonException(CommonError(ErrorCodes.EMAIL_IS_USED))
                }
            }
        }


    }

    fun saveUser(userPojo: UserPojo): UserPojo? {
        val res = dsl.insertInto(tableUsers,
                //tableUsers.ID,
                tableUsers.NICKNAME,
                tableUsers.EMAIL,
                tableUsers.LOGIN,
                tableUsers.PASSWORD,
                tableUsers.ROLE_ID,
                tableUsers.FIRST_NAME,
                tableUsers.LAST_NAME,
                tableUsers.IS_VERIFIED
        )
                .values(
                        userPojo.nickname,
                        userPojo.email,
                        userPojo.login,
                        userPojo.password,
                        userPojo.role_id,
                        userPojo.first_name,
                        userPojo.last_name,
                        userPojo.isVerified
                )
                .returning(
                        tableUsers.ID
                )
        //ToDo simplify code around
//        val fetch = res.fetchOne()?.get(tableUsers.ID)
        val userId = res.fetch()[0]?.get(tableUsers.ID)
        return dsl.select(tableUsers.NICKNAME,
                tableUsers.EMAIL,
                tableUsers.LOGIN,
                tableUsers.PASSWORD,
                tableUsers.ROLE_ID,
                tableUsers.FIRST_NAME,
                tableUsers.LAST_NAME,
                tableUsers.IS_VERIFIED)
                .from(tableUsers)
                .where(tableUsers.ID.eq((userId)))
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

    fun saveConfirmationToken(confirmationToken: ConfirmationTokenPojo)
            : ConfirmationTokenPojo? {
        return dsl.insertInto(tableConfirmation)
                .columns(
                        tableConfirmation.TOKEN,
                        tableConfirmation.CREATED_DATE,
                        tableConfirmation.USER_ID)
                .values(
                        confirmationToken.token,
                        confirmationToken.createdDate,
                        confirmationToken.userId)
                .returning()
                .fetchOne()
                ?.into(ConfirmationTokenPojo::class.java)
    }

    fun getConfirmationToken(user: UserPojo): String? {
        return dsl.select(tableConfirmation.TOKEN)
                .from(tableConfirmation)
                .where(tableConfirmation.USER_ID.eq(user.id))
                .fetchOne()
                ?.get(tableConfirmation.TOKEN)
    }

    fun verifyUser(up: UserPojo): UserPojo? {
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
                .set(tableUsers.IS_VERIFIED, true)
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

    //ToDo максимально обобщённый метод, надо разнести на несколько.
    // Не нравится как он выглядит
    fun updateUserData(userPojo: UserPojo): UserPojo? {
        return dsl.update(tableUsers)
                .set(
                        row(
                                tableUsers.NICKNAME,
                                tableUsers.EMAIL,
                                tableUsers.LOGIN,
                                tableUsers.PASSWORD,
                                tableUsers.ROLE_ID,
                                tableUsers.FIRST_NAME,
                                tableUsers.LAST_NAME,
                                tableUsers.IS_VERIFIED
                        ),
                        row(
                                userPojo.nickname,
                                userPojo.email,
                                userPojo.login,
                                userPojo.password,
                                userPojo.role_id,
                                userPojo.first_name,
                                userPojo.last_name,
                                userPojo.isVerified
                        ))
                .where(
                        tableUsers.ID.eq(userPojo.id))
                .returning()
                .fetchOne()
                ?.into(UserPojo::class.java)
    }

    fun findUserById(id: Int): UserPojo? {
        return dsl.selectFrom(tableUsers)
                .where(tableUsers.ID.eq(id))
                .fetchOne()
                ?.into(UserPojo::class.java)
    }
}