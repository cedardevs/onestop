import React, {useEffect, useLayoutEffect, useRef, useState} from 'react'
import watch from 'redux-watch'
import store from '../../../store'
import L from 'leaflet'
import * as E from 'esri-leaflet'
import 'leaflet-draw'
import _ from 'lodash'
import {
  recenterGeometry,
  convertGeoJsonToBbox,
  displayBboxAsLeafletGeoJSON,
} from '../../../utils/geoUtils'
import {consolidateStyles} from '../../../utils/styleUtils'
import CloseButton from '../../common/ui/CloseButton'
import {Key} from '../../../utils/keyboardUtils'

const COLOR_ORANGE = '#FFA268'
const COLOR_GREEN = '#00FFC8'

const MAP_HEIGHT = '400px'

const MARGIN_EMS = 1.618
const WIDTH_CLOSE_BUTTON = 3

const styleMapContainer = {
  boxSizing: 'border-box',
  backgroundColor: '#3D97D2',
  overflow: 'hidden',
  padding: '0em',
  maxHeight: '35em',
  width: '100%',
}

const styleMapContainerHeading = {
  display: 'flex',
  backgroundColor: '#18478F',
  paddingLeft: `${MARGIN_EMS}em`,
}

const styleMapText = {
  color: '#FFF',
  zIndex: 1,
  padding: '0.309em 0.618em',
  margin: '0 auto',
  textAlign: 'center',
  flexGrow: 1,
  paddingRight: `${MARGIN_EMS + WIDTH_CLOSE_BUTTON}em`,
}

const styleMap = {
  zIndex: 1,
  padding: 0,
  margin: '0 auto',
  display: 'flex',
  position: 'relative',
  height: MAP_HEIGHT,
  alignItems: 'flex-start',
  maxWidth: '1200px',
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

const Map = ({
  style,
  selection,
  bbox,
  features,
  filterType,
  handleNewGeometry,
  removeGeometry,
  submit,
  closeMap,
}) => {
  const containerRef = useRef(null)
  const mapRef = useRef(null)
  const [ initialized, setInitialized ] = useState(false)
  const [ resultsLayers, setResultsLayers ] = useState(new L.FeatureGroup())
  const [ editableLayers, setEditableLayers ] = useState(new L.FeatureGroup())
  const [ map, setMap ] = useState(null)

  const fitMapToResults = () => {
    const hasResults = resultsLayers && !_.isEmpty(resultsLayers.getLayers())
    if (!selection && features && hasResults) {
      map.fitBounds(resultsLayers.getBounds())
    }
    else {
      map.fitWorld()
    }
  }

  const updateSelectionLayer = () => {
    let w = watch(store.getState, 'search.' + filterType + '.bbox')
    store.subscribe(
      w(newBbox => {
        let newGeoJson = displayBboxAsLeafletGeoJSON(newBbox)
        editableLayers.clearLayers()
        if (!_.isEmpty(newGeoJson)) {
          let layer = L.geoJson(newGeoJson, {style: geoJsonStyle})
          editableLayers.addLayer(layer)
          map.panTo(layer.getBounds().getCenter())
        }
      })
    )
  }

  const updateResultsLayers = ({geoJsonFeatures, focusedFeatures}) => {
    if (!geoJsonFeatures) {
      return
    }
    // Apply colors to focused feature
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

  const drawDefaults = layerGroup => {
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

  const updateGeometryAndSubmit = newGeoJSON => {
    if (bbox || newGeoJSON) {
      if (newGeoJSON) {
        newGeoJSON.geometry.coordinates[0].reverse() // Change coords from CW to CCW
        handleNewGeometry(
          convertGeoJsonToBbox(recenterGeometry(newGeoJSON.geometry))
        )
      }
      else {
        removeGeometry()
      }
      submit()
    }
  }

  const loadDrawEventHandlers = () => {
    map.on('draw:drawstart', e => {
      updateGeometryAndSubmit()
    })
    map.on('draw:created', e => {
      let newLayer = e.layer.toGeoJSON()
      updateGeometryAndSubmit(newLayer)
    })
    map.on('draw:edited', e => {
      let layerModified = e.getLayers()[0].toGeoJSON()
      updateGeometryAndSubmit(layerModified)
    })
    map.on('draw:deleted', e => {
      updateGeometryAndSubmit()
    })
  }

  // this effect replaces UNSAFE_componentWillUpdate / getSnapshotBeforeUpdate
  // because the work done here relies on reading the DOM before the component is re-rendered
  // it is not an exact equivalent, but for what we need, this should be sufficient
  useLayoutEffect(() => {
    // Add/remove layers on map to reflect store
    if (initialized) {
      if (selection) {
        updateSelectionLayer()
      }
      if (features) {
        updateResultsLayers(props)
        fitMapToResults()
      }
    }
  })

  // map initialization (on mount)
  useEffect(() => {
    // on mount
    let geoJsonSelection = displayBboxAsLeafletGeoJSON(bbox)
    if (geoJsonSelection) {
      geoJsonSelection.geometry = recenterGeometry(geoJsonSelection.geometry)
      let geoJSONLayer = L.geoJson(geoJsonSelection, {style: geoJsonStyle})
      editableLayers.addLayer(geoJSONLayer)
    }

    if (features) {
      updateResultsLayers(props)
    }

    let initialMapProperties = {
      preferCanvas: true,
      maxBounds: BOUNDS,
      maxBoundsViscosity: 1.0,
      layers: [ E.basemapLayer('Imagery'), E.basemapLayer('ImageryLabels') ],
      attributionControl: false,
    }

    // define map with defaults
    setMap(L.map(mapRef.current, initialMapProperties))

    setInitialized(true)

    return () => {
      // unmount
      if (map) {
        map.off('click', onMapClick)
        setMap(null)
      }
    }
  }, [])

  // map setup (on mount and *after* initialization)
  useEffect(
    () => {
      if (initialized) {
        loadDrawEventHandlers()
        if (selection) {
          map.addControl(drawDefaults(editableLayers))
          map.addLayer(editableLayers)
        }
        if (features) {
          map.addLayer(resultsLayers)
        }
        fitMapToResults()
      }
    },
    [ initialized ]
  )

  // effect for every update, regardless of what changed
  useEffect(() => {
    if (map) {
      map.invalidateSize()
    }
  })

  const handleKeyPressed = event => {
    // do nothing if modifiers are pressed
    if (event.metaKey || event.shiftKey || event.ctrlKey || event.altKey) {
      return
    }
    event.stopPropagation()
    if (event.keyCode === Key.ESCAPE) {
      closeMap()
    }
  }

  const styleMapContainerMerged = consolidateStyles(styleMapContainer, style)

  return (
    <div
      style={styleMapContainerMerged}
      ref={containerRef}
      onKeyUp={handleKeyPressed}
    >
      <div style={styleMapContainerHeading}>
        <CloseButton
          title={'Close Map'}
          onClose={closeMap}
          size={`${WIDTH_CLOSE_BUTTON}em`}
        />
        <div style={styleMapText}>
          <p>
            Use the square button on the top right of the map to draw a bounding
            box.
          </p>
          <p>
            For accessibility, the Bounding Box form is an alternative to
            drawing.
          </p>
        </div>
      </div>
      <div style={styleMap} ref={mapRef} />
    </div>
  )
}

Map.defaultProps = {
  selection: false,
  features: true,
}

export default Map
