import React from 'react'
import ReactDOM from 'react-dom'
import TemporalSearchContainer from './temporal/TemporalSearchContainer'
import MapContainer from './map/MapContainer'
import ToggleDisplay from 'react-toggle-display'
import TextSearchField from './TextSearchField'
import _ from 'lodash'

import FlexRow from '../common/FlexRow'
import Button from '../common/input/Button'

import clock from 'fa/clock-o.svg'
import globe from 'fa/globe.svg'
import times from 'fa/times.svg'
import search from 'fa/search.svg'

const styleMap = {
  position: 'fixed',
  zIndex: '20',
  left: '50%',
  marginTop: '2em',
  marginLeft: '-30%',
  boxShadow: '14px 14px 5px rgba(0,0,0,.7)',
  border: '2px outset $color-primary',
  height: '30em',
  width: '80%',
  maxWidth: '70em',
}

class SearchFields extends React.Component {
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
    this.warningStyle = this.warningStyle.bind(this)
    this.validateAndSubmit = this.validateAndSubmit.bind(this)
    this.state = {
      showMap: false,
      showCalendar: false,
      warning: '',
      hoveringWarningClose: false,
    }
  }

  handleClick(e) {
    const target = e.target || e.srcElement
    this.calendarEvents(target, this.state, this.toggleCalendar)
    this.mapEvents(target, this.state, this.toggleMap)
  }

  handleMouseOverWarningClose = event => {
    this.setState({
      hoveringWarningClose: true,
    })
  }

  handleMouseOutWarningClose = event => {
    this.setState({
      hoveringWarningClose: false,
    })
  }

  calendarEvents(target, {timeComponent, timeButton, showCalendar}, toggle) {
    if (
      showCalendar &&
      !timeComponent.contains(target) &&
      !timeButton.contains(target) &&
      !_.startsWith(target.classList[0], 'rc-calendar')
    ) {
      toggle()
    }
  }

  mapEvents(target, {mapComponent, mapButton, showMap}, toggle) {
    if (
      showMap &&
      !mapComponent.contains(target) &&
      !mapButton.contains(target)
    ) {
      toggle()
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

  componentDidMount() {
    // Get component references for event tracking
    this.setState({
      mapComponent: ReactDOM.findDOMNode(this.mapComponent),
      mapButton: ReactDOM.findDOMNode(this.mapButton),
      timeComponent: ReactDOM.findDOMNode(this.timeComponent),
      timeButton: ReactDOM.findDOMNode(this.timeButton),
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

  warningStyle() {
    if (_.isEmpty(this.state.warning)) {
      return {
        display: 'none',
      }
    }
    else {
      return {
        position: 'absolute',
        top: 'calc(100% + 0.309em)',
        lineHeight: '1.618em',
        fontSize: '1em',
        color: 'white',
        backgroundColor: 'red',
        borderRadius: '1em',
        padding: '0.618em',
      }
    }
  }

  warningCloseStyle() {
    if (this.state.hoveringWarningClose) {
      return {
        cursor: 'pointer',
        fontWeight: 'bold',
      }
    }
    else {
      return {
        cursor: 'pointer',
      }
    }
  }

  validateAndSubmit() {
    let filtersApplied =
      !_.isEmpty(this.props.startDateTime) ||
      !_.isEmpty(this.props.endDateTime) ||
      !_.isEmpty(this.props.geoJSON)
    let trimmedQuery = _.trim(this.props.queryString)
    // Validates query string; assumes temporal & spatial selections (if any) are validated in their respective components
    if (!trimmedQuery && !filtersApplied) {
      this.setState({warning: 'You must enter search criteria.'})
    }
    else if (
      trimmedQuery &&
      (_.startsWith(trimmedQuery, '*') || _.startsWith(trimmedQuery, '?'))
    ) {
      this.setState({
        warning: 'Search query cannot start with asterisk or question mark.',
      })
    }
    else {
      this.setState({warning: ''})
      this.submit()
    }
  }

  render() {
    let styleTimeButton = {marginRight: '0.309em', flexShrink: '0'}
    if (this.props.startDateTime || this.props.endDateTime) {
      styleTimeButton['background'] = '#8967d2'
    }
    const timeButton = (
      <Button
        key="timeButton"
        ref={timeButton => (this.timeButton = timeButton)}
        icon={clock}
        onClick={this.toggleCalendar}
        title={'Filter by Time'}
        style={styleTimeButton}
      />
    )

    let styleMapButton = {marginRight: '0.309em', flexShrink: '0'}
    if (this.props.geoJSON) {
      styleMapButton['background'] = '#8967d2'
    }
    const mapButton = (
      <Button
        key="mapButton"
        ref={mapButton => (this.mapButton = mapButton)}
        icon={globe}
        onClick={this.toggleMap}
        title={'Filter by Location'}
        style={styleMapButton}
      />
    )

    const undoButton = (
      <Button
        key="undoButton"
        icon={times}
        onClick={this.clearSearchParams}
        title={'Reset Search'}
        style={{marginRight: '0.309em', flexShrink: '0'}}
      />
    )

    const searchButton = (
      <Button
        key="searchButton"
        icon={search}
        onClick={this.validateAndSubmit}
        title={'Search'}
        style={{flexShrink: '0'}}
      />
    )

    let searchFieldStyle = null
    if (this.props.home) {
      searchFieldStyle = {
        position: 'relative',
        marginRight: '1em',
        display: 'flex',
        flexWrap: 'wrap',
        justifyContent: 'center',
        alignItems: 'center',
        alignSelf: 'flex-end',
      }
    }
    else {
      searchFieldStyle = {
        position: 'relative',
        marginRight: '1em',
        display: 'flex',
        flexWrap: 'wrap',
        justifyContent: 'flex-start',
        alignItems: 'center',
        alignSelf: 'flex-end',
      }
    }

    const textBoxMargin = this.props.home ? {marginRight: '0.309em'} : null

    return (
      <section style={searchFieldStyle}>
        <div style={this.warningStyle()} role="alert">
          {this.state.warning}{' '}
          <span
            style={this.warningCloseStyle()}
            onClick={this.clearQueryString}
            onMouseOver={this.handleMouseOverWarningClose}
            onMouseOut={this.handleMouseOutWarningClose}
          >
            x
          </span>
        </div>

        <div style={textBoxMargin}>
          <TextSearchField
            onEnterKeyDown={this.validateAndSubmit}
            onChange={this.updateQuery}
            onClear={this.clearQueryString}
            value={this.props.queryString}
          />
        </div>
        <FlexRow
          style={{justifyContent: 'center', marginTop: '0.309em'}}
          items={[ timeButton, mapButton, undoButton, searchButton ]}
        />

        <ToggleDisplay show={this.state.showCalendar}>
          <TemporalSearchContainer
            ref={timeComponent => (this.timeComponent = timeComponent)}
            toggleSelf={this.toggleCalendar}
            calendarVisible={this.state.showCalendar}
          />
        </ToggleDisplay>

        <ToggleDisplay show={this.state.showMap}>
          {/* 'updated' passed to trigger update but is unused*/}
          <MapContainer
            ref={mapComponent => (this.mapComponent = mapComponent)}
            updated={this.state.showMap}
            selection={true}
            features={false}
            style={styleMap}
          />
        </ToggleDisplay>

      </section>
    )
  }
}

export default SearchFields
