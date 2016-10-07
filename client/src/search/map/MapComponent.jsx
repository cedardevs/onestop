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
		this.geoJSON = props.geoJSON
		this.mapDefaults = this.mapDefaults.bind(this)
		this.state = {}
	}

  componentDidMount() {
  	// Build the map defaults. When finished, use them to set the state then set up the map
  	Promise.resolve(this.mapDefaults())
  		.then(state => {
				this.setState(state)
				this.mapSetup()
				})
  }

	mapDefaults() {
		let editableLayers = new L.FeatureGroup()
		const drawStyle = {
			color: "#ffe800",
				weight: 3,
				opacity: 0.65
		}
		return {
			style: {
				color: '#00FF00',
					weight: 3,
					opacity: 0.65
			},
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
  	let { map, drawControl, editableLayers } = this.state
		this.loadDrawEventHandlers()
		map.addControl(drawControl)
		map.addLayer(editableLayers)
		map.fitWorld()
  }

  componentWillReceiveProps() {
  	let { map } = this.state
		map.invalidateSize() // Necessary to redraw map which isn't initially visible
  }

  componentWillUpdate(nextProps){
  	// Add/remove layer on map to reflect store
  	this.updateDrawnLayer(nextProps)
  }

  updateDrawnLayer({geoJSON}) {
  	let { editableLayers, style } = this.state
		if (!geoJSON) {
			if (editableLayers) {
				editableLayers.clearLayers()
			}
		} else {
			// Compare old vs. new layer
			if (editableLayers) {
				const prevGeoJSON = editableLayers.getLayers()[0] ?
					editableLayers.getLayers()[0].toGeoJSON() : null
					if (!prevGeoJSON || prevGeoJSON &&
							!_.isEqual(geoJSON.geometry.coordinates, prevGeoJSON.geometry.coordinates)){
						editableLayers.clearLayers()
						let layer = L.GeoJSON.geometryToLayer(geoJSON, {style})
						editableLayers.addLayer(layer)
					}
			}
		}
  }

  componentWillUnmount() {
    let { map } = this.state
    map.off('click', this.onMapClick)
    map = null
  }

  loadDrawEventHandlers(){
    let { map, editableLayers, geoJSON } = this.state
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

export default MapComponent
