export const NEW_GEOMETRY = 'new_geometry'
export const REMOVE_GEOMETRY = 'remove_geometry'

export const newGeometry = (geoJSON) => {
  return {
    type: NEW_GEOMETRY,
    geoJSON
  }
}

export const removeGeometry = () => {
  return {
    type: REMOVE_GEOMETRY
  }
}
