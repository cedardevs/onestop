module.exports = {
  // https://developers.arcgis.com/javascript/latest/api-reference/esri-Map.html
  map: {
    initialConditions: () => {
      return {
        basemap: 'hybrid',
      }
    },
  },
  mapView: {
    initialConditions: (container, map) => {
      return {
        center: [ -80, 35 ],
        container: container,
        map: map,
        zoom: 3,
      }
    },
  },
  extent: {
    initialConditions: (origin, point) => {
      return {
        xmin: Math.min(point.x, origin.x),
        xmax: Math.max(point.x, origin.x),
        ymin: Math.min(point.y, origin.y),
        ymax: Math.max(point.y, origin.y),
        spatialReference: {wkid: 102100},
      }
    },
    fillSymbol: () => {
      return {
        type: 'simple-fill', // autocasts as new SimpleFillSymbol()
        color: [ 255, 255, 255, 0.309 ],
        outline: {
          // autocasts as new SimpleLineSymbol()
          color: [ 255, 255, 255 ],
          width: 1,
        },
      }
    },
    textSymbol: text => {
      return {
        type: 'text', // autocasts as new TextSymbol()
        color: 'white',
        haloColor: 'black',
        haloSize: '1px',
        text: text,
        xoffset: 3,
        yoffset: 3,
        font: {
          // autocast as new Font()
          size: 12,
          family: 'sans-serif',
          weight: 'bolder',
        },
      }
    },
    textGeometry: bounds => {
      if (!bounds) {
        return null
      }

      let centerLongitude = (bounds.west + bounds.east) / 2.0
      let centerLatitude = (bounds.north + bounds.south) / 2.0

      if (bounds.west > bounds.east) {
        let positiveEast = bounds.east + 360.0
        centerLongitude = (bounds.west + positiveEast) / 2.0
      }

      return {
        type: 'point',
        longitude: centerLongitude,
        latitude: centerLatitude,
      }
    },
    toBounds: (extent, webMercatorUtils) => {
      // xyToLngLat defaults to normalizing longitude within -180 to +180
      let northWest = webMercatorUtils.xyToLngLat(extent.xmin, extent.ymax)
      let southEast = webMercatorUtils.xyToLngLat(extent.xmax, extent.ymin)
      return {
        north: northWest[1],
        west: northWest[0],
        south: southEast[1],
        east: southEast[0],
      }
    },
  },
}
