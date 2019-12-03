import {useState, useEffect} from 'react'

import _ from 'lodash'
import moment from 'moment/moment'
import {
  ymdToDateMap,
  isValidDate,
  isValidDateRange,
} from '../../../../utils/inputUtils'

/**
  Corresponds to a value, tied to a single form field.
**/
function useDatePart(){
  // value is a string, tied to an input
  const [ value, setValue ] = useState('')
  // valid status, including controls for display validity, validitiy of this field (internal), and validity as part of a larger set (external)
  const valid = useLayeredValidation()
  // array of error messages on the field
  const [ errors, setErrors ] = useState(new Array())

  useEffect(
    // reset the validity and error messages when the field is reset to a blank value
    () => {
      if (value == '') {
        valid.setInternal(true)
        setErrors(new Array())
      }
    },
    [ value ]
  )

  return {
    value: value,
    set: setValue,
    valid: valid.valid,
    setValidInternal: valid.setInternal,
    setValidExternal: valid.setExternal,
    errors: errors,
    setErrors: setErrors,
  }
}

export function useLayeredValidation(){
  // validation of this specific field (in isolation)
  const [ internal, setInternal ] = useState(true)
  // validation of this field as part of the set
  const [ external, setExternal ] = useState(true)
  // total valid status (used for display)
  const [ valid, setValid ] = useState(true)

  useEffect(
    // keep valid in sync (it's always calculated from internal and external values)
    () => {
      setValid(internal && external)
    },
    [ internal, external ]
  )

  return {
    valid,
    internal,
    setInternal,
    setExternal,
  }
}

/**
  Corresponds to a fieldset for datetime, controlled with a year, month, and day field.
**/
export function useDatetime(dateString){
  const year = useDatePart()
  const month = useDatePart()
  const day = useDatePart()
  // validity of the date, computed from validity of each field. used to display validity for the fieldset.
  const [ valid, setValid ] = useState(true)
  // computed representation of the date in a map format
  const [ asMap, setAsMap ] = useState(ymdToDateMap('', '', ''))

  useEffect(
    // keep valid in sync
    () => {
      setValid(year.valid && month.valid && day.valid)
    },
    [ year.valid, month.valid, day.valid ]
  )

  useEffect(
    // validate the date whenever any component of it changes, and update other calculated fields
    () => {
      const errors = isValidDate(year.value, month.value, day.value)
      year.setErrors(errors.year)
      month.setErrors(errors.month)
      day.setErrors(errors.day)
      year.setValidInternal(_.isEmpty(errors.year))
      month.setValidInternal(_.isEmpty(errors.month))
      day.setValidInternal(_.isEmpty(errors.day))
      setAsMap(ymdToDateMap(year.value, month.value, day.value))
    },
    [ year.value, month.value, day.value ]
  )

  const clear = () => {
    // reset all fields to defaults (validity should be updated automatically)
    year.set('')
    month.set('')
    day.set('')
  }

  useEffect(
    // update/reset fields when prop changes
    () => {
      if (dateString != null) {
        let dateObj = moment(dateString).utc()
        year.set(dateObj.year().toString())
        month.set(dateObj.month().toString())
        day.set(dateObj.date().toString())
      }
      else {
        clear()
      }
    },
    [ dateString ]
  )

  return [
    {
      year: year,
      month: month,
      day: day,
      valid: valid,
      asMap: asMap,
      clear: clear,
      setValidExternal: isValid => {
        // when there is something wrong externally (date range problem), mark all fields as invalid since there is no way to know which must be changed to fix the problem
        year.setValidExternal(isValid)
        month.setValidExternal(isValid)
        day.setValidExternal(isValid)
      },
    },
  ]
}

/**
  Corresponds to the start and end date fields, and controls validation of the set.
**/
export function useDateRange(startDateTime, endDateTime){
  const [ start ] = useDatetime(startDateTime)
  const [ end ] = useDatetime(endDateTime)
  // error for the combination of a valid start and end date (basically: is the date range valid)
  const [ errorCumulative, setErrorCumulative ] = useState('') // TODO replace with layered ?
  // computed array of all the errors for the fields
  const [ errors, setErrors ] = useState(new Array())

  useEffect(
    () => {
      // reset external validation each time *any* field changes, since old validation no longer applies
      start.setValidExternal(true)
      end.setValidExternal(true)
      setErrorCumulative('') // TODO end and start date same (ie 2019/12/2 for both) is valid
    },
    [ start.asMap, end.asMap ]
  )

  useEffect(
    // compute the overall error messages
    () => {
      let errors = new Array()
      start.year.errors.forEach((err, index) => {
        errors.push(`start year ${err}`)
      })
      start.month.errors.forEach((err, index) => {
        errors.push(`start month ${err}`)
      })
      start.day.errors.forEach((err, index) => {
        errors.push(`start day ${err}`)
      })
      end.year.errors.forEach((err, index) => {
        errors.push(`end year ${err}`)
      })
      end.month.errors.forEach((err, index) => {
        errors.push(`end month ${err}`)
      })
      end.day.errors.forEach((err, index) => {
        errors.push(`end day ${err}`)
      })

      if (!_.isEmpty(errorCumulative)) {
        errors.push(errorCumulative)
      }
      setErrors(errors)
    },
    [
      start.year.errors,
      start.month.errors,
      start.day.errors,
      end.year.errors,
      end.month.errors,
      end.day.errors,
      errorCumulative,
    ]
  )

  const clear = () => {
    start.clear()
    end.clear()
    setErrorCumulative('')
  }

  const validate = () => {
    if (start.valid && end.valid) {
      let isValid = isValidDateRange(start.asMap, end.asMap)
      if (!isValid) {
        setErrorCumulative('Start date must be before end date.')
      }
      start.setValidExternal(isValid)
      end.setValidExternal(isValid)
      return isValid
    }
    return false
  }

  const asDateStrings = () => {
    // convert (assumed valid) dates into datestrings, required to submit the filter
    let startMap = start.asMap
    let startDateString = !_.every(startMap, _.isNull)
      ? moment(startMap).utc().startOf('day').format()
      : null
    let endMap = end.asMap
    let endDateString = !_.every(endMap, _.isNull)
      ? moment(endMap).utc().startOf('day').format()
      : null

    return [ startDateString, endDateString ]
  }

  return [ start, end, clear, validate, asDateStrings, errors ]
}
