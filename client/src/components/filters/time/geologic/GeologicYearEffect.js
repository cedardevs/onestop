import {useState, useEffect} from 'react'

import moment from 'moment/moment'
import {ymdToDateMap, isValidDate} from '../../../../utils/inputUtils'
import {isValidYear, convertYearToCE} from '../../../../utils/inputUtils'

export function useYear(yearInt, format, afterValidate){
  const [ year, setYear ] = useState('') // user entered values (tied to text box)
  const [ CE, setCE ] = useState('') // converted value
  const [ valid, setValid ] = useState(true)

  useEffect(
    () => {
      let yearCE = convertYearToCE(year, format)
      setValid(isValidYear(yearCE))
      setCE(yearCE)
      afterValidate(yearCE)
    },
    [ year ]
  )

  const clear = () => {
    setYear('')
    setValid(true)
  }

  useEffect(
    () => {
      if (yearInt != null) {
        // internal to component, values should be string. expected input is integer
        if (format == 'BP') {
          setYear(convertYearToCE(`${yearInt}`, 'BP'))
        }
        else {
          setYear(`${yearInt}`)
        }
      }
      else {
        setYear('')
      }
    },
    [ yearInt, format ] // when props date / redux store changes, update fields
  )

  return [
    {
      year: year,
      CE: CE,
      valid: valid,
      setYear: setYear,
      clear: clear,
    },
  ]
}
