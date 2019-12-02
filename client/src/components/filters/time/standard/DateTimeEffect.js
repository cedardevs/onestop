import {useState, useEffect} from 'react'

import _ from 'lodash'
import moment from 'moment/moment'
import {ymdToDateMap, isValidDate} from '../../../../utils/inputUtils'

function useDatePart(){
  const [ value, setValue ] = useState('')
  const [ valid, setValid ] = useState(true)

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

      const errors = isValidDate(
        year.value,
        month.value,
        day.value
      )
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

// export function useDateRange(submitClear) {
//   const [ dateRangeValid, setDateRangeValid ] = useState(true)
//
//   const [ start ] = useDatetime(startDateTime, date => {
//     setWarning('')
//     setDateRangeValid(isValidDateRange(date, end.asMap()))
//   })
//   const [ end ] = useDatetime(endDateTime, date => {
//     setWarning('')
//     setDateRangeValid(isValidDateRange(start.asMap(), date))
//   })
//   const clear = () => {
//     submitClear()
//     start.clear()
//     end.clear()
//     setDateRangeValid(true)
//     setWarning('')
//   }
//
//   const validate = () => {
//     if (!start.valid && !end.valid) return 'Invalid start and end date.'
//     if (!start.valid) return 'Invalid start date.'
//     if (!end.valid) return 'Invalid end date.'
//     if (!dateRangeValid) return 'Invalid date range.'
//   }
// }
