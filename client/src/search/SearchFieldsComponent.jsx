import React from 'react'
import ReactDOM from 'react-dom'
import TemporalContainer from './temporal/TemporalContainer'
import MapContainer from './map/MapContainer'
import ToggleDisplay from 'react-toggle-display'
import TextSearchField from './TextSearchFieldComponent'

import styles from '../components/root.css'


class SearchFieldsComponent extends React.Component {
  constructor(props) {
    super(props)
    this.submit = props.submit
    this.updateQuery = props.updateQuery
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
    return<div className={`pure-form `}>
        <div id ="searchbox" className={` ${styles.searchFields}`}>
          <TextSearchField onEnterKeyDown={this.submit} onChange={this.updateQuery}
                           value={this.props.queryString}
                           />
        </div>
      <span>
        <div className={styles.temporalBox}>
          <TemporalContainer />
        </div>
        <button id="mapButton" className={`pure-button ${styles.mapButton} ${styles.landingButton}`}
                   onClick={this.toggleMap}>Map</button>
        <ToggleDisplay show={this.state.showMap}>
          <div className={styles.mapContainer}>
            <span className={styles.mapContent}>
              <MapContainer updated={this.state.showMap} ref='mapComponent' />
            </span>
          </div>
        </ToggleDisplay>
        <button className={`pure-button ${styles.landingButton}`} onClick={this.submit}>Search</button>
      </span>
    </div>
  }
}

export default SearchFieldsComponent