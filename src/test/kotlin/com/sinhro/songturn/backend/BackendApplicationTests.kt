package com.sinhro.songturn.backend

import com.sinhro.songturn.rest.request_response.RegisterReqData
import com.sinhro.songturn.backend.pojos.UserPojo
import com.sinhro.songturn.backend.service.UserService
import com.sinhro.songturn.rest.validation.Validator
import com.sinhro.songturn.rest.validation.ValidationResult
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy


//@SpringBootTest
//@ContextConfiguration(classes=UserService)
class BackendApplicationTests {
	@Lazy
	@Autowired
	lateinit var userService : UserService

	@Test
	fun contextLoads() {
		LoggerFactory
				.getLogger(this.javaClass)
				.debug("Tests successful")
	}

//	@Test
	fun constraintsNewUser(){
		try {
			userService.registerUser(
					UserPojo(
							null,
							"test1",
							"test1",
							"test1",
							1,
							"test1",
							"test1",
							"test1"
					), true
			)
		}catch (e : Exception){
			e.printStackTrace()
		}
	}

	@Test
	fun validatorTest(){
		val obj = RegisterReqData("log","pas")
		val validator = Validator()

		val result = validator.validate(obj)

		assert((result["login"] ?: error("Result dont contains login"))
				.contains(ValidationResult.MinLengthError) &&
				(result["password"] ?: error("Result dont contains password"))
						.contains(ValidationResult.MinLengthError)
		)
		print("____________________________Success")
	}
}
