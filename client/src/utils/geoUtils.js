import _ from 'lodash'

export const shiftCoordinate = (coordinate, rotations) => {
  if (rotations === 0) { return coordinate }
  const newX = (coordinate[0] + (360 * rotations))
  return _.concat([newX], _.slice(coordinate, 1))
}

export const shiftCoordinates = (coordinates, rotations) => {
  if (rotations === 0) { return coordinates }
  return _.map(coordinates, coordinate => shiftCoordinate(coordinate, rotations))
}

export const findMaxRotations = (coordinates) => {
  return _.chain(coordinates)
      .map(coordinate => coordinate[0])
      .map(x => Math.trunc(x / 360))
      .maxBy(rotations => Math.abs(rotations))
      .value()
}

export const recenterGeometry = (geometry) => {
  if (geometry.type.toLowerCase() !== "polygon" || geometry.coordinates.length !== 1) {
    throw Error("Can only recenter single-ring polygons")
  }

  const firstRing = geometry.coordinates[0]
  const maxRotations = findMaxRotations(firstRing)
  const newRing = shiftCoordinates(firstRing, -1 * maxRotations)
  const newCoordinates = [newRing]
  return _.assign({}, geometry, {coordinates: newCoordinates})
}

export const convertEnvelopeToPolygon = (geometry) => {
  if (geometry.type.toLowerCase() !== 'envelope') {
    return geometry
  }

  const eCoords = geometry.coordinates
  const pCoords = [
    [
      [eCoords[0][0], eCoords[0][1]],
      [eCoords[0][0], eCoords[1][1]],
      [eCoords[1][0], eCoords[1][1]],
      [eCoords[1][0], eCoords[0][1]]
    ]
  ]
  return {
    type: "Polygon",
    coordinates: pCoords
  }
}
