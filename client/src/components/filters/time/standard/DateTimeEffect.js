import {useState, useEffect} from 'react'

import moment from 'moment/moment'
import {ymdToDateMap, isValidDate} from '../../../../utils/inputUtils'

export function useDatetime(onDateChange){
  const [ year, setYear] = useState('')
  const [ month, setMonth] = useState('')
  const [ day, setDay] = useState('')
  const [ valid, setValid] = useState(true)

  const toMap = () => ymdToDateMap(year, month, day)

  useEffect(
    () => {
      setValid(isValidDate(year, month, day))
      onDateChange(toMap()) // setWarning, validate range
    },
    [ year, month, day ]
  )

  const clear = () => {
    setYear('') ; setMonth(''); setDay(''); setValid(true) // duplicate clear
  }
  const initFromString = (str) => {
    if (str != null) {
      let dateObj = moment(str).utc()
      setYear (dateObj.year().toString())
      setMonth (dateObj.month().toString())
      setDay (dateObj.date().toString())
      setValid(true) // TODO I think?
    }
    else {
      clear()
    }
  }

  return [initFromString, clear, toMap, {year: year, month: month, day:day, valid: valid}, {year: setYear, month: setMonth, day: setDay}]
}
