export const UPDATE_GEOMETRY = 'update_geometry'

export const updateGeometry = (searchGeometry) => {
  return {
    type: UPDATE_GEOMETRY,
    searchGeometry
  }
}
