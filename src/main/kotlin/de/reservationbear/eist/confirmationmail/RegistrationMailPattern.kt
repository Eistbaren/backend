//Source: https://www.youtube.com/watch?v=QwQuro7ekvc
package de.reservationbear.eist.confirmationmail

import org.springframework.stereotype.Service
import java.net.URL
import java.util.*
import kotlin.math.floor

/**
 * Pattern for sending a registration mail
 */
@Service
class RegistrationMailPattern(val mailSender: MailSender) {

    /**
     * Method that calls the mailSender.send method to send the mail
     *
     * @param mailAddress       Address for Mail
     * @param name              name of the recipient
     * @param reservationId     id from the reservation
     */
    fun sendMail(mailAddress: String, name: String, url: URL, reservationId: UUID) {
        //No such endpoint - do we want to approve mail addresses?
        val link = "${url.host + ":" + url.port}/reservation-details/${reservationId}"

        //List of Emojis for Title
        val icons = arrayOf("🍚","🥗","🍕","🍔","🍝","🍰","🧇","🌮","🥙","🍣","🥗","🍺","🍹","🍷")
        mailSender.send(
            mailAddress,
            buildEmail(name.split(" ")[0], link),
            "${icons[floor(Math.random() * icons.size).toInt()]} Confirmation of your reservation (${reservationId})",
            null
        )
    }

    /**
     * Mail text that will be sent (HTML Mail)
     *
     * @param name name of the recipient
     * @param link link for the reservation
     * @return
     */
    private fun buildEmail(name: String, link: String): String {
        return """
<div style="width: 100%; height: 50px; background-color: #81a1c1;">
	<h1 style="color: white; text-align: center; font-size: calc(7pt + 4vw); padding-top: 7px; font-family: Helvetica,Arial,sans-serif; margin-top: 0;">
		Reservation confirmation
	</h1>
</div>
<div style="width: 90%; height: 20px; background-color: #2e3440; margin: auto auto;">
</div>
<div style="height: 40px;"></div>
<div style="width: 90%; margin: auto auto;">
	<p style="margin: 0 0 20px 0; font-size: 19px; line-height: 25px; color: #2e3440c;">
		Hi $name,
	</p>
	<p style="margin: 0 0 20px 0; font-size: 19px; line-height: 25px; color: #2e3440;">
		Thank you for booking through our service. Below you will find a link to your dashboard:
	</p>
	<blockquote style="margin: 0 0 20px 0; border-left: 10px solid #2e3440; padding: 15px 0 0.1px 15px; font-size: 19px; line-height: 25px;">
		<p style="margin: 0 0 20px 0; font-size: 19px; line-height: 25px; color: #2e3440c;">
			<a style="color: #88c0d0;" href="$link">
				Click here for dashboard
			</a>
		</p>
	</blockquote>
	24 hours before your reservation date you will receive another email where you have to confirm your reservation one last time (this can be done up to 12 hours before).
	<p>
		Have a nice day
	</p>
	<p>
		Your Eistbären team 🐻‍❄️‍
	</p>
</div>
"""
    }
}
