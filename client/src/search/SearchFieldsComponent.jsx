import React from 'react'
import ReactDOM from 'react-dom'
import TemporalContainer from './temporal/TemporalContainer'
import MapContainer from './map/MapContainer'
import ToggleDisplay from 'react-toggle-display'
import TextSearchField from './TextSearchFieldComponent'
import _ from 'lodash'
import clock from 'fa/clock-o.svg'
import globe from 'fa/globe.svg'
import times from 'fa/times.svg'
import search from 'fa/search.svg'

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
    const target = e.target || e.srcElement
    this.calendarEvents(target, this.state, this.toggleCalendar)
    this.mapEvents(target, this.state, this.toggleMap)
  }

  calendarEvents(target, { timeComponent, timeButton, showCalendar }, toggle) {
    if (showCalendar
     && !timeComponent.contains(target)
     && !timeButton.contains(target)
     && !target.classList[0].startsWith('rc-calendar'))
     {
    console.log('toggle')
       toggle() }
  }

  mapEvents(target, { mapComponent, mapButton, showMap }, toggle) {
    if (showMap && !mapComponent.contains(target)
     && !mapButton.contains(target)) { toggle() }
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

  componentDidMount() {
    // Get component references for event tracking
    this.setState({
      mapComponent: ReactDOM.findDOMNode(this.mapComponent),
      mapButton: ReactDOM.findDOMNode(this.mapButton),
      timeComponent: ReactDOM.findDOMNode(this.timeComponent),
      timeButton: ReactDOM.findDOMNode(this.timeButton)
    })

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

    } else if (trimmedQuery && (_.startsWith(trimmedQuery, '*') || _.startsWith(trimmedQuery, '?'))) {
      this.setState({warning: 'Search query cannot start with asterisk or question mark.'})

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
                    onClick={this.toggleCalendar} title="Add Temporal Criteria"
                    ref={timeButton=>this.timeButton=timeButton}>
              <img src={clock} />
            </button>
            <ToggleDisplay show={this.state.showCalendar}>
              <TemporalContainer ref={timeComponent=>this.timeComponent=timeComponent} toggleSelf={this.toggleCalendar}
                calendarVisible={this.state.showCalendar}/>
            </ToggleDisplay>
            <button id="mapButton" className={`pure-button ${this.mapButtonStyle()}`}
                    onClick={this.toggleMap} title="Add Spatial Criteria"
                    ref={mapButton=>this.mapButton=mapButton}>
              <img src={globe} />
            </button>
            <ToggleDisplay show={this.state.showMap}>
              {/* 'updated' passed to trigger update but is unused*/}
              <MapContainer
                  ref={mapComponent=>this.mapComponent=mapComponent}
                  updated={this.state.showMap}
                  selection={true}
                  features={false}
                  style={styles.mapContainer}
              />
            </ToggleDisplay>
            <button className={`pure-button ${styles.undoButton}`}
                    onClick={this.clearSearchParams} title="Clear Search Criteria">
              <img src={times} />
            </button>
            <button className={`pure-button ${styles.searchButton}`} onClick={this.validateAndSubmit} title="Search">
              <img src={search} />
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
