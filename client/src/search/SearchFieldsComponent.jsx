import React from 'react'
import ReactDOM from 'react-dom'
import TemporalContainer from './temporal/TemporalContainer'
import MapContainer from './map/MapContainer'
import ToggleDisplay from 'react-toggle-display'
import TextSearchField from './TextSearchFieldComponent'
import _ from 'lodash'

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
    this.clearSearchParams = this.clearSearchParams.bind(this)
    this.toggleMap = this.toggleMap.bind(this)
    this.toggleCalendar = this.toggleCalendar.bind(this)
    this.mapButtonStyle = this.mapButtonStyle.bind(this)
    this.timeButtonStyle = this.timeButtonStyle.bind(this)
    this.warningStyle = this.warningStyle.bind(this)
    this.validateAndSubmit = this.validateAndSubmit.bind(this)
    this.state = {
      showMap: false,
      showCalendar: false,
      warning: ''
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
    this.setState({warning: ''})
    this.updateQuery('')
  }

  clearSearchParams() {
    this.setState({warning: ''})
    this.clearSearch()
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

  warningStyle() {
    if (_.isEmpty(this.state.warning)) {
      return styles.hidden
    }
    else {
      return styles.warning
    }

  }


  validateAndSubmit() {
    let filtersApplied = !_.isEmpty(this.props.startDateTime) || !_.isEmpty(this.props.endDateTime) || !_.isEmpty(this.props.geoJSON)
    let trimmedQuery = _.trim(this.props.queryString)
    // Validates query string; assumes temporal & spatial selections (if any) are validated in their respective components
    if (!trimmedQuery && !filtersApplied) {
      this.setState({warning: 'You must enter search criteria.'})

    } else if (trimmedQuery && trimmedQuery === '*') {
      this.setState({warning: 'An asterisk is an invalid search query.'})

    } else {
      this.setState({warning: ''})
      this.submit()
    }
  }

  render() {
    return (
        <div className={`pure-form  ${styles.searchFields}
          ${this.props.header ? styles.header : ''} `}>
          <div className={styles.searchLayout}>
            <div id='searchBox' className={styles.searchContainer}>
              <TextSearchField onEnterKeyDown={this.validateAndSubmit} onChange={this.updateQuery}
                               value={this.props.queryString}/>
            </div>
            <button className={`${styles.clearButton}`} onClick={this.clearQueryString}>x</button>
          </div>

          <div id='searchButtons' className={styles.buttonLayout}>
            <button id="timeButton" className={`pure-button ${this.timeButtonStyle()}`}
                    onClick={this.toggleCalendar} title="Add Temporal Criteria">
              <i className={`${styles.icon} fa fa-clock-o fa-2x`}></i>
            </button>
            <ToggleDisplay show={this.state.showCalendar}>
              <TemporalContainer ref="timeComponent" toggleSelf={this.toggleCalendar}
                calendarVisible={this.state.showCalendar}/>
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
                    onClick={this.clearSearchParams} title="Clear Search Criteria">
              <i className={`${styles.icon} fa fa-times fa-2x`}></i>
            </button>
            <button className={`pure-button ${styles.searchButton}`} onClick={this.validateAndSubmit} title="Search">
              <i className={`${styles.icon} fa fa-search fa-2x`}></i>
            </button>
          </div>
          <div className={`${this.warningStyle()}`} role="alert"><i className="fa fa-warning"
                                                                    aria-hidden="true"></i> {this.state.warning}</div>
        </div>
    )
  }
}

SearchFieldsComponent.defaultProps = {
  header: false
}

export default SearchFieldsComponent
