import React from 'react'
import ReactDOM from 'react-dom'
import TemporalContainer from './temporal/TemporalContainer'
import MapContainer from './map/MapContainer'
import ToggleDisplay from 'react-toggle-display'
import TextSearchField from './TextSearchFieldComponent'

import styles from './searchFields.css'


class SearchFieldsComponent extends React.Component {
  constructor(props) {
    super(props)
    this.submit = props.submit
    this.clearSearch = props.clearSearch
    this.updateQuery = props.updateQuery
    this.handleClick = this.handleClick.bind(this)
    this.handleKeyup = this.handleKeyup.bind(this)
    this.clearQueryString = this.clearQueryString.bind(this)
    this.toggleMap = this.toggleMap.bind(this)
    this.toggleCalendar = this.toggleCalendar.bind(this)
    this.mapButtonStyle = this.mapButtonStyle.bind(this)
    this.timeButtonStyle = this.timeButtonStyle.bind(this)
    this.state = {
      showMap: false,
      showCalendar: false
    }
  }

  handleClick(e) {
    // Close map when user clicks outside of it
    const map = ReactDOM.findDOMNode(this.refs.mapComponent)
    if (this.state.showMap && !map.contains(e.target) && e.target.id !== 'mapButton') {
      this.toggleMap()
    }
  }

  handleKeyup(e) {
    if (e.keyCode === 27) {
      this.setState({showMap: false, showCalendar: false})
    }
  }

  clearQueryString() {
    this.updateQuery('')
  }

  componentWillMount() {
    document.addEventListener('click', this.handleClick, false)
    document.addEventListener('keyup', this.handleKeyup, false)
  }

  componentWillUnmount() {
    document.removeEventListener('click', this.handleClick, false)
    document.removeEventListener('keyup', this.handleKeyup, false)
  }

  toggleMap() {
    this.setState({showMap: !this.state.showMap})
  }

  toggleCalendar() {
    this.setState({showCalendar: !this.state.showCalendar})
  }

  mapButtonStyle() {
    if (this.props.geoJSON) {
      return styles.mapButtonApplied
    }
    else {
      return styles.mapButton
    }
  }

  timeButtonStyle() {
    if (this.props.startDateTime || this.props.endDateTime) {
      return styles.timeButtonApplied
    }
    else {
      return styles.timeButton
    }
  }

  render() {
    return (
        <div className={`pure-form  ${styles.searchFields}`}>
          <div id="searchbox" className={styles.searchContainer}>
            <TextSearchField onEnterKeyDown={this.submit} onChange={this.updateQuery}
                             value={this.props.queryString}/>
          </div>
          <button className={`${styles.clearButton}`} onClick={this.clearQueryString}>x</button>
          <button id="timeButton" className={`pure-button ${this.timeButtonStyle()}`}
                  onClick={this.toggleCalendar} title="Add Temporal Criteria">
            <i className={`${styles.icon} fa fa-clock-o fa-2x`}></i>
          </button>
          <ToggleDisplay show={this.state.showCalendar}>
            <TemporalContainer ref="timeComponent" toggleSelf={this.toggleCalendar} />
          </ToggleDisplay>
          <button id="mapButton" className={`pure-button ${this.mapButtonStyle()}`}
                  onClick={this.toggleMap} title="Add Spatial Criteria">
            <i className={`${styles.icon} fa fa-globe fa-2x`}></i>
          </button>
          <ToggleDisplay show={this.state.showMap}>
            {/* 'updated' passed to trigger update but is unused*/}
            <MapContainer
                ref='mapComponent'
                updated={this.state.showMap}
                selection={true}
                features={false}
                style={styles.mapContainer}
            />
          </ToggleDisplay>
          <button className={`pure-button ${styles.undoButton}`}
                  onClick={this.clearSearch} title="Clear Search Criteria">
            <i className={`${styles.icon} fa fa-times fa-2x`}></i>
          </button>
          <button className={`pure-button ${styles.searchButton}`} onClick={this.submit} title="Search">
            <i className={`${styles.icon} fa fa-search fa-2x`}></i>
          </button>
        </div>
    )
  }
}

export default SearchFieldsComponent
