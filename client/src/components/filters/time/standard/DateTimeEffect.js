import {useState, useEffect} from 'react'
import {useLayeredValidation} from '../../LayeredValidationEffect'

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
  // mark fields as required dynamically (ex: year is required when month is set)
  const [ required, setRequired ] = useState(false)
  // valid status, including controls for display validity, validitiy of this field, and validity as part of a larger fieldset
  const [ valid, field, fieldset ] = useLayeredValidation()

  useEffect(
    // reset the validity and error messages when the field is reset to a blank value
    () => {
      if (value == '') {
        field.setValid(true)
        field.setError('')
      }
    },
    [ value ]
  )

  return {
    value: value,
    set: setValue,

    valid: valid,
    required: required,

    setRequired: required => {
      setRequired(required)
      fieldset.setValid(true) // setRequired is called as value is changed, so clear valid and error...
      fieldset.setError('')
    },
    errors: {
      field: field.error,
      fieldset: fieldset.error,
      setField: error => {
        field.setValid(_.isEmpty(error))
        field.setError(error)
      },
      setFieldset: error => {
        fieldset.setValid(_.isEmpty(error))
        fieldset.setError(error)
      },

      setInvalidRange: () => {
        fieldset.setValid(false)
      },
      clearFieldsetValidity: () => {
        fieldset.setValid(true)
      },
    },
  }
}

/**
  Corresponds to a fieldset for datetime, controlled with a year, month, and day field.
**/
export function useDatetime(name, dateString){
  const year = useDatePart()
  const month = useDatePart()
  const day = useDatePart()
  const time = useDatePart()
  // validity of the date, computed from validity of each field. used to display validity for the fieldset.
  const [ valid, setValid ] = useState(true)
  // computed representation of the date in a map format
  const [ asMap, setAsMap ] = useState(ymdToDateMap('', '', '', ''))

  useEffect(
    // keep valid in sync
    () => {
      setValid(year.valid && month.valid && day.valid && time.valid)
    },
    [ year.valid, month.valid, day.valid, time.valid ]
  )

  useEffect(
    // validate the date whenever any component of it changes, and update other calculated fields
    () => {
      const errors = isValidDate(year.value, month.value, day.value, time.value)

      year.errors.setField(
        _.isEmpty(errors.year.field) ? '' : `${name} year ${errors.year.field}.`
      )
      year.setRequired(errors.year.required)
      month.errors.setField(
        _.isEmpty(errors.month.field)
          ? ''
          : `${name} month ${errors.month.field}.`
      )
      month.setRequired(errors.month.required)
      day.errors.setField(
        _.isEmpty(errors.day.field) ? '' : `${name} day ${errors.day.field}.`
      )
      day.setRequired(errors.day.required)
      time.errors.setField(
        _.isEmpty(errors.time.field) ? '' : `${name} time ${errors.time.field}.`
      )
      time.setRequired(errors.time.required)

      setAsMap(ymdToDateMap(year.value, month.value, day.value, time.value))
    },
    [ year.value, month.value, day.value, time.value ]
  )

  const clear = () => {
    // reset all fields to defaults (validity should be updated automatically)
    year.set('')
    month.set('')
    day.set('')
    time.set('')
  }

  useEffect(
    // update/reset fields when prop changes
    () => {
      if (dateString != null) {
        let dateObj = moment.utc(dateString)
        year.set(dateObj.year().toString())
        month.set(dateObj.month().toString())
        day.set(dateObj.date().toString())
        time.set(
          `${dateObj
            .hour()
            .toString()
            .padStart(2, '0')}:${dateObj
            .minute()
            .toString()
            .padStart(2, '0')}:${dateObj.second().toString().padStart(2, '0')}`
        )
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
      time: time,
      valid: valid,
      asMap: asMap,
      clear: clear,
      validate: () => {
        let isValid = true
        if (year.required && _.isEmpty(year.value)) {
          year.errors.setFieldset(`${name} year required.`)
          isValid = false
        }
        if (month.required && _.isEmpty(month.value)) {
          month.errors.setFieldset(`${name} month required.`)
          isValid = false
        }
        return isValid
      },
      setValidDaterange: isValid => {
        // when there is something wrong externally (date range problem), mark all fields as invalid since there is no way to know which must be changed to fix the problem
        if (isValid) {
          year.errors.clearFieldsetValidity()
          month.errors.clearFieldsetValidity()
          day.errors.clearFieldsetValidity()
          time.errors.clearFieldsetValidity()
        }
        else {
          year.errors.setInvalidRange()
          month.errors.setInvalidRange()
          day.errors.setInvalidRange()
          time.errors.setInvalidRange()
        }
      },
    },
  ]
}

/**
  Corresponds to the start and end date fields, and controls validation of the set.
**/
export function useDateRange(startDateTime, endDateTime){
  const [ start ] = useDatetime('Start', startDateTime)
  const [ end ] = useDatetime('End', endDateTime)
  // error for the combination of a valid start and end date (basically: is the date range valid)
  const [ errorCumulative, setErrorCumulative ] = useState('') // TODO replace with layered ?

  useEffect(
    () => {
      // reset external validation each time *any* field changes, since old validation no longer applies
      start.setValidDaterange(true)
      end.setValidDaterange(true)
      setErrorCumulative('') // TODO end and start date same (ie 2019/12/2 for both) is valid
    },
    [ start.asMap, end.asMap ]
  )

  const clear = () => {
    start.clear()
    end.clear()
    setErrorCumulative('')
  }

  const validate = () => {
    if (start.valid && end.valid && start.validate() && end.validate()) {
      let isValid = isValidDateRange(start.asMap, end.asMap)
      if (!isValid) {
        setErrorCumulative('Start date must be before end date.')
      }
      start.setValidDaterange(isValid)
      end.setValidDaterange(isValid)
      return isValid
    }
    return false
  }

  const asDateStrings = () => {
    // convert (assumed valid) dates into datestrings, required to submit the filter
    let startMap = start.asMap
    let startDateString = !_.every(startMap, _.isNull)
      ? moment.utc(startMap).startOf('second').format()
      : null
    let endMap = end.asMap
    let endDateString = !_.every(endMap, _.isNull)
      ? moment.utc(endMap).startOf('second').format()
      : null

    return [ startDateString, endDateString ]
  }

  return [ start, end, clear, validate, asDateStrings, errorCumulative ]
}
