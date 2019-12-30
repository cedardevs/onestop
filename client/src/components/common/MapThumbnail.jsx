import React from 'react'
import PropTypes from 'prop-types'
import ReactDOM from 'react-dom'
import L from 'leaflet'
import * as E from 'esri-leaflet'
import _ from 'lodash'
import {
  displayLeafletGeometry,
  renderPointAsPolygon,
} from '../../utils/resultUtils'

class MapThumbnail extends React.Component {
  constructor(props) {
    super(props)

    this.map = undefined // defined by render ref callback
  }

  shouldComponentUpdate(nextProps) {
    return !_.isEqual(this.props.geometry, nextProps.geometry)
  }

  render() {
    return (
      <div
        style={{width: '100%', height: '100%'}}
        ref={() => this.renderMap()}
      />
    )
  }

  renderMap() {
    if (this.map) {
      this.map.remove()
    }

    let geoJsonLayer
    let layers = [ E.basemapLayer('Imagery'), E.basemapLayer('ImageryLabels') ]
    if (this.props.geometry) {
      let geometry
      if (this.props.geometry.type.toLowerCase() === 'point') {
        geometry = renderPointAsPolygon(this.props.geometry) // allows use of setStyle, which does not exist for GeoJSON points
      }
      else {
        geometry = this.props.geometry
      }
      geoJsonLayer = L.GeoJSON.geometryToLayer({
        type: 'Feature',
        geometry: geometry,
      })
      geoJsonLayer.setStyle({
        color: 'red',
        weight: 5,
        opacity: 1,
      })
      layers.push(geoJsonLayer)
    }

    this.map = L.map(ReactDOM.findDOMNode(this), {
      preferCanvas: true,
      layers: layers,
      maxZoom: this.props.interactive ? 10 : 3,
      zoomControl: this.props.interactive,
      attributionControl: false,
      dragging: this.props.interactive,
      touchZoom: this.props.interactive,
      scrollWheelZoom: this.props.interactive,
      doubleClickZoom: this.props.interactive,
      boxZoom: this.props.interactive,
      tap: this.props.interactive,
      keyboard: this.props.interactive,
    })
    this.fitMapToResults(geoJsonLayer)
  }

  fitMapToResults(geoJsonLayer) {
    if (this.props.geometry) {
      this.map.fitBounds(geoJsonLayer.getBounds(), {maxZoom: 3})
    }
    else {
      this.map.fitWorld()
    }
  }
}

MapThumbnail.propTypes = {
  geometry: PropTypes.object,
  interactive: PropTypes.bool.isRequired,
}

export default MapThumbnail
