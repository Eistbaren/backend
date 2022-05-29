package de.reservationbear.eist.db.converter

import org.springframework.util.StringUtils
import java.net.URI
import javax.persistence.AttributeConverter
import javax.persistence.Converter

// source https://stackoverflow.com/questions/33781855/how-to-handle-properties-of-type-uri-with-spring-data-jpa
@Converter(autoApply = true)
class UriPersistenceConverter : AttributeConverter<URI?, String?> {
    override fun convertToDatabaseColumn(entityValue: URI?): String? {
        return entityValue?.toString()
    }

    override fun convertToEntityAttribute(databaseValue: String?): URI? {
        return if (StringUtils.hasLength(databaseValue)) URI.create(databaseValue!!.trim { it <= ' ' }) else null
    }
}