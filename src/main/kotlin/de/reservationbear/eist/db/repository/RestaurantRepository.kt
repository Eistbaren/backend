package de.reservationbear.eist.db.repository

import de.reservationbear.eist.db.entity.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
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
            value = "SELECT r.comments " +
                    "FROM Restaurant r " +
                    "WHERE r.id = ?1",

            countQuery = "SELECT size(r.comments) " +
                    "FROM Restaurant r " +
                    "WHERE r.id = ?1",
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
            value = "SELECT r.reservations " +
                    "FROM Restaurant r " +
                    "JOIN r.reservations rs " +
                    "WHERE r.id = ?1 " +
                    "AND rs.reservationFrom >= ?2 " +
                    "AND rs.reservationTo <= ?3",
            countQuery = "SELECT count(r.reservations) " +
                    "FROM Restaurant r " +
                    "JOIN r.reservations rs " +
                    "WHERE r.id = ?1 " +
                    "AND rs.reservationFrom >= ?2 " +
                    "AND rs.reservationTo <= ?3",
    )
    fun findReservationsInTimeframeOfRestaurant(
            uuid: UUID,
            from: Timestamp,
            to: Timestamp,
            pageable: Pageable?
    ): Page<Reservation?>?

    /**
     * Searches after the tables of a given restaurant
     * @param uuid the uuid of the restaurant
     * @param pageable access the database values in batches
     * @return a page of tables
     */
    @Query(
            value = "SELECT r " +
                    "FROM RestaurantTable r " +
                    "WHERE r.restaurant.id = ?1",

            countQuery = "SELECT r " +
                    "FROM RestaurantTable r " +
                    "WHERE r.restaurant.id = ?1",
    )
    fun findTablesOfRestaurant(uuid: UUID, pageable: Pageable?): Page<RestaurantTable?>?

    /**
     * Searches after the opening hours of a given restaurant
     * @param uuid the uuid of the restaurant
     * @param fromDay the start of the day to query
     * @param fromDay the end of the day to query (normally the next day)
     * @param pageable access the database values in batches
     * @return a page of tables
     */
    @Query(
            value = "SELECT r.openingHours " +
                    "FROM Restaurant r " +
                    "JOIN r.openingHours oh " +
                    "WHERE r.id = ?1 " +
                    "AND oh.timeslotFrom >= ?2 " +
                    "AND oh.timeslotTo <= ?3",

            countQuery = "SELECT size(r.openingHours) " +
                    "FROM Restaurant r " +
                    "JOIN r.openingHours oh " +
                    "WHERE r.id = ?1 " +
                    "AND oh.timeslotFrom >= ?2 " +
                    "AND oh.timeslotTo <= ?3",
    )
    fun findTimeslotsInTimeframeOfRestaurant(
            uuid: UUID,
            fromDay: Timestamp,
            toDay: Timestamp,
            pageable: Pageable?
    ): Page<Timeslot?>?


    /**
     * Filters all restaurants based on the given query parameters. Each parameter can be null.
     * @param query string, substring that needs to be contained in the restaurants name
     * @param priceCategory int, from 1 to 3
     * @param minimumAverageRating double, from 1.0 t0 5.0
     * @param timeFrom date, start of the reservation
     * @param timeTo date, end of the reservation
     * @param latitude double coordinates of restaurant location
     * @param longitude double, coordinates of restaurant location
     * @param radius int, radius around the location to search
     * @param numberOfVisitors int, number of min-seats for tables
     * @return a page of Restaurants
     */
    @Query("SELECT DISTINCT r from Restaurant  r " +
            "JOIN r.restaurantTables t " +
            "where lower(r.name) LIKE lower(concat('%', :query, '%')) " +
            "and (:priceCategory IS NULL or r.priceCategory = :priceCategory)" +
            "and (:minimumAverageRating IS NULL or r.averageRating >= :minimumAverageRating)" +
            "and (:numberOfVisitors IS NULL or t.seats >= :numberOfVisitors)" +
            "and (:timeFrom IS NULL or :timeTo IS NULL or (t.id NOT IN " +
            "(Select tx.id from Reservation rsx " +
            "JOIN rsx.restaurantTables tx " +
            "WHERE (rsx.reservationFrom <= :timeFrom and rsx.reservationTo >= :timeTo) or (rsx.reservationFrom < :timeFrom " +
            "and rsx.reservationTo > :timeFrom) or (rsx.reservationFrom < :timeTo and rsx.reservationTo > :timeTo)))) " +
            "and (r.location IS NULL or :latitude IS NULL or :longitude IS NULL or :radius IS NULL or (" +
            ":radius >= 6371  * FUNCTION('acos', " +
            "(FUNCTION('cos', FUNCTION('radians', :latitude))) " +
            "* (FUNCTION ('cos', FUNCTION('radians', r.location.lat))) " +
            "* (FUNCTION('cos', (FUNCTION('radians', r.location.lon)) - (FUNCTION('radians', :longitude)))) " +
            "+ (FUNCTION('sin', FUNCTION('radians', :latitude))) " +
            "* (FUNCTION('sin', FUNCTION('radians', r.location.lat))))" +
            "))")

    fun filterRestaurants(@Param("query") query: String,
                          @Param("priceCategory") priceCategory: Int?,
                          @Param("minimumAverageRating") minimumAverageRating: Double?,
                          @Param("timeFrom") timeFrom: Date?,
                          @Param("timeTo") timeTo: Date?,
                          @Param("latitude") latitude: Double?,
                          @Param("longitude") longitude: Double?,
                          @Param("radius") radius: Double?,
                          @Param("numberOfVisitors") numberOfVisitors: Int?,
                          pageable: Pageable?) :Page<Restaurant>
}
