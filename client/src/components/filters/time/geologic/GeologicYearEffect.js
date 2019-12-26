import {useState, useEffect} from 'react'

import _ from 'lodash'
import moment from 'moment/moment'

import {isValidYear, convertYearToCE} from '../../../../utils/inputUtils'
import {useLayeredValidation} from '../../LayeredValidationEffect'

export function useYear(yearInt, format){
  const [ year, setYear ] = useState('') // user entered values (tied to text box)
  const [ CE, setCE ] = useState('') // converted value
  const [ valid, field, fieldset ] = useLayeredValidation()

  useEffect(
    () => {
      let yearCE = convertYearToCE(year, format)
      let message = isValidYear(yearCE)
      field.setValid(_.isEmpty(message))
      field.setError(message)
      setCE(yearCE)
    },
    [ year ]
  )

  const clear = () => {
    setYear('')
    field.setValid(true)
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
      errors: {
        field: field.error,
        fieldset: fieldset.error,
      },
      setValid: isValid => {
        fieldset.setValid(isValid)
      },
    },
  ]
}
