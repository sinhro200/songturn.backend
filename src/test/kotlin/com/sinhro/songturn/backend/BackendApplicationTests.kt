package com.sinhro.songturn.backend

import com.sinhro.songturn.rest.request_response.RegisterReqData
import com.sinhro.songturn.backend.service.UserService
import com.sinhro.songturn.rest.model.RegisterUserInfo
import com.sinhro.songturn.rest.validation.Validator
import com.sinhro.songturn.rest.validation.ValidationResult
import com.sinhro.songturn.rest.validation.ValidationResultType
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy


//@SpringBootTest
//@ContextConfiguration(classes=UserService)
class BackendApplicationTests {
    @Lazy
    @Autowired
    lateinit var userService: UserService

    @Test
    fun contextLoads() {
        LoggerFactory
                .getLogger(this.javaClass)
                .debug("Tests successful")
    }

    //	@Test
    fun constraintsNewUser() {
        try {
            userService.registerUser(
                    RegisterUserInfo(
                            "test1",
                            "test1",
                            "test1",
                            "test1",
                            "test1",
                            "test1",
                    ), true
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Test
    fun validatorTest() {
        val obj = RegisterReqData("log", "pas","","","","")
        val validator = Validator()

        val result = validator.validate(obj).resultForErrorFields()

        assert((result["login"] ?: error("Result dont contains login"))
                .find { it.type.equals(ValidationResultType.MinLengthError) } != null &&
                (result["password"] ?: error("Result dont contains password"))
                        .find { it.type.equals(ValidationResultType.MinLengthError) } != null
        )
        print("____________________________Success")
    }
}
