import React from 'react'
import ReactDOM from 'react-dom'
import watch from 'redux-watch'
import store from '../../store'
import L from 'leaflet'
import E from 'esri-leaflet'
import 'leaflet-draw'
import _ from 'lodash'
import {recenterGeometry} from '../../utils/geoUtils'

const COLOR_ORANGE = '#FFA268'
const COLOR_GREEN = '#00FFC8'

const MAP_HEIGHT = '400px'

const styleMapContainer = (open, display, maxHeight, width) => {
  return {
    boxSizing: 'border-box',
    backgroundColor: '#3D97D2',
    transition: open // immediate transition
      ? 'max-height .5s 0.0s, width 0.2s 0.2s' // width needs to start opening before max-height completes, or the transitionEnd check will not be able to compute height
      : 'width 0.2s 0.2s, max-height 0.2s 0.4s',
    overflow: 'hidden',
    padding: '0em',
    // properties set on a separate timer using state:
    maxHeight: maxHeight,
    width: width,
    display: display,
  }
}

const styleMapText = (open, opacity, flex) => {
  return {
    zIndex: 1,
    padding: '0.309em 0.618em',
    margin: '0 auto',
    backgroundColor: '#18478F',
    textAlign: 'center',
    transition: open //immediate transition
      ? 'flex 0.2s 0.0s ease-out, opacity 0.2s 0.4s'
      : 'opacity 0.2s 0.0s, flex 0.2s 0.0s ease-out',
    flex: flex,
    // properties set on a separate timer using state:
    opacity: opacity,
  }
}

const styleMap = (open, opacity, flex) => {
  return {
    zIndex: 1,
    padding: 0,
    transition: open //immediate transition
      ? 'flex 0.2s 0.0s ease-out, opacity 0.2s 0.4s'
      : 'opacity 0.2s 0.0s, flex 0.2s 0.0s ease-out',
    flex: flex,
    margin: '0 auto',
    display: 'flex',
    position: 'relative',
    height: MAP_HEIGHT,
    alignItems: 'flex-start',
    maxWidth: '1200px',
    // properties set on a separate timer using state:
    opacity: opacity,
  }
}
const SOUTH_WEST = L.latLng(-90, 5 * -360)
const NORTH_EAST = L.latLng(90, 5 * 360)
const BOUNDS = L.latLngBounds(SOUTH_WEST, NORTH_EAST)

const geoJsonStyle = {
  color: COLOR_GREEN,
  weight: 3,
  opacity: 0.65,
}

const drawStyle = {
  color: COLOR_ORANGE,
  weight: 3,
  opacity: 0.65,
}

class Map extends React.Component {
  constructor(props) {
    super(props)
    const {showMap} = this.props
    this.state = {
      initialized: false,
      open: showMap,
      display: showMap ? 'block' : 'none',
      maxHeight: showMap ? 'initial' : '0em',
      flex: showMap ? '1' : '0',
      width: showMap ? '100%' : '0%',
      opacity: showMap ? '1' : '0',
    }
  }

  handleTransitionEnd = event => {
    // this ensures the map tiles get loaded properly around the animation
    const {map} = this.state
    const property = event.propertyName
    if (property === 'max-height' || property === 'width') {
      map.invalidateSize()
    }
    if (property === 'max-height') {
      this.resize()
    }
  }

  initialState = () => {
    const {geoJsonSelection, features} = this.props

    let resultsLayers = new L.FeatureGroup()
    let editableLayers = new L.FeatureGroup()

    if (geoJsonSelection) {
      let geoJSONLayer = L.geoJson(geoJsonSelection, {style: geoJsonStyle})
      editableLayers.addLayer(geoJSONLayer)
    }

    if (features) {
      this.updateResultsLayers(this.props)
    }

    let initialMapProperties = {
      maxBounds: BOUNDS,
      maxBoundsViscosity: 1.0,
      layers: [ E.basemapLayer('Imagery'), E.basemapLayer('ImageryLabels') ],
      attributionControl: false,
    }

    let state = {
      initialized: true,
      resultsLayers,
      editableLayers,
      // Define map with defaults
      map: L.map(ReactDOM.findDOMNode(this.mapNode), initialMapProperties),
      previousLayer: {},
    }
    return state
  }

  componentDidMount() {
    this.container.addEventListener('transitionend', this.handleTransitionEnd)
    this.setState(this.initialState(), this.mapSetup)
  }

  drawDefaults(layerGroup) {
    return new L.Control.Draw({
      edit: {
        featureGroup: layerGroup,
        edit: false, // edit button
        remove: false, // trash can button
      },
      position: 'topright',
      draw: {
        polyline: false,
        marker: false,
        polygon: false,
        circle: false,
        circlemarker: false,
        rectangle: {
          shapeOptions: drawStyle,
        },
      },
    })
  }

  mapSetup() {
    const {selection, features} = this.props
    let {map, editableLayers, resultsLayers} = this.state
    this.loadDrawEventHandlers()
    if (selection) {
      map.addControl(this.drawDefaults(editableLayers))
      map.addLayer(editableLayers)
    }
    if (features) {
      map.addLayer(resultsLayers)
    }
    this.fitMapToResults()
  }

