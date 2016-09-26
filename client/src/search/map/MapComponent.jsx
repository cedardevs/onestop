import React from 'react'
import ReactDOM from 'react-dom'
import L from 'leaflet'
import 'esri-leaflet'
import 'leaflet-draw'
import styles from './map.css'

class MapComponent extends React.Component {
    constructor(props) {
        super(props)
        this.handleGeometryUpdate = props.handleGeometryUpdate
        this.geoJSON
        if (props.geoJSON){
            this.geoJSON = props.geoJSON.toJS()
        }
        this.lastLayer
    }

    componentDidMount() {
        let editableLayers = new L.FeatureGroup()
        // Reload previous map selection from store
        if (this.geoJSON){
            let layer = this.lastLayer = L.GeoJSON.geometryToLayer(this.geoJSON)
            editableLayers.addLayer(layer)
        }
        let map = this.map = L.map(ReactDOM.findDOMNode(this), {
            minZoom: 2,
            maxZoom: 20,
            layers: [
              L.esri.basemapLayer("Oceans")
            ],
            attributionControl: false
        })
        map.addLayer(editableLayers)
        map.on('draw:created', (e) => {
            let layer = this.lastLayer = e.layer;
            editableLayers.addLayer(layer)
            this.handleGeometryUpdate(layer.toGeoJSON())
        })
        map.on('draw:edited', (e) => {
            this.handleGeometryUpdate(e.layers.getLayers()[0].toGeoJSON())
        })
        map.on('draw:deleted', (e) => {
            editableLayers.removeLayer(this.lastLayer)
            this.handleGeometryUpdate(null)
        })
        map.on('draw:drawstart', (e) => {
            editableLayers.removeLayer(this.lastLayer)
        })

        let shadeOptions = {
            color: '#00FF00',
            weight: 6
        }
        var drawControl = new L.Control.Draw({
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
                    shapeOptions: shadeOptions
                }
            }
        });
        map.addControl(drawControl);
        map.fitWorld()
    }

    componentWillReceiveProps() {
        this.map.invalidateSize()
    }

    componentWillUnmount() {
        var map = this.map
        map.off('click', this.onMapClick)
        map = null
    }

    render() {
        return (
            <div className={styles.mapContainer}></div>
        )
    }
}

export default MapComponent
