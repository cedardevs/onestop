import React from 'react'
import _ from 'lodash'
import AppliedFilterBubble from './AppliedFilterBubble'
import * as geoUtils from '../../utils/geoUtils'

const Theme = {
  facet: {
    backgroundColor: '#1F4B4D',
    borderColor: '#237D81',
  },
  map: {
    backgroundColor: '#265F35',
    borderColor: '#2B9F4A',
  },
  time: {
    backgroundColor: '#422555',
    borderColor: '#7A2CAB',
  },
  text: {
    backgroundColor: '#0e274e',
    borderColor: '#18478F',
  },
}

const styleWrapper = {
  display: 'flex',
  flexFlow: 'row wrap',
  margin: '1.618em',
  justifyContent: 'center',
}

const styleHidden = {
  display: 'flex',
  flexFlow: 'row wrap',
  margin: '1.618em 0 0 0',
}

export default class AppliedFilters extends React.Component {
  constructor(props) {
    super(props)
  }

  unselectFacetAndSubmitSearch = (category, term) => {
    this.props.toggleFacet(category, term, false)
    this.props.submit()
  }

  unselectDateTimeAndSubmitSearch = (start, end) => {
    this.props.updateDateRange(start, end)
    this.props.submit()
  }

  unselectMapAndSubmitSearch = () => {
    this.props.removeGeometry()
    this.props.submit()
  }

  unselectExcludeGlobal = () => {
    this.props.toggleExcludeGlobal()
    this.props.submit()
  }

  unselectText = () => {
    this.props.clearFilterText()
    this.props.submit()
  }

  buildTextBubbles = () => {
    const {textFilter} = this.props
    let bubbles = []
    if (textFilter) {
      bubbles.push(
        <AppliedFilterBubble
          backgroundColor={Theme.text.backgroundColor}
          borderColor={Theme.text.borderColor}
          text={`Filename Contains: ${textFilter}`}
          key="appliedFilter::textFilter"
          onUnselect={() => this.unselectText()}
        />
      )
    }
    return bubbles
  }

  buildFacetBubbles = () => {
    const {selectedFacets} = this.props
    return _.flatMap(selectedFacets, (terms, category) => {
      return _.map(terms, term => {
        const name = term.split('>').pop().trim() || 'DNE'
        const key = `appliedFilter::${term}`

        return (
          <AppliedFilterBubble
            backgroundColor={Theme.facet.backgroundColor}
            borderColor={Theme.facet.borderColor}
            text={name}
            key={key}
            onUnselect={() => this.unselectFacetAndSubmitSearch(category, term)}
          />
        )
      })
    })
  }

  buildTimeBubbles = () => {
    const {startDateTime, endDateTime} = this.props
    const removeZeroTime = dateTime => dateTime.replace('T00:00:00Z', '')
    let timeBubbles = []
    if (startDateTime) {
      timeBubbles.push(
        <AppliedFilterBubble
          backgroundColor={Theme.time.backgroundColor}
          borderColor={Theme.time.borderColor}
          text={`After: ${removeZeroTime(startDateTime)}`}
          key="appliedFilter::start"
          onUnselect={() =>
            this.unselectDateTimeAndSubmitSearch(null, endDateTime)}
        />
      )
    }
    if (endDateTime) {
      timeBubbles.push(
        <AppliedFilterBubble
          backgroundColor={Theme.time.backgroundColor}
          borderColor={Theme.time.borderColor}
          text={`Before: ${removeZeroTime(endDateTime)}`}
          key="appliedFilter::end"
          onUnselect={() =>
            this.unselectDateTimeAndSubmitSearch(startDateTime, null)}
        />
      )
    }
    return timeBubbles
  }

  buildSpaceBubbles = () => {
    const {geoJSON, excludeGlobal} = this.props
    let spaceBubbles = []
    if (geoJSON && geoJSON.geometry && geoJSON.geometry.coordinates) {
      let bbox = geoUtils.convertGeoJsonToBbox(geoJSON)
      const {west, south, east, north} = bbox
      spaceBubbles.push(
        <AppliedFilterBubble
          backgroundColor={Theme.map.backgroundColor}
          borderColor={Theme.map.borderColor}
          text={`West: ${west}°, South: ${south}°, East: ${east}°, North: ${north}°`}
          key="appliedFilter::boundingBox"
          onUnselect={() => this.unselectMapAndSubmitSearch()}
        />
      )
    }
    if (excludeGlobal) {
      spaceBubbles.push(
        <AppliedFilterBubble
          backgroundColor={Theme.map.backgroundColor}
          borderColor={Theme.map.borderColor}
          text={'Exclude Global Results'}
          key="appliedFilter::excludeGlobal"
          onUnselect={() => this.unselectExcludeGlobal()}
        />
      )
    }
    return spaceBubbles
  }

  render() {
    const {showAppliedFilters} = this.props

    const appliedFilters = [
      ...this.buildTextBubbles(),
      ...this.buildSpaceBubbles(),
      ...this.buildTimeBubbles(),
      ...this.buildFacetBubbles(),
    ]

    if (showAppliedFilters && appliedFilters.length !== 0) {
      return <div style={styleWrapper}>{appliedFilters}</div>
    }
    return <div style={styleHidden} />
  }
}
