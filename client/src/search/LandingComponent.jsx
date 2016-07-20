import React from 'react'
import TextSearchField from './TextSearchFieldComponent'
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
            <TextSearchField onEnterKeyDown={this.submit} onChange={this.updateQuery} value={this.props.queryString}/>
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
        <button className={`pure-button ${styles.landingButton} ${styles.searchButton}`} onClick={this.submit}>Search</button>
      </div>
  }
}

export default LandingComponent
