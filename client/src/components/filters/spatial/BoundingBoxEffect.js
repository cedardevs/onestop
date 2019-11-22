import {useState, useEffect} from 'react'
import {constructBbox, textToNumber} from '../../../utils/inputUtils'

function useCoordinate(defaultValue, typeName, limit){
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
        setReason('Invalid coordinates entered.')
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
  const west = useCoordinate('', 'longitude', 180)
  const south = useCoordinate('', 'latitude', 90)
  const east = useCoordinate('', 'longitude', 180)
  const north = useCoordinate('', 'latitude', 90)
  const [ valid, setValid ] = useState(true) // for total set of coords
  const [ reason, setReason ] = useState('')

  const clear = () => {
    west.reset()
    north.reset()
    east.reset()
    south.reset()
  }

  useEffect(
    () => {
      if (bbox) {
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

  useEffect(
    () => {
      if (!west.valid || !south.valid || !east.valid || !north.valid) {
        setValid(false)
        setReason(west.reason || south.reason || east.reason || north.reason)
        return
      }

      if (
        (north.isSet() || south.isSet() || east.isSet() || west.isSet()) &&
        !(north.isSet() && south.isSet() && east.isSet() && west.isSet())
      ) {
        setValid(false)
        setReason(
          'Incomplete coordinates entered. Ensure all four fields are populated.'
        )
        return
      }
      if (north.isSet() && south.isSet() && east.isSet() && west.isSet()) {
        if (north.asNumber() < south.asNumber()) {
          setValid(false)
          setReason('North is always greater than South.')
          return
        }

        if (north.asNumber() == south.asNumber()) {
          setValid(false)
          setReason('North cannot be the same as South.')
          return
        }
        if (east.asNumber() == west.asNumber()) {
          setValid(false)
          setReason('East cannot be the same as West.')
          return
        }
      }

      // nothing is set, which is valid by default
      setValid(true)
      setReason('')
    },
    [ north, south, east, west ]
  )

  return [
    {
      west: west,
      south: south,
      east: east,
      north: north,
      valid: valid,
      reason: reason,
      clear: clear,
      asBbox: () => constructBbox(west.get, south.get, east.get, north.get), // constructBbox is responsible for string->num type conversion (although maybe that should me moved internal to this effect, since I have to convert to numbers for validation anyway...) TODO
    },
  ]
}
