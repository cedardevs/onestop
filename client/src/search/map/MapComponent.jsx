import React from 'react'
import ReactDOM from 'react-dom'
import L from 'leaflet'
import 'leaflet-draw'
import styles from './map.css'

class MapComponent extends React.Component {
    constructor(props) {
        super(props)
        this.handleGeometryUpdate = props.handleGeometryUpdate
    }

    componentDidMount() {
        var self = this
        var map = this.map = L.map(ReactDOM.findDOMNode(this), {
            drawControl: true,
            minZoom: 2,
            maxZoom: 20,
            layers: [
                L.tileLayer(
                    'http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
                    {attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>'})
            ],
            attributionControl: false
        })
        map.on('click', this.onMapClick)
        map.on('draw:created', function (e) {
            let type = e.layerType;
            let layer = e.layer;

            // When a user finishes editing a shape we get that information here
            //editableLayers.addLayer(layer);
            // console.log('draw:created->');
            // console.log(JSON.stringify(layer.toGeoJSON()));
            self.handleGeometryUpdate(layer.toGeoJSON().geometry)
        })
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

    onDrawCreated(e) {

    }

    onMapClick() {
        // Do some wonderful map things...
        console.log("You clicked the map!")
    }

    render() {
        return (
            <div className={styles.mapContainer}></div>
        )
    }
}

export default MapComponent
