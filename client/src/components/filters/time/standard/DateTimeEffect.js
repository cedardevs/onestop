import {useState, useEffect} from 'react'

import moment from 'moment/moment'
import {ymdToDateMap, isValidDate} from '../../../../utils/inputUtils'

export function useDatetime(dateString, afterValidate){
  const [ year, setYear ] = useState('')
  const [ month, setMonth ] = useState('')
  const [ day, setDay ] = useState('')
  const [ valid, setValid ] = useState(true)

  const asMap = () => ymdToDateMap(year, month, day)

  useEffect(
    () => {
      setValid(isValidDate(year, month, day))
      afterValidate(asMap()) // TODO doesn't actually make much sense to call this if it's not valid
    },
    [ year, month, day ]
  )

  const clear = () => {
    setYear('')
    setMonth('')
    setDay('')
    setValid(true)
  }

  useEffect(
    () => {
      if (dateString != null) {
        let dateObj = moment(dateString).utc()
        setYear(dateObj.year().toString())
        setMonth(dateObj.month().toString())
        setDay(dateObj.date().toString())
        setValid(true) // TODO I think?
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
      setYear: setYear,
      setMonth: setMonth,
      setDay: setDay,
      asMap: asMap,
      clear: clear,
    },
  ]
}
