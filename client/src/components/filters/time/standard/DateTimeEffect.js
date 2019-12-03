import {useState, useEffect} from 'react'

import _ from 'lodash'
import moment from 'moment/moment'
import {
  ymdToDateMap,
  isValidDate,
  isValidDateRange,
} from '../../../../utils/inputUtils'

function useDatePart(){
  const [ value, setValue ] = useState('')
  const [ valid, setValid ] = useState(true) // TODO useLayeredValidation (so fields can be marked invalid when external validation is set)

  useEffect(
    () => {
      if (value == '') {
        setValid(true)
      }
    },
    [ value ]
  )

  return {
    value: value,
    set: setValue,
    valid: valid,
    setValid: setValid,
  }
}

export function useLayeredValidation(){
  const [ internal, setInternal ] = useState(true)
  const [ external, setExternal ] = useState(true) // TODO mark fields as invalid (aria) when this is set?
  const [ valid, setValid ] = useState(true)
  useEffect(
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

export function useDatetime(name, dateString){
  const year = useDatePart()
  const month = useDatePart()
  const day = useDatePart()
  const valid = useLayeredValidation()
  const [ reasons, setReasons ] = useState({
    year: new Array(),
    month: new Array(),
    day: new Array(),
  })
  const [ asMap, setAsMap ] = useState(ymdToDateMap('', '', ''))

  useEffect(
    () => {
      valid.setInternal(year.valid && month.valid && day.valid)
    },
    [ year.valid, month.valid, day.valid ]
  )

  useEffect(
    () => {
      const errors = isValidDate(year.value, month.value, day.value)
      setReasons(errors)
      year.setValid(_.isEmpty(errors.year))
      month.setValid(_.isEmpty(errors.month))
      day.setValid(_.isEmpty(errors.day))
      setAsMap(ymdToDateMap(year.value, month.value, day.value))
    },
    [ year.value, month.value, day.value ]
  )

  const clear = () => {
    year.set('')
    month.set('')
    day.set('')
  }

  useEffect(
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
      valid: valid.valid,
      errors: reasons,
      asMap: asMap,
      clear: clear,
      setValidExternal: valid.setExternal,
    },
  ]
}

export function useDateRange(startDateTime, endDateTime, applyFilter){
  const [ start ] = useDatetime('Start', startDateTime)
  const [ end ] = useDatetime('End', endDateTime)
  const [ reasonCumulative, setReasonCumulative ] = useState('') // TODO replace with layered
  // const [ reasonIndividual, setReasonIndividual ] = useState('')
  const [ errors, setErrors ] = useState(new Array())

  useEffect(
    () => {
      // reset external validation each time *any* field changes, since old validation no longer applies
      start.setValidExternal(true)
      end.setValidExternal(true)
      setReasonCumulative('') // TODO end and start date same (ie 2019/12/2 for both) is valid
    },
    [ start.asMap, end.asMap ]
  )

  useEffect(
    // any time individual field(set)s have errors, build up the overall error message
    () => {
      let errors = new Array()
      start.errors.year.forEach((err, index) => {
        errors.push(`start year ${err}`)
      })
      start.errors.month.forEach((err, index) => {
        errors.push(`start month ${err}`)
      })
      start.errors.day.forEach((err, index) => {
        errors.push(`start day ${err}`)
      })
      end.errors.year.forEach((err, index) => {
        errors.push(`end year ${err}`)
      })
      end.errors.month.forEach((err, index) => {
        errors.push(`end month ${err}`)
      })
      end.errors.day.forEach((err, index) => {
        errors.push(`end day ${err}`)
      })

      // setReasonIndividual(_.join(errors, ', '))
      if (!_.isEmpty(reasonCumulative)) {
        errors.push(reasonCumulative)
      }
      setErrors(errors)
    },
    [ start.errors, end.errors, reasonCumulative ]
  )

  const clear = () => {
    start.clear()
    end.clear()
    // setDateRangeValid(true)
    setReasonCumulative('')
  }

  const validate = () => {
    if (start.valid && end.valid) {
      let isValid = isValidDateRange(start.asMap, end.asMap)
      if (!isValid) {
        setReasonCumulative('Start date must be before end date.')
      }
      start.setValidExternal(isValid)
      end.setValidExternal(isValid)
      return isValid
      // if (isValid) {
      //   let startMap = start.asMap
      //   let startDateString = !_.every(startMap, _.isNull)
      //     ? moment(startMap).utc().startOf('day').format()
      //     : null
      //   let endMap = end.asMap
      //   let endDateString = !_.every(endMap, _.isNull)
      //     ? moment(endMap).utc().startOf('day').format()
      //     : null
      //
      //   applyFilter(startDateString, endDateString)
      // }
    }
    return false
  }

  const asDateStrings = () => {
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

  return [
    start,
    end,
    clear,
    // applyDates,
    validate,
    asDateStrings,
    errors,
  ]
}
