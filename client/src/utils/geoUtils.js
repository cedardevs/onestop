import _ from 'lodash'
import {textToNumber} from './inputUtils'

export const shiftCoordinate = (coordinate, rotations) => {
  if (rotations === 0) {
    return coordinate
  }
  const newX = coordinate[0] + 360 * rotations
  return _.concat([ newX ], _.slice(coordinate, 1))
}

export const shiftCoordinates = (coordinates, rotations) => {
  if (rotations === 0) {
    return coordinates
  }
  return _.map(coordinates, coordinate =>
    shiftCoordinate(coordinate, rotations)
  )
}

export const findMaxRotations = coordinates => {
  return _.chain(coordinates)
    .map(coordinate => coordinate[0])
    .map(x => Math.trunc(x / 360))
    .maxBy(rotations => Math.abs(rotations))
    .value()
}

export const recenterGeometry = geometry => {
  if (
    geometry.type.toLowerCase() !== 'polygon' ||
    geometry.coordinates.length !== 1
  ) {
    throw Error('Can only recenter single-ring polygons')
  }

  const firstRing = geometry.coordinates[0]
  const maxRotations = findMaxRotations(firstRing)
  const newRing = shiftCoordinates(firstRing, -1 * maxRotations)
  const newCoordinates = [ newRing ]
  return _.assign({}, geometry, {coordinates: newCoordinates})
}

export const ensureDatelineFriendlyPolygon = geometry => {
  let coords
  if (geometry.type.toLowerCase() === 'point') {
    coords = Array(5).fill(geometry.coordinates)
  }
  else {
    coords = geometry.coordinates[0]
  }
  return {
    type: 'Polygon',
    coordinates: [ convertNegativeLongitudes(coords) ],
  }
}

export const convertNegativeLongitudes = coordinates => {
  const crossesDateline = coordinates[0][0] > coordinates[1][0]
  return coordinates.map(
    pair =>
      crossesDateline && pair[0] < 0 && pair[0] > -180
        ? [ pair[0] + 360, pair[1] ]
        : pair
  )
}

export const convertBboxToGeoJson = (west, south, east, north) => {
  const w = textToNumber(west)
  const s = textToNumber(south)
  const e = textToNumber(east)
  const n = textToNumber(north)

  if (!w || !s || !e || !n) {
    return null
  }

  const ws = [ w, s ] // min x, min y
  const wn = [ w, n ]
  const en = [ e, n ] // max x, max y
  const es = [ e, s ]
  const coordinates = [ ws, es, en, wn, ws ] // CCW for exterior polygon
  if (
    !_.every(
      coordinates,
      p => p[0] >= -180 && p[0] <= 180 && p[1] >= -90 && p[1] <= 90
    )
  ) {
    return undefined
  }
  else {
    return {
      type: 'Feature',
      properties: {},
      geometry: {
        coordinates: [ coordinates ],
        type: 'Polygon',
      },
    }
  }
  // return {
  //   type: 'Feature',
  //   properties: {},
  //   geometry: ensureDatelineFriendlyPolygon({coordinates: [ coordinates ], type: 'Polygon'})
  // }
}

export const convertBboxStringToGeoJson = coordString => {
  const coordArray = coordString.split(',').map(x => parseFloat(x))
  return convertBboxToGeoJson(...coordArray)
}

export const convertGeoJsonToBbox = geoJSON => {
  const coordinates =
    geoJSON && geoJSON.geometry && geoJSON.geometry.coordinates
  let bbox = null
  if (coordinates) {
    bbox = {
      west: _.round(coordinates[0][0][0], 4),
      south: _.round(coordinates[0][0][1], 4),
      east: _.round(coordinates[0][2][0], 4),
      north: _.round(coordinates[0][2][1], 4),
    }
  }
  return bbox
}

export const convertGeoJsonToBboxString = geoJSON => {
  const bbox = convertGeoJsonToBbox(geoJSON)
  return bbox ? `${bbox.west},${bbox.south},${bbox.east},${bbox.north}` : ''
}
