import React from 'react'
import ReactDOM from 'react-dom'
import PrimarySearchComponent from './PrimarySearchComponent'
import TemporalContainer from './temporal/TemporalContainer'
import MapContainer from './map/MapContainer'
import ToggleDisplay from 'react-toggle-display'
import styles from './landing.css'


class LandingComponent extends React.Component {
  constructor(props) {
    super(props)
    this.submit = props.submit
    this.toggleMap = this.toggleMap.bind(this)
    this.handleClick = this.handleClick.bind(this)
    this.state = {
      showMap: false
    }
  }

  handleClick(e) {
    // Close map when user clicks outside of it
    var component = ReactDOM.findDOMNode(this.refs.mapComponent)
    if (this.state.showMap && !component.contains(e.target) && e.srcElement.id !== 'mapButton') {
      this.toggleMap()
    }
  }

  componentWillMount() {
    document.addEventListener('click', this.handleClick, false);
  }

  componentWillUnmount() {
    document.removeEventListener('click', this.handleClick, false);
  }

  toggleMap() {
    this.state.showMap = !this.state.showMap
    this.forceUpdate()
  }

  onClickOut(e) {
    if (this.state.showMap) {
      this.state.showMap = false //Close map when clicked anywhere outside of it
      this.forceUpdate()
    }
  }

  render() {
    return <div className={styles.landingComponents}>
        <form className={`pure-form`}>
          <div className={styles.searchFields}>
            <PrimarySearchComponent onEnterKeyDown={this.submit}/>
          </div>
        </form>
        <div className={styles.temporalBox}>
          <TemporalContainer />
        </div>
        <button id="mapButton" className={`${styles.mapButton}`} onClick={this.toggleMap}></button>
        <ToggleDisplay show={this.state.showMap}>
          <div className={styles.mapContainer}>
            <MapContainer updated={this.state.showMap} ref='mapComponent' />
          </div>
        </ToggleDisplay>
      </div>
  }
}

export default LandingComponent
