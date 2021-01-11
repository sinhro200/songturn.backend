package com.sinhro.songturn.backend.controller.registration

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailMessage
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.mail.javamail.MimeMessagePreparator
import org.springframework.stereotype.Component
import javax.mail.internet.MimeMessage
import kotlin.jvm.Throws

@Component
class ConfirmationMailBuilder {
    @Value("\${mail-confirmation.address}")
    private val address: String? = null

    @Value("\${server.port}")
    private val port: String? = null

    fun createSimpleConfirmationMail(emailTo: String, confirmationToken: String)
            : SimpleMailMessage {
        val simpleMailMessage = SimpleMailMessage()
        simpleMailMessage.setTo(emailTo)
        simpleMailMessage.setSubject("Complete Registration!")
        simpleMailMessage.setText(
                "To confirm your account, please click here : "
                        + "http://" + address
                        + (if (port.isNullOrBlank()) "" else ":$port")
                        + "/confirm-account?token=" + confirmationToken
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
                                + "http://" + address
                                + (if (port.isNullOrBlank()) "" else ":$port")
                                + "/confirm-account?token=" + confirmationToken
                )
            }
        }
    }
}