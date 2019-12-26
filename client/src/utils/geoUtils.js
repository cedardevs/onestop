import _ from 'lodash'
import {textToNumber} from './inputUtils'

// note: only exported for tests
export const shiftCoordinate = (coordinate, rotations) => {
  if (rotations === 0) {
    return coordinate
  }
  const newX = coordinate[0] + 360 * rotations
  return _.concat([ newX ], _.slice(coordinate, 1))
}

// note: this is a helper function, only exported outside this file for tests
export const shiftCoordinates = (coordinates, rotations) => {
  if (rotations === 0) {
    return coordinates
  }
  return _.map(coordinates, coordinate =>
    shiftCoordinate(coordinate, rotations)
  )
}

// note: only exported for tests
export const findMaxRotations = coordinates => {
  return _.chain(coordinates)
    .map(coordinate => coordinate[0])
    .map(x => (Math.abs(x) <= 180 ? 0 : x))
    .map(x => _.round(x / 360))
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

export const ensureDatelineFriendlyGeometry = geometry => {
  let coords =
    geometry.type.toLowerCase() === 'polygon'
      ? [ convertNegativeLongitudes(geometry.coordinates[0]) ]
      : convertNegativeLongitudes(geometry.coordinates)

  return {
    type: geometry.type,
    coordinates: coords,
  }
}

const convertNegativeLongitudes = coordinates => {
  const crossesDateline = coordinates[0][0] > coordinates[1][0]
  return coordinates.map(
    pair =>
      crossesDateline && pair[0] < 0 && pair[0] > -180
        ? [ pair[0] + 360, pair[1] ]
        : pair
  )
}

const bboxAsCoordinates = (west, south, east, north) => {
  const w = textToNumber(west)
  const s = textToNumber(south)
  const e = textToNumber(east)
  const n = textToNumber(north)

  // Invalid coordinates checks
  if (w == null || e == null || s == null || n == null) {
    return null
  }

  if (n < s) {
    return null
  }

  const ws = [ w, s ] // min x, min y
  const wn = [ w, n ]
  const en = [ e, n ] // max x, max y
  const es = [ e, s ]
  return [ ws, es, en, wn, ws ] // CCW for exterior polygon
}

export const convertBboxToGeoJson = (west, south, east, north) => {
  const coordinates = bboxAsCoordinates(west, south, east, north)
  if (coordinates == null) {
    return null
  }
  return {
    type: 'Feature',
    properties: {},
    geometry: {
      coordinates: [ coordinates ],
      type: 'Polygon',
    },
  }
}

export const convertBboxToQueryGeoJson = (west, south, east, north) => {
  if (west > east) {
    if (west - 360 < -360) {
      return convertBboxToGeoJson(west, south, east + 360, north)
    }
    return convertBboxToGeoJson(west - 360, south, east, north)
  }
  return convertBboxToGeoJson(west, south, east, north)
}

// TODO probably makes more sense to just make it a ring of coords....
export const convertBboxToLeafletGeoJson = (west, south, east, north) => {
  const coordinates = bboxAsCoordinates(west, south, east, north)
  if (coordinates == null) {
    return null
  }

  let datelineFriendlyGeometry = ensureDatelineFriendlyGeometry({
    coordinates: [ coordinates ],
    type: 'Polygon',
  })
  return {
    type: 'Feature',
    properties: {},
    geometry: datelineFriendlyGeometry,
  }
}

// allows the map to update the geometry filters
export const convertGeoJsonToBbox = geometry => {
  let coordinates = geometry.coordinates
  let bbox = null
  if (coordinates) {
    let west = coordinates[0][0][0]
    let east = coordinates[0][2][0]

    // If coords wrap around earth, reset to -180 to 180
    if (Math.abs(west - east) > 360) {
      west = -180
      east = 180
    }

    // If coords cross dateline, they've been shifted and need to be put back to [-180, 180]
    if (west < -180) {
      west += 360
    }

    if (east > 180) {
      east -= 360
    }

    bbox = {
      west: _.round(west, 4),
      south: _.round(coordinates[0][0][1], 4),
      east: _.round(east, 4),
      north: _.round(coordinates[0][2][1], 4),
    }
  }
  return bbox
}

export const displayBboxAsLeafletGeoJSON = bbox => {
  let geojson
  if (bbox) {
    if (bbox.west > bbox.east) {
      geojson = convertBboxToLeafletGeoJson(
        bbox.west,
        bbox.south,
        bbox.east + 360,
        bbox.north
      )
      if (geojson) {
        return {
          type: 'Feature',
          properties: {},
          geometry: recenterGeometry(geojson.geometry),
        }
      }
    }
    return convertBboxToLeafletGeoJson(
      bbox.west,
      bbox.south,
      bbox.east,
      bbox.north
    )
  }
}

export const isPolygonABoundingBox = coordinates => {
  return (
    coordinates[0].length === 5 && // if is bbox
    // 2 distinct latitudes
    new Set(
      coordinates[0].map(it => {
        return it[1]
      })
    ).size == 2 &&
    // 2 distinct longitues
    new Set(
      coordinates[0].map(it => {
        return it[0]
      })
    ).size == 2
  )
}
