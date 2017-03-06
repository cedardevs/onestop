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

    const geoJson = {
      type: "Feature",
      geometry: convertEnvelopeToPolygon(this.props.geometry)
    }
    this.map = L.map(ReactDOM.findDOMNode(this), {
      layers: [
        L.esri.basemapLayer("Oceans"),
        L.GeoJSON.geometryToLayer(geoJson)
      ],
      zoomControl: false,
      attributionControl: false,
      dragging: false,
      touchZoom: false,
      scrollWheelZoom: false,
      doubleClickZoom: false,
      boxZoom: false,
      tap: false
    })
    this.map.fitWorld()
  }
}

MapThumbnailComponent.propTypes = {
  geometry: PropTypes.object.isRequired
}

export default MapThumbnailComponent
