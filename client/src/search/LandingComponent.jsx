import React from 'react'
import PrimarySearchComponent from './PrimarySearchComponent'
import TemporalContainer from './temporal/TemporalContainer'
import MapContainer from './map/MapContainer'
import ToggleDisplay from 'react-toggle-display'
import styles from './landing.css'


class LandingComponent extends React.Component {
  constructor(props) {
    super(props)
    this.submit = props.submit
    this.updateQuery = props.updateQuery
    this.toggleMap = this.toggleMap.bind(this)
    this.state = {
      showMap: false
    }
  }

  toggleMap() {
    this.state.showMap = !this.state.showMap
    this.forceUpdate()
  }

  render() {
    return <div className={styles.landingComponents}>
        <form className={`pure-form`}>
          <div className={styles.searchFields}>
            <PrimarySearchComponent onEnterKeyDown={this.submit} onChange={this.updateQuery}/>
          </div>
        </form>
        <div className={styles.temporalBox}>
          <TemporalContainer />
        </div>
        <button className={`pure-button ${styles.landingButton}`} onClick={this.toggleMap}>Map</button>
        <ToggleDisplay show={this.state.showMap}>
          <div className={styles.mapContainer}>
            <MapContainer updated={this.state.showMap} />
          </div>
        </ToggleDisplay>
      </div>
  }
}

export default LandingComponent
