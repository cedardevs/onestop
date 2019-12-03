import _ from 'lodash'
import moment from 'moment/moment'

// if the input represents a finite number, coerces and returns it, else null
export const textToNumber = text => {
  const number = text != null ? _.toNumber(text) : null
  if (_.isNaN(number)) {
    return null
  }
  if (_.isString(text) && _.isEmpty(text)) {
    return null
  }
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
  // expects and returns a string
  if (typeof year != 'string') {
    return year
  } // TODO really bad if an int, because it won't convert it

  if (_.isEmpty(year)) {
    return ''
  }
  let value = year

  // check for SI year values:
  let yearSI = year.toLowerCase()
  if (yearSI.endsWith('ka')) {
    let num = textToNumber(yearSI.split('ka')[0])
    value = num ? num * 1000 : NaN
  }
  else if (yearSI.endsWith('ma')) {
    let num = textToNumber(yearSI.split('ma')[0])
    value = num ? num * 1000000 : NaN
  }
  else if (yearSI.endsWith('ga')) {
    let num = textToNumber(yearSI.split('ga')[0])
    value = num ? num * 1000000000 : NaN
  }
  if (!Number.isInteger(textToNumber(value))) {
    return year // otherwise we'll always validate against empty string...
  }

  value = textToNumber(value)

  if (format == 'CE') {
    return `${value}`
  }
  if (format == 'BP') {
    return `${1950 - value}`
  }
  return year
}

export const isValidYear = year => {
  // assumes year is in CE! also assumes it is a STRING TODO isEmpty check will be wrong if an int is passed in here!

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

export const isValidDate = (year, month, day, nowOverride) => {
  let errors = {
    year: new Array(),
    month: new Array(),
    day: new Array(),
  }

  // No date given is technically valid (since a complete range is unnecessary)
  if (_.isEmpty(year) && _.isEmpty(month) && _.isEmpty(day)) {
    return errors
  }

  let dateMap = ymdToDateMap(year, month, day)

  const now = nowOverride ? nowOverride : moment()
  const givenDate = moment(dateMap)

  if (!_.isEmpty(year)) {
    if (dateMap.year == null) {
      // catches problems like 'foo'
      errors.year.push('invalid')
    }
    if (dateMap.year > now.year()) {
      errors.year.push('cannot be in the future')
    }
    if (dateMap.year < 0) {
      // moment() gets choked by negative years
      errors.year.push('must be greater than zero')
    }
  }

  if (!_.isEmpty(month)) {
    if (dateMap.month == null) {
      // hard to reproduce with a dropdown, but 'foo', presumably '-1' TODO
      errors.month.push('invalid')
    }
    else if (_.isEmpty(year)) {
      errors.year.push('required') // TODO dynamically set aria-required property on fields?
    }
    else if (
      dateMap.year != null &&
      dateMap.year <= now.year() &&
      !moment([ dateMap.year, dateMap.month ]).isSameOrBefore(now)
    ) {
      // only indicate month cannot be in the future if year alone is *not* in the future
      errors.month.push('cannot be in the future')
    }
  }

  if (!_.isEmpty(day)) {
    if (!givenDate.isValid()) {
      // TODO this is a weird one I don't know how to manually reproduce - try to verify it in tests ('-1'?)
      errors.day.push('invalid')
    }

    if (dateMap.day == null) {
      errors.day.push('invalid')
    }
    else if (!_.isEmpty(year) && _.isEmpty(month)) {
      // TODO indicate aria-required on field?
      errors.month.push('required')
    }
    else if (_.isEmpty(year) && _.isEmpty(month)) {
      // TODO indicate aria-required on field?
      // don't duplicate marking year required if month is set and will catch it (so only mark start year required if month is ALSO empty)
      errors.year.push('required')
      errors.month.push('required')
    }
    else if (
      dateMap.year <= now.year() &&
      moment([ dateMap.year, dateMap.month ]).isSameOrBefore(now) &&
      !givenDate.isSameOrBefore(now)
    ) {
      // only indicate month cannot be in the future if year and month alone is *not* in the future
      errors.day.push('cannot be in the future')
    }
  }

  return errors
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
