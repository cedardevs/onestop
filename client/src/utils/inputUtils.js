import _ from 'lodash'
import moment from 'moment/moment'

// if the input represents a finite number, coerces and returns it, else null
export const textToNumber = text => {
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

export const convertYearToCE = (year, format) => {
  // EXPECTS A STRING!! RETURNS A STRING!
  if (_.isEmpty(year)) {
    //TODO isEmpty check will be wrong if an int is passed in here!
    return ''
  }
  let value = year

  // check for SI year values:
  if (year.endsWith('ka')) {
    // TODO make these case insensitive!
    value = textToNumber(year.split('ka')[0]) * 1000
  }
  else if (year.endsWith('Ma')) {
    // TODO make these case insensitive!
    value = textToNumber(year.split('Ma')[0]) * 1000000
  }
  else if (year.endsWith('Ga')) {
    // TODO make these case insensitive!
    value = textToNumber(year.split('Ga')[0]) * 1000000000
  }
  if (!Number.isInteger(textToNumber(value))) {
    return value // otherwise we'll always validate against empty string...
  }
  value = textToNumber(value)
  if (format == 'CE') {
    return `${value}`
  }
  if (format == 'BP') {
    return `${1950 - value}`
  }
}

export const isValidYear = year => {
  // assumes year is in CE! also assumes it is a STRING TODO isEmpty check will be wrong if an int is passed in here!
  // TODO add unit tests!

  // No date given is technically valid (since a complete range is unnecessary)
  if (_.isEmpty(year)) {
    return true
  }

  if (!Number.isInteger(textToNumber(year))) {
    return false
  }

  const now = moment()

  return textToNumber(year) <= now.year()
}

export const isValidYearRange = (start, end) => {
  // assumes year is a STRING in CE!! TODO eeeep if not
  if (_.isEmpty(start) || _.isEmpty(end)) {
    return true
  }

  return textToNumber(start) < textToNumber(end)
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

  let validYear =
    year &&
    dateMap.year !== null &&
    dateMap.year <= now.year() &&
    dateMap.year >= 0
  let validMonth = month
    ? dateMap.month !== null &&
      dateMap.year !== null &&
      moment([ dateMap.year, dateMap.month ]).isSameOrBefore(now)
    : true
  let validDay = day
    ? dateMap.day !== null &&
      givenDate.isValid() &&
      givenDate.isSameOrBefore(now)
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
