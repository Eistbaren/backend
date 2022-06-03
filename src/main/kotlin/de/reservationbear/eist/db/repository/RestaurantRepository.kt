package de.reservationbear.eist.db.repository

import de.reservationbear.eist.db.entity.Comment
import de.reservationbear.eist.db.entity.Reservation
import de.reservationbear.eist.db.entity.Restaurant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.sql.Timestamp
import java.util.*

/**
 * The restaurant repository used to manipulate and query values from of the database
 */
interface RestaurantRepository : JpaRepository<Restaurant, UUID> {
    /**
     * Searches after comments / short reviews of a given restaurant
     * @param uuid the uuid of the restaurant
     * @param pageable access the database values in batches
     * @return a page of comments
     */
    @Query(
        value = "SELECT COMMENT.* FROM RESTAURANT " +
                "JOIN RESTAURANT_COMMENTS USING (RESTAURANT_ID) JOIN COMMENT USING (COMMENTS_ID) " +
                "WHERE RESTAURANT_ID = ?1",
        countQuery = "SELECT count(COMMENT.*) FROM RESTAURANT JOIN RESTAURANT_COMMENTS USING (RESTAURANT_ID) JOIN COMMENT USING (COMMENTS_ID) WHERE RESTAURANT_ID = ?1",
        nativeQuery = true
    )
    fun findCommentsOfRestaurant(uuid: UUID, pageable: Pageable?): Page<Comment?>?

    /**
     * Searches after reservations of a specific restaurant in a given time frame
     * @param uuid the uuid of the restaurant
     * @param from start of the search time frame
     * @param to end of the search time frame
     * @param pageable access the database values in batches
     * @return all reservation of the restaurant in the given time frame
     */
    @Query(
        value = "SELECT RESERVATION.* FROM RESTAURANT " +
                "JOIN RESTAURANT_RESERVATIONS USING (RESTAURANT_ID) JOIN RESERVATION USING (RESERVATION_ID) " +
                "WHERE RESTAURANT_ID = ?1 AND RESERVATION_FROM >= ?2 AND RESERVATION_TO <= ?3",
        countQuery = "SELECT RESERVATION.* FROM RESTAURANT " +
                "JOIN RESTAURANT_RESERVATIONS USING (RESTAURANT_ID) JOIN RESERVATION USING (RESERVATION_ID) " +
                "WHERE RESTAURANT_ID = ?1 AND RESERVATION_FROM >= ?2 AND RESERVATION_TO <= ?3",
        nativeQuery = true
    )
    fun findReservationsInTimeframeOfRestaurant(
        uuid: UUID,
        from: Timestamp,
        to: Timestamp,
        pageable: Pageable?
    ): Page<Reservation?>?
}