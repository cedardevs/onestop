import {useState, useEffect} from 'react'
import {constructBbox} from '../../../utils/geoUtils'

export function useGeoJson(bbox){
  // TODO ok useGeoJson isn't really the right anymore...
  const [ west, setWest ] = useState('')
  const [ south, setSouth ] = useState('')
  const [ east, setEast ] = useState('')
  const [ north, setNorth ] = useState('')

  const clear = () => {
    setWest('')
    setSouth('')
    setEast('')
    setNorth('')
  }
  useEffect(
    () => {
      if (bbox) {
        setWest(bbox.west)
        setSouth(bbox.south)
        setEast(bbox.east)
        setNorth(bbox.north)
      }
      else {
        clear()
      }
    },
    [ bbox ]
  )

  return [
    {
      west: west,
      south: south,
      east: east,
      north: north,
      setWest: setWest,
      setSouth: setSouth,
      setEast: setEast,
      setNorth: setNorth,
      clear: clear,
      asBbox: () => constructBbox(west, south, east, north),
    },
  ]
}
