import React from 'react'
import PropTypes from 'prop-types'
import ReactDOM from 'react-dom'
import L from 'leaflet'
import 'esri-leaflet'
import _ from 'lodash'
import { ensureDatelineFriendlyPolygon } from '../utils/geoUtils'

class MapThumbnailComponent extends React.Component {
  constructor(props) {
    super(props)

    this.map = undefined // defined by render ref callback
  }

  shouldComponentUpdate(nextProps) {
    return !_.isEqual(this.props.geometry, nextProps.geometry)
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
      geometry: ensureDatelineFriendlyPolygon(this.props.geometry) // allows use of setStyle, which does not exist for GeoJSON points
    })
    geoJsonLayer.setStyle({
      color: "red",
      weight: 5,
      opacity: 1
    })

    this.map = L.map(ReactDOM.findDOMNode(this), {
      layers: [
        L.esri.basemapLayer("Imagery"),
        L.esri.basemapLayer("ImageryLabels"),
        geoJsonLayer
      ],
      maxZoom: this.props.interactive ? 10 : 3,
      zoomControl: this.props.interactive,
      attributionControl: false,
      dragging: this.props.interactive,
      touchZoom: this.props.interactive,
      scrollWheelZoom: this.props.interactive,
      doubleClickZoom: this.props.interactive,
      boxZoom: this.props.interactive,
      tap: this.props.interactive
    })
    this.fitMapToResults(geoJsonLayer)
  }

  fitMapToResults(geoJsonLayer) {
    if (this.props.geometry) {
      this.map.fitBounds(geoJsonLayer.getBounds(), { maxZoom: 3 })
    }
    else {
      this.map.fitWorld()
    }
  }
}


MapThumbnailComponent.propTypes = {
  geometry: PropTypes.object.isRequired,
  interactive: PropTypes.bool.isRequired
}

export default MapThumbnailComponent
