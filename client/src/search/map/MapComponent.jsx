import React from 'react'
import ReactDOM from 'react-dom'
import L from 'leaflet'
import 'esri-leaflet'
import 'leaflet-draw'
import _ from 'lodash'
import styles from './map.css'

class MapComponent extends React.Component {
	constructor(props) {
		super(props)
		this.handleNewGeometry = props.handleNewGeometry
		this.removeGeometry = props.removeGeometry
		this.geoJsonSelection = props.geoJsonSelection
		this.geoJsonFeatures = props.geoJsonFeatures
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
        	if (this.props.selection) this.updateDrawnLayer(this.props)
          if (this.props.features) this.updateResultsLayers(this.props)
        })
				this.mapSetup()
			})
  }

	mapDefaults() {
    let resultsLayers = new L.FeatureGroup()
		let editableLayers = new L.FeatureGroup()
		const drawStyle = {
			color: "#ffe800",
				weight: 3,
				opacity: 0.65
		}
		return {
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
      // Define map's draw control with defaults
      drawControl: new L.Control.Draw({
        edit: {
          featureGroup: editableLayers
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
  }

  mapSetup() {
  	let { map, drawControl, editableLayers, resultsLayers } = this.state
		this.loadDrawEventHandlers()
		map.addControl(drawControl)
		map.addLayer(resultsLayers)
		map.addLayer(editableLayers)
		map.fitWorld()
  }

  componentWillReceiveProps() {
  	let { map } = this.state
		map.invalidateSize() // Necessary to redraw map which isn't initially visible
  }

  componentWillUpdate(nextProps){
  	// Add/remove layers on map to reflect store
    if (this.props.selection && this.state._initialized) this.updateDrawnLayer(nextProps)
  }

  updateDrawnLayer({geoJsonSelection}) {
  	let { editableLayers, style } = this.state
		if (!geoJsonSelection) {
			if (editableLayers) {
				editableLayers.clearLayers()
			}
		} else {
			// Compare old vs. new layer
			if (editableLayers) {
				const prevSelection = editableLayers.getLayers()[0] ?
					editableLayers.getLayers()[0].toGeoJSON() : null
					if (!prevSelection || prevSelection &&
							!_.isEqual(geoJsonSelection.geometry.coordinates, prevSelection.geometry.coordinates)){
						editableLayers.clearLayers()
						let layer = L.GeoJSON.geometryToLayer(geoJsonSelection, {style})
						editableLayers.addLayer(layer)
					}
			}
		}
  }

  updateResultsLayers({geoJsonFeatures}) {
    let { resultsLayers, map } = this.state
    geoJsonFeatures.forEach(feature=> resultsLayers.addLayer(L.geoJson(feature)))
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
      <div className={styles.mapContainer}></div>
    )
  }
}

MapComponent.defaultProps= {
  selection: false,
  features: true
}


export default MapComponent
