package com.sinhro.songturn.backend.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessagePreparator
import org.springframework.stereotype.Component
import javax.mail.internet.MimeMessage

@Component
class EmailSenderService
    @Autowired constructor(
            private val javaMailSender: JavaMailSender
    )
{
    fun sendEmail(email: SimpleMailMessage?) {
        javaMailSender.send(email)
    }

    fun sendEmail(mail: MimeMessage?) {
        javaMailSender.send(mail)
    }

    fun sendEmail(mail: MimeMessagePreparator?) {
        javaMailSender.send(mail)
    }

}