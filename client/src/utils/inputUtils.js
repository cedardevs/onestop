import _ from 'lodash'
import moment from 'moment/moment'

// if the input represents a finite number, coerces and returns it, else null
export const textToNumber = (text) => {
  const number = text ? _.toNumber(text) : null
  return _.isFinite(number) ? number : null
}

export const ymdToDateMap = (year, month, day) => {
  const y = textToNumber(year)
  const m = textToNumber(month)
  const d = textToNumber(day)
  return {
    year: _.isInteger(y) ? y : null,
    month: _.isInteger(m) ? m : null,
    day: _.isInteger(d) ? d : null,
  }
}

export const isValidDate = (year, month, day) => {
  // No date given is technically valid (since a complete range is unnecessary)
  if (_.isEmpty(year) && _.isEmpty(month) && _.isEmpty(day)) {
    return true
  }

  // Valid date can be year only, year & month only, or full date
  if (!_.isEmpty(year) && _.isEmpty(month) && !_.isEmpty(day)) {
    // Year + day is not valid
    return false
  }

  let dateMap = ymdToDateMap(year, month, day)

  const now = moment()
  const givenDate = moment(dateMap)

  let validYear = year && dateMap.year !== null && dateMap.year <= now.year()
  let validMonth = month
    ? dateMap.month !== null && dateMap.year !== null && moment([dateMap.year, dateMap.month]).isSameOrBefore(now)
    : true
  let validDay = day
    ? dateMap.day !== null && givenDate.isValid() && givenDate.isSameOrBefore(now)
    : true

  return validYear && validMonth && validDay
}

export const isValidDateRange = (startMap, endMap) => {
  // No entered date will create a moment for now. Make sure if no data was entered, days are correctly identified as null
  const start = startMap.year == null ? null : moment(startMap)
  const end = endMap.year == null ? null : moment(endMap)

  // Valid date range can be just start, just end, or a start <= end
  if (start && end) {
    return start.isSameOrBefore(end)
  }
  else {
    return true
  }
}
