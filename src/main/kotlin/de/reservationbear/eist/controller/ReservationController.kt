package de.reservationbear.eist.controller

import de.reservationbear.eist.controller.responseMapper.ConfirmationMapper
import de.reservationbear.eist.controller.responseMapper.ReservationMapper
import de.reservationbear.eist.controller.responseMapper.TimeslotMapper
import de.reservationbear.eist.db.entity.Reservation
import de.reservationbear.eist.exceptionhandler.ApiException
import de.reservationbear.eist.service.MailService
import de.reservationbear.eist.service.ReservationService
import de.reservationbear.eist.service.TableService
import jdk.jfr.ContentType
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.sql.Timestamp
import java.util.*
import java.util.stream.Collectors
import javax.servlet.http.HttpServletResponse


/**
 * REST-Controller for the reservation entity
 */
@RestController
@RequestMapping(value = ["/api"])
class ReservationController(
    val reservationService: ReservationService,
    val tableService: TableService,
    val mailService: MailService
) {

    /**
     * Options method to change header for CORS
     *
     * @return 200 HttpStatus
     */
    @RequestMapping(value = ["/restaurant"], method = [RequestMethod.OPTIONS])
    fun options(response: HttpServletResponse): ResponseEntity<*>? {
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST,GET,PATCH,DELETE,OPTIONS")
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, MediaType.ALL_VALUE)
        return ResponseEntity<Any?>(HttpStatus.OK)
    }

    /**
     * Creates a reservation and pass it to the reservation service
     *
     * @param reservationMapper     Consumes JSON Object and creates a new reservation
     * @return                      ResponseEntity with status and body with JSON
     */
    @PostMapping(
        value = ["/reservation"],
        produces = ["application/json"]
    )
    fun createReservation(
        @RequestBody reservationMapper: ReservationMapper
    ): ResponseEntity<ReservationMapper> {

        val reservation = Reservation(
            null,
            reservationMapper.tables?.stream()
                ?.map { t -> tableService.getTable(t!!) }
                ?.collect(Collectors.toSet())
                ?.toSet(),
            Timestamp(reservationMapper.time!!.from!! * 1000),
            Timestamp(reservationMapper.time.to!! * 1000),
            reservationMapper.userName!!,
            reservationMapper.userEmail!!,
            false
        )

        if (reservation.restaurantTables == null || reservation.restaurantTables.isEmpty()) {
           throw ApiException("Tablelist cannot be null or error", 401)
        }

        reservationService.saveReservation(reservation)

        val insertedReservation: Reservation = reservation.id?.let { reservationService.getReservation(it) }!!

        mailService.sendRegistrationMail(
            insertedReservation.userEmail,
            insertedReservation.userName,
            insertedReservation.id!!
        )

        return ResponseEntity.ok(
            ReservationMapper(
                insertedReservation.id,
                insertedReservation.restaurantTables?.map { tables -> tables.id }?.toList(),
                TimeslotMapper(insertedReservation.reservationFrom.time / 1000, insertedReservation.reservationTo.time / 1000),
                insertedReservation.userName,
                insertedReservation.userEmail,
                insertedReservation.confirmed
            )
        )
    }

    /**
     * Returns a reservation, specified by the id.
     * Edits an existing reservation - is not allowed to create
     *
     * @param id        id of the reservation
     * @return          ResponseEntity with status and body with JSON
     */
    @GetMapping(
        value = ["/reservation/{id}"],
        produces = ["application/json"]
    )
    fun getReservation(
        @PathVariable("id") id: UUID
    ): ResponseEntity<ReservationMapper> {

        val reservation: Reservation = reservationService.getReservation(id)

        return ResponseEntity.ok(
            ReservationMapper(
                reservation.id,
                reservation.restaurantTables?.map { tables -> tables.id }?.toList(),
                TimeslotMapper(reservation.reservationFrom.time / 1000, reservation.reservationTo.time / 1000),
                reservation.userName,
                reservation.userEmail,
                reservation.confirmed
            )
        )
    }

    /**
     * Edits an existing reservation - is not allowed to create
     *
     * @param id                Id of the reservation
     * @param confirmationToken confirmationToken for the reservation
     * @return                  ResponseEntity with status and body with JSON
     */
    @PatchMapping(
        value = ["/reservation/{id}"],
        produces = ["application/json"]
    )
    fun patchReservation(
        @PathVariable("id") id: UUID,
        @RequestParam(value = "confirmationToken", required = true) confirmationToken: UUID,
        @RequestBody confirmationMapper: ConfirmationMapper
    ): ResponseEntity<ReservationMapper> {

        val patchedReservation: Reservation = reservationService.confirmReservation(id, confirmationToken)

        return ResponseEntity.ok(
            ReservationMapper(
                patchedReservation.id,
                patchedReservation.restaurantTables?.map { tables -> tables.id }?.toList(),
                TimeslotMapper(patchedReservation.reservationFrom.time / 1000, patchedReservation.reservationTo.time /1000),
                patchedReservation.userName,
                patchedReservation.userEmail,
                patchedReservation.confirmed
            )
        )
    }

    /**
     * Removes a reservation from the persistent layer.
     *
     * @param id            id of the reservation
     * @return              ResponseEntity with status and body with JSON
     */
    @DeleteMapping(
        value = ["/reservation/{id}"],
        produces = ["application/json"]
    )
    fun deleteReservation(
        @PathVariable("id") id: UUID
    ): ResponseEntity<ReservationMapper> {

        val removedReservation: Reservation = reservationService.deleteReservation(id)

        return ResponseEntity.ok(
            ReservationMapper(
                removedReservation.id,
                removedReservation.restaurantTables?.map { tables -> tables.id }?.toList(),
                TimeslotMapper(removedReservation.reservationFrom.time / 1000, removedReservation.reservationTo.time / 1000),
                removedReservation.userName,
                removedReservation.userEmail,
                removedReservation.confirmed
            )
        )
    }

    /**
     * Returns a reservation ics, specified by the id
     *
     * @param id        id of the reservation
     * @return          ResponseEntity with status and an ICS file
     */
    @GetMapping(
        value = ["/reservation/{id}/ics"],
        produces = ["text/calendar"]
    )
    fun getReservationIcs(
        @PathVariable("id") id: UUID
    ): ResponseEntity<Resource> {
        return ResponseEntity.ok(reservationService.getICSResource(id))
    }
}
