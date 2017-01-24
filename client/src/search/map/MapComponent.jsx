import React from 'react'
import ReactDOM from 'react-dom'
import watch from 'redux-watch'
import store from '../../store'
import L from 'leaflet'
import 'esri-leaflet'
import 'leaflet-draw'
import _ from 'lodash'

class MapComponent extends React.Component {
	constructor(props) {
		super(props)
		this.handleNewGeometry = props.handleNewGeometry
		this.removeGeometry = props.removeGeometry
		this.geoJsonSelection = props.geoJsonSelection
		this.geoJsonFeatures = props.geoJsonFeatures
    this.focusedFeatures = props.focusedFeatures
    this.style = props.style
		this.mapDefaults = this.mapDefaults.bind(this)
		this.state = {
      _initialized: false
    }
	}

  componentDidMount() {
  	// Build the map defaults. When finished, use them to set the state then set up the map
  	Promise.resolve(this.mapDefaults())
  		.then(state => {
				this.setState(state, ()=> {
          let { geoJsonSelection } = this.props
          if (geoJsonSelection) {
            let { editableLayers, style } = this.state
            let layer = L.GeoJSON.geometryToLayer(geoJsonSelection, {style})
            editableLayers.addLayer(layer)
          }
          if (this.props.features) { this.updateResultsLayers(this.props) }
        })
				this.mapSetup()
			})
  }

	mapDefaults() {
    let resultsLayers = new L.FeatureGroup()
		let editableLayers = new L.FeatureGroup()
		let mapSettings = {
      _initialized: true,
			style: {
				color: '#00FF00',
        weight: 3,
				opacity: 0.65
			},
			resultsLayers,
      editableLayers,
      // Define map with defaults
      map: L.map(ReactDOM.findDOMNode(this), {
        minZoom: 2,
        maxZoom: 20,
        layers: [
            L.esri.basemapLayer("Oceans"),
            L.esri.basemapLayer("OceansLabels")
        ],
        attributionControl: false
      }),
      previousLayer: {},
    }
    return mapSettings
  }

  drawDefaults(layerGroup){
		const drawStyle = { color: "#ffe800",
      weight: 3,
			opacity: 0.65
		}
    return new L.Control.Draw({
      edit: {
        featureGroup: layerGroup
      },
      remove: true,
      position: 'topright',
      draw: {
        polyline: false,
        marker: false,
        polygon: false,
        circle: false,
        rectangle: {
          shapeOptions: drawStyle
        }
      }
    })
  }

  mapSetup() {
  	let { map, drawControl, editableLayers, resultsLayers } = this.state
		this.loadDrawEventHandlers()
		if (this.props.selection) {
      map.addControl(this.drawDefaults(editableLayers))
      map.addLayer(editableLayers)
    }
		if (this.props.features) {
      map.addLayer(resultsLayers)
    }
    if (!this.props.selection && this.props.features){
  		map.fitBounds(resultsLayers.getBounds())
    } else {
  		map.fitWorld()
    }
  }

  componentWillReceiveProps() {
  	let { map } = this.state
		map.invalidateSize() // Necessary to redraw map which isn't initially visible
  }

  componentWillUpdate(nextProps) {
  	// Add/remove layers on map to reflect store
    if (this.state._initialized) {
      if (this.props.selection) { this.updateSelectionLayer() }
      if (this.props.features) { this.updateResultsLayers(nextProps) }
    }
  }

  updateSelectionLayer() {
  	let { editableLayers, style } = this.state
		let w = watch(store.getState, 'searchAndFacets.search.geometry.geoJSON')
		store.subscribe(w((newGeoJson) => {
      editableLayers.clearLayers()
      if (!_.isEmpty(newGeoJson)){
        let layer = L.GeoJSON.geometryToLayer(newGeoJson, {style})
        editableLayers.addLayer(layer)
      }
		}))
  }

  updateResultsLayers({geoJsonFeatures, focusedFeatures}) {
		// Apply colors to focused feature
    let { resultsLayers } = this.state
    const selectedStyle = {color: '#f9c642'}
    resultsLayers.clearLayers()
    geoJsonFeatures.forEach(feature => {
      resultsLayers.addLayer(L.geoJson(feature, {
        style: (f) => focusedFeatures.indexOf(f.properties.id) >= 0 ? selectedStyle : {}
      }))
    })
    this.geoJsonFeatures = geoJsonFeatures
    this.focusedFeatures = focusedFeatures
  }

  componentWillUnmount() {
    let { map } = this.state
    map.off('click', this.onMapClick)
    map = null
  }

  loadDrawEventHandlers(){
    let { map } = this.state
    map.on('draw:drawstart', (e) => {
      this.removeGeometry()
    })
    map.on('draw:created', (e) => {
      let newLayer = e.layer.toGeoJSON()
      this.handleNewGeometry(newLayer)
    })
    map.on('draw:edited', (e) => {
      let layerModified = e.getLayers()[0].toGeoJSON()
      this.handleNewGeometry(layerModified)
    })
    map.on('draw:deleted', (e) => {
      this.removeGeometry()
    })
  }

  render() {
    return (
      <div className={this.style}></div>
    )
  }
}

MapComponent.defaultProps = {
  selection: false,
  features: true
}


export default MapComponent
