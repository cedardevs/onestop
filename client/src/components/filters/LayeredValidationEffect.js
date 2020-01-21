import {useState, useEffect} from 'react'

/**
  Keep separate track of field and fieldset errors.
**/
export function useLayeredValidation(){
  // validation of this specific field (in isolation)
  const [ fieldValid, setFieldValid ] = useState(true)
  const [ fieldError, setFieldError ] = useState('')
  // validation of this field as part of the set
  const [ fieldsetValid, setFieldsetValid ] = useState(true)
  const [ fieldsetError, setFieldsetError ] = useState('')
  // computed valid status and errors (used for display)
  const [ valid, setValid ] = useState(true)

  useEffect(
    // keep valid in sync (it's always calculated from internal and external values)
    () => {
      setValid(fieldValid && fieldsetValid)
    },
    [ fieldValid, fieldsetValid ]
  )

  return [
    valid,
    {
      // field
      valid: fieldValid,
      setValid: setFieldValid,
      error: fieldError,
      setError: setFieldError,
    },
    {
      // fieldset
      valid: fieldsetValid,
      setValid: setFieldsetValid,
      error: fieldsetError,
      setError: setFieldsetError,
    },
  ]
}
