import React from 'react'
import ReactDOM from 'react-dom'
import L from 'leaflet'
import 'leaflet-draw'
import styles from './map.css'

class MapComponent extends React.Component {
    constructor(props) {
        super(props)
    }

    componentDidMount() {
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
        map.fitWorld()
    }

    componentWillUnmount() {
        map.off('click', this.onMapClick)
        map = null
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
