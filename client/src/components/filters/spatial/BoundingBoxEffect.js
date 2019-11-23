import {useState, useEffect} from 'react'
import {constructBbox, textToNumber} from '../../../utils/inputUtils'

function useCoordinate(name, defaultValue, typeName, limit){
  const [ value, setValue ] = useState(defaultValue)
  const [ valid, setValid ] = useState(true)
  const [ reason, setReason ] = useState('')

  const reset = () => {
    setValue(defaultValue)
  }

  useEffect(
    () => {
      // validate
      if (value == '') {
        setValid(true)
        setReason('')
        return
      }
      const num = textToNumber(value)
      if (num == null) {
        setValid(false)
        setReason(`${name}: Invalid coordinates entered.`)
        return
      }
      if (Math.abs(num) > limit) {
        setValid(false)
        setReason(
          `Invalid coordinates entered. Valid ${typeName} are between -${limit} and ${limit}.`
        )
        return
      }
      setValid(true)
      setReason('')
    },
    [ value ]
  )

  return {
    get: value,
    set: setValue,
    valid: valid,
    reason: reason,
    reset: reset,
    isSet: () => textToNumber(value) != null,
    asNumber: () => textToNumber(value),
  }
}

export function useBoundingBox(bbox){
  const west = useCoordinate('west', '', 'longitude', 180)
  const south = useCoordinate('south', '', 'latitude', 90)
  const east = useCoordinate('east', '', 'longitude', 180)
  const north = useCoordinate('north', '', 'latitude', 90)
  const [ validIndividual, setValidIndividual ] = useState(true)
  const [ validCumulative, setValidCumulative ] = useState(true)
  const [ reasonIndividual, setReasonIndividual ] = useState('')
  const [ reasonCumulative, setReasonCumulative ] = useState('')

  const clear = () => {
    west.reset()
    north.reset()
    east.reset()
    south.reset()
    setValidCumulative(true)
    setReasonCumulative('')
  }

  useEffect(
    () => {
      if (bbox) {
        setValidCumulative(true)
        setReasonCumulative('')
        west.set(bbox.west)
        south.set(bbox.south)
        east.set(bbox.east)
        north.set(bbox.north)
      }
      else {
        clear()
      }
    },
    [ bbox ]
  )

  const validateIndividualFields = () => {
    if (!west.valid || !south.valid || !east.valid || !north.valid) {
      setValidIndividual(false)
      setReasonIndividual(
        west.reason || south.reason || east.reason || north.reason
      )
      return false
    }
    setValidIndividual(true)
    setReasonIndividual('')
    return true
  }

  const validate = () => {
    if (!validIndividual) {
      return false
    }
    if (
      (north.isSet() || south.isSet() || east.isSet() || west.isSet()) &&
      !(north.isSet() && south.isSet() && east.isSet() && west.isSet())
    ) {
      setValidCumulative(false)
      setReasonCumulative(
        'Incomplete coordinates entered. Ensure all four fields are populated.'
      )
      return false
    }
    if (north.isSet() && south.isSet() && east.isSet() && west.isSet()) {
      if (north.asNumber() < south.asNumber()) {
        setValidCumulative(false)
        setReasonCumulative('North is always greater than South.')
        return false
      }

      if (north.asNumber() == south.asNumber()) {
        setValidCumulative(false)
        setReasonCumulative('North cannot be the same as South.')
        return false
      }
      if (east.asNumber() == west.asNumber()) {
        setValidCumulative(false)
        setReasonCumulative('East cannot be the same as West.')
        return false
      }
    }

    // nothing is set, which is valid by default
    setValidCumulative(true)
    setReasonCumulative('')
    return true
  }

  useEffect(
    () => {
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
      asBbox: () => constructBbox(west.get, south.get, east.get, north.get), // constructBbox is responsible for string->num type conversion (although maybe that should me moved internal to this effect, since I have to convert to numbers for validation anyway...) TODO
    },
  ]
}
