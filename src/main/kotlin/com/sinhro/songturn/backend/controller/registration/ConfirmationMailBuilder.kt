package com.sinhro.songturn.backend.controller.registration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.mail.javamail.MimeMessagePreparator
import org.springframework.stereotype.Component
import javax.mail.internet.MimeMessage


@Component
class ConfirmationMailBuilder {
    @Value("\${mail-confirmation.address}")
    private lateinit var address: String

    @Autowired
    private val serverProperties: ServerProperties? = null

    @Value("\${spring.profiles.active}")
    private lateinit var activeProfile: String

    fun createSimpleConfirmationMail(emailTo: String, confirmationToken: String)
            : SimpleMailMessage {
        val simpleMailMessage = SimpleMailMessage()
        simpleMailMessage.setTo(emailTo)
        simpleMailMessage.setSubject("Complete Registration!")
        simpleMailMessage.setText(
                "To confirm your account, please click here : " +
                        serverAddress()  + "/confirm-account?token=" + confirmationToken
        )
        return simpleMailMessage
    }

    fun createConfirmationMail(emailTo: String, confirmationToken: String)
            : MimeMessagePreparator {
        return object : MimeMessagePreparator {
            override fun prepare(mimeMessage: MimeMessage) {
                val messageHelper = MimeMessageHelper(mimeMessage)
                //                messageHelper.setFrom(sender);
                messageHelper.setTo(emailTo)
                messageHelper.setSubject("Complete Registration!")
                messageHelper.setText(
                        "To confirm your account, please click here : "
                                + serverAddress()
                                + "/confirm-account?token=" + confirmationToken
                )
            }
        }
    }

    private fun serverAddress(): String {
        return if (activeProfile.contains("local"))
            "http://$address:${serverProperties?.port}"
        else
            address
    }
}