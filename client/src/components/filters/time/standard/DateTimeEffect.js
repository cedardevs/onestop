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

  return {
    value: value,
    set: setValue,
    valid: valid,
    setValid: setValid,
  }
}

export function useDatetime(dateString, afterValidate){
  const year = useDatePart()
  const month = useDatePart()
  const day = useDatePart()
  const [ valid, setValid ] = useState(true)

  const asMap = () => ymdToDateMap(year.value, month.value, day.value)

  useEffect(
    () => {
      setValid(year.valid && month.valid && day.valid)
    },
    [ year.valid, month.valid, day.valid ]
  )

  useEffect(
    () => {
      // setValid(isValidDate(year, month, day))
      const [ yv, mv, dv ] = isValidDate(year.value, month.value, day.value)
      year.setValid(yv)
      month.setValid(mv)
      day.setValid(dv)
      afterValidate(asMap()) // TODO doesn't actually make much sense to call this if it's not valid, since it then validates the date range, but that's what we'd been doing before
    },
    [ year, month, day ]
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
      valid: valid,
      asMap: asMap,
      clear: clear,
    },
  ]
}
