export const UPDATE_GEOMETRY = 'update_geometry'

export const updateGeometry = (geoJSON) => {
  return {
    type: UPDATE_GEOMETRY,
    geoJSON
  }
}
