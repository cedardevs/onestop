import {useState, useEffect} from 'react'
import {
  convertBboxToGeoJson,
  convertGeoJsonToBbox,
} from '../../../utils/geoUtils'

export function useGeoJson(geoJSON){
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
      if (geoJSON) {
        let bbox = convertGeoJsonToBbox(geoJSON)
        setWest(bbox.west)
        setSouth(bbox.south)
        setEast(bbox.east)
        setNorth(bbox.north)
      }
      else {
        clear()
      }
    },
    [ geoJSON ]
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
      asGeoJSON: () => convertBboxToGeoJson(west, south, east, north),
    },
  ]
}
