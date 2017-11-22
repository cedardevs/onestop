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

// import styles from './searchFields.css'
const styles = {}

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
    this.mapButtonStyle = this.mapButtonStyle.bind(this)
    this.timeButtonStyle = this.timeButtonStyle.bind(this)
    this.warningStyle = this.warningStyle.bind(this)
    this.validateAndSubmit = this.validateAndSubmit.bind(this)
    this.state = {
      showMap: false,
      showCalendar: false,
      warning: '',
    }
  }

  handleClick(e) {
    const target = e.target || e.srcElement
    this.calendarEvents(target, this.state, this.toggleCalendar)
    this.mapEvents(target, this.state, this.toggleMap)
  }

  calendarEvents(target, { timeComponent, timeButton, showCalendar }, toggle) {
    if (
      showCalendar &&
      !timeComponent.contains(target) &&
      !timeButton.contains(target) &&
      !target.classList[0].startsWith('rc-calendar')
    ) {
      console.log('toggle')
      toggle()
    }
  }

  mapEvents(target, { mapComponent, mapButton, showMap }, toggle) {
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
      this.setState({ showMap: false, showCalendar: false })
    }
  }

  clearQueryString() {
    this.setState({ warning: '' })
    this.updateQuery('')
  }

  clearSearchParams() {
    this.setState({ warning: '' })
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
    this.setState({ showMap: !this.state.showMap })
  }

  toggleCalendar() {
    this.setState({ showCalendar: !this.state.showCalendar })
  }

  mapButtonStyle() {
    if (this.props.geoJSON) {
      return styles.mapButtonApplied
    } else {
      return styles.mapButton
    }
  }

  timeButtonStyle() {
    if (this.props.startDateTime || this.props.endDateTime) {
      return styles.timeButtonApplied
    } else {
      return styles.timeButton
    }
  }

  warningStyle() {
    if (_.isEmpty(this.state.warning)) {
      return styles.hidden
    } else {
      return styles.warning
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
      this.setState({ warning: 'You must enter search criteria.' })
    } else if (
      trimmedQuery &&
      (_.startsWith(trimmedQuery, '*') || _.startsWith(trimmedQuery, '?'))
    ) {
      this.setState({
        warning: 'Search query cannot start with asterisk or question mark.',
      })
    } else {
      this.setState({ warning: '' })
      this.submit()
    }
  }

  render() {
    const timeButton = (
      <Button
        icon={clock}
        onClick={this.toggleCalendar}
        ariaLabel={'Add Temporal Criteria'}
        style={{marginRight: "2px"}}
      />
    )

    const mapButton = (
      <Button
        icon={globe}
        onClick={this.toggleMap}
        ariaLabel={'Add Spatial Criteria'}
        style={{marginRight: "2px"}}
      />
    )

    const undoButton = (
      <Button
        icon={times}
        onClick={this.clearSearchParams}
        ariaLabel={'Clear Search Criteria'}
        style={{marginRight: "2px"}}
      />
    )

    const searchButton = (
      <Button
        icon={search}
        onClick={this.validateAndSubmit}
        ariaLabel={'Submit Search'}
      />
    )

    return (
      <section style={{marginRight:"1em"}}>
        <TextSearchField
          onEnterKeyDown={this.validateAndSubmit}
          onChange={this.updateQuery}
          onClear={this.clearQueryString}
          value={this.props.queryString}
        />
        <div>
          <FlexRow
            style={{ justifyContent: 'center', marginTop: '0.309em' }}
            items={[timeButton, mapButton, undoButton, searchButton]}
          />
        </div>
      </section>
    )
  }
}

SearchFields.defaultProps = {
  header: false,
}

export default SearchFields
