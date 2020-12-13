package com.hep88.util
import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, DateTimeParseException}

object DateUtil {

  private val datePattern: String = "dd.MM.yyyy HH:mm"
  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(datePattern)

  implicit class DateFormatter(val date: LocalDateTime) {

    def asString: String = {
      if (date == null) {
        return null
      }
      dateFormatter.format(date)
    }
  }

  implicit class StringFormatter(val date: String) {

    def asDate: LocalDateTime = {
      try {
        LocalDateTime.parse(date, dateFormatter)
      }
      catch {
        case _: DateTimeParseException => null
      }
    }
  }

  implicit val orderingLocalDateTime: Ordering[LocalDateTime] = Ordering.by(d => (d.getYear, d.getDayOfYear, d.getMinute))

}

