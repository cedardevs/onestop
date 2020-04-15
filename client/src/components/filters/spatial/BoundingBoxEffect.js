import {useState, useEffect} from 'react'
import _ from 'lodash'
import {constructBbox, textToNumber} from '../../../utils/inputUtils'
import {useLayeredValidation} from '../LayeredValidationEffect'

function useCoordinate(name, defaultValue, typeName, limit){
  // value is a string, tied to a text input
  const [ value, setValue ] = useState(`${defaultValue}`)
  // numeric is the number representation of value (for validation and submitting filters)
  const [ numeric, setNumeric ] = useState(null)
  // valid status, including controls for display validity, validitiy of this field, and validity as part of a larger fieldset
  const [ valid, field, fieldset ] = useLayeredValidation()

  const reset = () => {
    setValue(`${defaultValue}`)
    // internal validation is automatically updated when the value changes
  }

  useEffect(
    () => {
      // validate when value changes, and update other calculated fields

      // === instead of == makes sure it can't somehow get a int 0 for a value and fail this check
      if (value === '') {
        setNumeric(null)
        field.setValid(true)
        field.setError('')
        return
      }
      const num = textToNumber(value)
      setNumeric(num)

      if (num == null) {
        field.setValid(false)
        field.setError(`${name}: Invalid coordinates entered.`)
        return
      }
      if (Math.abs(num) > limit) {
        field.setValid(false)
        field.setError(
          `${name}: Invalid coordinates entered. Valid ${typeName} coordinates are between -${limit} and ${limit}.`
        )
        return
      }
      field.setValid(true)
      field.setError('')
    },
    [ value ]
  )

  return {
    value: value,
    set: setValue,
    validInternal: field.valid,
    setValidExternal: fieldset.setValid,
    valid: valid,
    error: field.error,
    reset: reset,
    isSet: () => numeric != null,
    number: numeric,
  }
}

export function useBoundingBox(bbox){
  const west = useCoordinate('West', '', 'longitude', 180)
  const south = useCoordinate('South', '', 'latitude', 90)
  const east = useCoordinate('East', '', 'longitude', 180)
  const north = useCoordinate('North', '', 'latitude', 90)
  // track validation problems with both individual fields and combinations of fields to report errors to the user:
  const [ reasonIndividual, setReasonIndividual ] = useState('') // TODO A: use layered, B: tie geometry errors correctly to fields with aria, C: rename 'reason' to 'error', generally speaking
  const [ reasonCumulative, setReasonCumulative ] = useState('')

  const clear = () => {
    west.reset()
    north.reset()
    east.reset()
    south.reset()
    setReasonCumulative('')
  }

  useEffect(
    () => {
      // reset external validation each time *any* field changes, since old validation no longer applies
      east.setValidExternal(true)
      west.setValidExternal(true)
      north.setValidExternal(true)
      south.setValidExternal(true)
      setReasonCumulative('')
    },
    [ east.value, north.value, south.value, west.value ]
  )

  useEffect(
    // update/reset fields when prop changes
    () => {
      if (bbox) {
        setReasonCumulative('')
        // only set string representations of the numbers:
        west.set(`${bbox.west}`)
        south.set(`${bbox.south}`)
        east.set(`${bbox.east}`)
        north.set(`${bbox.north}`)
      }
      else {
        clear()
      }
    },
    [ bbox ]
  )

  const validateIndividualFields = () => {
    if (
      !west.validInternal ||
      !south.validInternal ||
      !east.validInternal ||
      !north.validInternal
    ) {
      setReasonIndividual(
        `${west.error} ${south.error} ${east.error} ${north.error}`
      )
      return false
    }
    setReasonIndividual('')
    return true
  }

  const validate = () => {
    // perform full validation only on submit request, since things being flagged 'invalid' while you are typing in a different field is horrible
    if (!_.isEmpty(reasonIndividual)) {
      return false
    }
    if (!(north.isSet() && south.isSet() && east.isSet() && west.isSet())) {
      setReasonCumulative(
        'Incomplete coordinates entered. Ensure all four fields are populated.'
      )
      // mark whichever fields are not set as invalid:
      if (!north.isSet()) {
        north.setValidExternal(false)
      }
      if (!south.isSet()) {
        south.setValidExternal(false)
      }
      if (!east.isSet()) {
        east.setValidExternal(false)
      }
      if (!west.isSet()) {
        west.setValidExternal(false)
      }
      return false
    }
    if (north.isSet() && south.isSet() && east.isSet() && west.isSet()) {
      if (north.number < south.number) {
        setReasonCumulative('North is always greater than South.')
        north.setValidExternal(false)
        south.setValidExternal(false)
        return false
      }

      if (north.number == south.number) {
        setReasonCumulative('North cannot be the same as South.')
        north.setValidExternal(false)
        south.setValidExternal(false)
        return false
      }
      if (
        east.number == west.number ||
        (west.number == 180 && east.number == -180)
      ) {
        setReasonCumulative('East cannot be the same as West.')
        east.setValidExternal(false)
        west.setValidExternal(false)
        return false
      }
    }

    // nothing is set, which is valid by default
    setReasonCumulative('')
    return true
  }

  useEffect(
    () => {
      // check the individual validation status of each field any time something changes
      validateIndividualFields()
    },
    [ north, south, east, west ]
  )

  return [
    {
      west: west,
      south: south,
      east: east,
      north: north,
      reason: {individual: reasonIndividual, cumulative: reasonCumulative},
      validate: validate,
      clear: clear,
      asBbox: {
        west: west.number,
        south: south.number,
        east: east.number,
        north: north.number,
      },
    },
  ]
}
