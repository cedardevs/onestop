import {useState, useEffect} from 'react'

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

  return [ value, setValue, valid, setValid ]
}

export function useDatetime(dateString, afterValidate){
  const [ year, setYear, yearValid, setYearValid ] = useDatePart()
  const [ month, setMonth, monthValid, setMonthValid ] = useDatePart()
  const [ day, setDay, dayValid, setDayValid ] = useDatePart()
  const [ valid, setValid ] = useState(true)

  const asMap = () => ymdToDateMap(year, month, day)

  useEffect(
    () => {
      setValid(yearValid && monthValid && dayValid)
    },
    [ yearValid, monthValid, dayValid ]
  )

  useEffect(
    () => {
      // setValid(isValidDate(year, month, day))
      const [ yv, mv, dv ] = isValidDate(year, month, day)
      setYearValid(yv)
      setMonthValid(mv)
      setDayValid(dv)
      afterValidate(asMap()) // TODO doesn't actually make much sense to call this if it's not valid, since it then validates the date range, but that's what we'd been doing before
    },
    [ year, month, day ]
  )

  const clear = () => {
    setYear('')
    setMonth('')
    setDay('')
    // setValid(true)
  }

  useEffect(
    () => {
      if (dateString != null) {
        let dateObj = moment(dateString).utc()
        setYear(dateObj.year().toString())
        setMonth(dateObj.month().toString())
        setDay(dateObj.date().toString())
        // setValid(true) // TODO I think? (we weren't doing this before but it makes sense, given that if we can parse the dateString as a moment, it is valid for usre)
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
      yearValid: yearValid,
      monthValid: monthValid,
      dayValid: dayValid,
      asMap: asMap,
      clear: clear,
    },
  ]
}
