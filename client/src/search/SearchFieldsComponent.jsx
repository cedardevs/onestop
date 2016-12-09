import React from 'react'
import ReactDOM from 'react-dom'
import TemporalContainer from './temporal/TemporalContainer'
import MapContainer from './map/MapContainer'
import ToggleDisplay from 'react-toggle-display'
import TextSearchField from './TextSearchFieldComponent'

import styles from '../root/root.css'


class SearchFieldsComponent extends React.Component {
  constructor(props) {
    super(props)
    this.submit = props.submit
    this.clearSearch = props.clearSearch
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
    if (this.state.showMap && !component.contains(e.target) && e.target.id !== 'mapButton') {
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

  render() {
    return<div className={`pure-form  ${styles.searchFields}`}>
      <div id ="searchbox" className={styles.searchContainer}>
        <TextSearchField onEnterKeyDown={this.submit} onChange={this.updateQuery}
                         value={this.props.queryString}
                         />
      </div>
      <button className={`${styles.clearButton}`} onClick={this.clearSearch}>x</button>
      <div className={styles.temporalContainer}>
         <span className={styles.temporalContent}>
           <TemporalContainer />
         </span>
      </div>
      <button id="mapButton" className={`pure-button ${styles.mapButton}`}
                 onClick={this.toggleMap}>
        <i className={`${styles.globeIcon} fa fa-globe fa-2x`}></i>
      </button>
      <ToggleDisplay show={this.state.showMap}>
        <span className={styles.mapContent}>
          {/* 'updated' passed to trigger update but is unused*/}
          <MapContainer
            ref='mapComponent'
            updated={this.state.showMap}
            selection={true}
            features={false}
            style={styles.mapContainer}
          />
        </span>
      </ToggleDisplay>
      <button className={`${styles.landingButton}`} onClick={this.submit}>
        <i className={'fa fa-search fa-lg'}></i>
      </button>
      <p className={styles.oneStopText}> OneStop </p>
    </div>
  }
}

export default SearchFieldsComponent
