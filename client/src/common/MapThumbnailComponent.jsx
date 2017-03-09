import React, { PropTypes } from 'react'
import ReactDOM from 'react-dom'
import L from 'leaflet'
import 'esri-leaflet'
import { convertEnvelopeToPolygon } from '../utils/geoUtils'

class MapThumbnailComponent extends React.Component {
  constructor(props) {
    super(props)

    this.map = undefined // defined by render ref callback
  }

  render() {
    return <div style={{width: '100%', height: '100%'}} ref={() => this.renderMap()}></div>
  }

  renderMap() {
    if (this.map) {
      this.map.remove()
    }

    const geoJsonLayer = L.GeoJSON.geometryToLayer({
      type: "Feature",
      geometry: convertEnvelopeToPolygon(this.props.geometry)
    })
    geoJsonLayer.setStyle({
      color: "red",
      weight: 5,
      opacity: 1
    })

    this.map = L.map(ReactDOM.findDOMNode(this), {
      layers: [
        L.esri.basemapLayer("Oceans"),
        geoJsonLayer
      ],
      maxZoom: 3,
      zoomControl: false,
      attributionControl: false,
      dragging: false,
      touchZoom: false,
      scrollWheelZoom: false,
      doubleClickZoom: false,
      boxZoom: false,
      tap: false
    })
    this.fitMapToResults(geoJsonLayer)
  }

  fitMapToResults(geoJsonLayer) {
    if (this.props.geometry) {
      this.map.fitBounds(geoJsonLayer.getBounds())
    }
    else {
      this.map.fitWorld()
    }
  }
}


MapThumbnailComponent.propTypes = {
  geometry: PropTypes.object.isRequired
}

export default MapThumbnailComponent