  componentWillReceiveProps(nextProps) {
    let {map} = this.state
    if (map) {
      map.invalidateSize()
    } // Necessary to redraw map which isn't initially visible

    if (this.props.showMap != nextProps.showMap) {
      this.setState(prevState => {
        const isOpen = prevState.open
        const isDisplayed = prevState.display === 'block'
        const shouldClose = isOpen && isDisplayed
        const shouldOpen = !isOpen && !isDisplayed

        // these transitions do occasionally have timing issues, but I've only seen them when rapidly toggling a single element on and off..
        if (shouldOpen) {
          setTimeout(
            () =>
              this.setState({
                maxHeight: '35em', // default maxHeight to something 'big enough' to allow the transition to complete and compute the real maxHeight
                flex: '1',
                width: '100%',
                opacity: '1',
              }),
            15
          )
        }
        if (shouldClose) {
          setTimeout(() => this.setState({display: 'none', opacity: '0'}), 500)
        }

        const immediateTransition = shouldOpen
          ? {display: 'block', opacity: '0'}
          : shouldClose
            ? {
                maxHeight: '0em',
                flex: '0',
                width: '0%',
                opacity: '0',
              }
            : {}
        return {open: !isOpen, ...immediateTransition}
      })
    }
  }

  componentWillUpdate(nextProps) {
    // Add/remove layers on map to reflect store
    const {selection, features} = this.props
    const {initialized} = this.state
    if (initialized) {
      if (selection) {
        this.updateSelectionLayer()
      }
      if (features) {
        this.updateResultsLayers(nextProps)
        this.fitMapToResults()
      }
    }
  }

  componentDidUpdate(prevProps, prevState) {
    const {map} = this.state
    map.invalidateSize()
  }

  updateSelectionLayer() {
    let {editableLayers, style} = this.state
    let w = watch(store.getState, 'behavior.search.geoJSON')
    store.subscribe(
      w(newGeoJson => {
        editableLayers.clearLayers()
        if (!_.isEmpty(newGeoJson)) {
          let layer = L.geoJson(newGeoJson, {style: style})
          editableLayers.addLayer(layer)
          this.state.map.panTo(layer.getBounds().getCenter())
        }
      })
    )
  }

  updateResultsLayers({geoJsonFeatures, focusedFeatures}) {
    if (!geoJsonFeatures) {
      return
    }
    // Apply colors to focused feature
    let {resultsLayers} = this.state
    const selectedStyle = {color: COLOR_ORANGE}
    const defaultStyle = {
      color: COLOR_GREEN,
      fillOpacity: 0.002,
      opacity: 0.5,
    }
    if (!resultsLayers) {
      return
    }

    resultsLayers.clearLayers()
    geoJsonFeatures.forEach(feature => {
      resultsLayers.addLayer(
        L.geoJson(feature, {
          style: f =>
            focusedFeatures.indexOf(f.properties.id) >= 0
              ? selectedStyle
              : defaultStyle,
        })
      )
    })
  }

  fitMapToResults() {
    const {selection, features} = this.props
    const {map, resultsLayers} = this.state
    const hasResults = resultsLayers && !_.isEmpty(resultsLayers.getLayers())
    if (!selection && features && hasResults) {
      map.fitBounds(resultsLayers.getBounds())
    }
    else {
      map.fitWorld()
    }
  }

  componentWillUnmount() {
    let {map} = this.state
    map.off('click', this.onMapClick)
    map = null

    this.container.removeEventListener(
      'transitionend',
      this.handleTransitionEnd
    )
  }

  resize = () => {
    // do work only if container exists
    if (this.container) {
      // get actual height dynamically, so that when closing, the max height is set correctly to prevent the text from getting taller as it collapses
      const containerRect = this.container.getBoundingClientRect()
      const height = containerRect.height
      this.setState({
        maxHeight: height,
      })
    }
  }

  updateGeometryAndSubmit = newGeoJSON => {
    const {geoJSON, handleNewGeometry, removeGeometry, submit} = this.props
    if (geoJSON || newGeoJSON) {
      if (newGeoJSON) {
        newGeoJSON.geometry.coordinates[0].reverse() // Change coords from CW to CCW
        let adjustedGeoJSON = {
          type: 'Feature',
          properties: {},
          geometry: recenterGeometry(newGeoJSON.geometry),
        }

        handleNewGeometry(adjustedGeoJSON)
      }
      else {
        removeGeometry()
      }
      submit()
    }
  }

  loadDrawEventHandlers() {
    let {map} = this.state
    map.on('draw:drawstart', e => {
      this.updateGeometryAndSubmit()
    })
    map.on('draw:created', e => {
      let newLayer = e.layer.toGeoJSON()
      this.updateGeometryAndSubmit(newLayer)
    })
    map.on('draw:edited', e => {
      let layerModified = e.getLayers()[0].toGeoJSON()
      this.updateGeometryAndSubmit(layerModified)
    })
    map.on('draw:deleted', e => {
      this.updateGeometryAndSubmit()
    })
  }

  render() {
    const {open, display, flex, maxHeight, width, opacity} = this.state

    return (
      <div
        style={styleMapContainer(open, display, maxHeight, width)}
        ref={container => {
          this.container = container
        }}
      >
        <div style={styleMapText(open, opacity, flex)}>
          <p>
            Use the square button on the top right of the map to draw a bounding
            box.
          </p>
          <p>
            If the draw feature does not meet your accessibility needs, text
            entry in the Bounding Box Coordinates form will apply the same
            filter.
          </p>
        </div>
        <div
          style={styleMap(open, opacity, flex)}
          ref={mapNode => {
            this.mapNode = mapNode
          }}
        />
      </div>
    )
  }
}

Map.defaultProps = {
  selection: false,
  features: true,
}

export default Map
