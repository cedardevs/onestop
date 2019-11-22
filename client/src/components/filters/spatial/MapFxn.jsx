import React, {useEffect, useLayoutEffect, useRef, useState} from 'react'
import watch from 'redux-watch'
import store from '../../../store'
import L from 'leaflet'
import * as E from 'esri-leaflet'
import 'leaflet-draw'
import _ from 'lodash'
import {recenterGeometry} from '../../../utils/geoUtils'
import {consolidateStyles} from '../../../utils/styleUtils'

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
    color: '#FFF',
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

const MapFxn = props => {
  const {
    showMap,
    selection,
    geoJsonSelection,
    features,
    filterType,
    geoJSON,
    handleNewGeometry,
    removeGeometry,
    submit,
  } = props

  const containerRef = useRef(null)
  const mapRef = useRef(null)
  const [ initialized, setInitialized ] = useState(false)
  const [ open, setOpen ] = useState(showMap)
  const [ display, setDisplay ] = useState(showMap ? 'block' : 'none')
  const [ maxHeight, setMaxHeight ] = useState(showMap ? 'initial' : '0em')
  const [ flex, setFlex ] = useState(showMap ? '1' : '0')
  const [ width, setWidth ] = useState(showMap ? '100%' : '0%')
  const [ opacity, setOpacity ] = useState(showMap ? '1' : '0')

  const [ resultsLayers, setResultsLayers ] = useState(new L.FeatureGroup())
  const [ editableLayers, setEditableLayers ] = useState(new L.FeatureGroup())
  const [ map, setMap ] = useState(null)

  const resize = () => {
    // do work only if container exists
    if (containerRef.current) {
      // get actual height dynamically, so that when closing, the max height is set correctly to prevent the text from getting taller as it collapses
      const containerRect = containerRef.current.getBoundingClientRect()
      const height = containerRect.height
      setMaxHeight(height)
    }
  }

  const handleTransitionEnd = event => {
    // this ensures the map tiles get loaded properly around the animation
    const property = event.propertyName
    if (property === 'max-height' || property === 'width') {
      if(map) {
        map.invalidateSize()
      }
    }
    if (property === 'max-height') {
      resize()
    }
  }

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
    let w = watch(store.getState, 'search.' + filterType + '.geoJSON')
    store.subscribe(
      w(newGeoJson => {
        editableLayers.clearLayers()
        if (!_.isEmpty(newGeoJson)) {
          let layer = L.geoJson(newGeoJson, {style: null})
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

  const onMapClick = event => {
    console.log('onMapClick::event', event)
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
    map.on('click', onMapClick)
  }

  // this effect replaces UNSAFE_componentWillUpdate / getSnapshotBeforeUpdate
  // because the work done here relies on reading the DOM before the component is re-rendered
  // it is not an exact equivalent, but for what we accomplished before, this should be sufficient
  useLayoutEffect(() => {
    // Add/remove layers on map to reflect store
    if (initialized) {
      if (selection) {
        updateSelectionLayer()
      }
      if (features) {
        updateResultsLayers(nextProps)
        fitMapToResults()
      }
    }
  })

  // map initialization (on mount)
  useEffect(() => {
    // on mount
    if (containerRef.current) {
      containerRef.current.addEventListener(
        'transitionend',
        handleTransitionEnd
      )
    }

    if (geoJsonSelection) {
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

      if (containerRef.current) {
        containerRef.current.removeEventListener(
          'transitionend',
          handleTransitionEnd
        )
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

  // effect for when open status of map changes
  useEffect(
    () => {
      const isDisplayed = display === 'block'
      const shouldClose = open && isDisplayed
      const shouldOpen = !open && !isDisplayed

      if (shouldOpen) {
        setTimeout(() => {
          setMaxHeight('35em')
          setFlex('1')
          setWidth('100%')
          setOpacity('1')
        }, 15)
      }
      if (shouldClose) {
        setTimeout(() => {
          setDisplay('none')
          setOpacity('0')
        }, 500)
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

      setOpen(props.open)

      if (shouldOpen) {
        setDisplay('block')
        setOpacity('0')
      }
      if (shouldClose) {
        setMaxHeight('0em')
        setFlex('0')
        setWidth('0%')
        setOpacity('0')
      }
    },
    [ props.open ]
  )

  const styleMapContainerMerged = consolidateStyles(
    styleMapContainer(open, display, maxHeight, width),
    props.style ? props.style : null
  )

  return (
    <div style={styleMapContainerMerged} ref={containerRef}>
      <div style={styleMapText(open, opacity, flex)}>
        <p>
          Use the square button on the top right of the map to draw a bounding
          box.
        </p>
        <p>
          For accessibility, the Bounding Box form is an alternative to drawing.
        </p>
      </div>
      <div style={styleMap(open, opacity, flex)} ref={mapRef} />
    </div>
  )
}

MapFxn.defaultProps = {
  selection: false,
  features: true,
}

export default MapFxn
