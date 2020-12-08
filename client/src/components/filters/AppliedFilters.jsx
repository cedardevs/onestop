import React from 'react'
import _ from 'lodash'
import AppliedFilterBubble from './AppliedFilterBubble'
import * as geoUtils from '../../utils/geoUtils'
import {displayBigYears} from '../../utils/readableUtils'

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

  unselectYearAndSubmitSearch = (start, end) => {
    this.props.updateYearRange(start, end)
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

  // updateTimeRelation = relation => {
  //   this.props.updateTimeRelation(relation)
  //   this.props.submit()
  // }
  //
  // updateGeoRelation = relation => {
  //   this.props.updateGeoRelation(relation)
  //   this.props.submit()
  // }

  buildTextBubbles = allowRemoveFilter => {
    const {textFilter, allTermsMustMatch} = this.props
    let bubbles = []
    if (textFilter) {
      let qualifier = allTermsMustMatch ? 'All' : 'Any'
      bubbles.push(
        <AppliedFilterBubble
          allowRemoveFilter={allowRemoveFilter}
          backgroundColor={Theme.text.backgroundColor}
          borderColor={Theme.text.borderColor}
          text={`Name Matches ${qualifier}: ${textFilter}`}
          key="appliedFilter::textFilter"
          onUnselect={() => this.unselectText()}
        />
      )
    }
    return bubbles
  }

  buildFacetBubbles = allowRemoveFilter => {
    const {selectedFacets} = this.props
    return _.flatMap(selectedFacets, (terms, category) => {
      return _.map(terms, term => {
        const name = term.split('>').pop().trim() || (
          <abbr title="Does Not Exist">DNE</abbr>
        ) // TODO verify the title is handled correctly for these see search for title: "NOAA Aircraft Operations Center (AOC) Flight Level Data"
        const title = term.split('>').pop().trim()
          ? null
          : 'Remove Does Not Exist Filter'
        const key = `appliedFilter::${term}`

        return (
          <AppliedFilterBubble
            allowRemoveFilter={allowRemoveFilter}
            backgroundColor={Theme.facet.backgroundColor}
            borderColor={Theme.facet.borderColor}
            text={name}
            title={title}
            key={key}
            onUnselect={() => this.unselectFacetAndSubmitSearch(category, term)}
          />
        )
      })
    })
  }

  buildTimeBubbles = allowRemoveFilter => {
    const {
      startDateTime,
      endDateTime,
      startYear,
      endYear,
      timeRelationship,
    } = this.props
    const removeZeroTime = dateTime => {
      // This removes '00:00:00' and 'Z' and adds a space between date and time
      const dateParts = dateTime.split('T')
      const strippedTime = dateParts[1].replace(/00:00:00|Z/g, '')
      return dateParts[0] + ' ' + strippedTime
    }
    let timeBubbles = []
    if (startDateTime) {
      timeBubbles.push(
        <AppliedFilterBubble
          allowRemoveFilter={allowRemoveFilter}
          backgroundColor={Theme.time.backgroundColor}
          borderColor={Theme.time.borderColor}
          text={`${_.capitalize(timeRelationship)}${timeRelationship ==
          'disjoint'
            ? ' From'
            : ''} Date After: ${removeZeroTime(startDateTime)}`}
          key="appliedFilter::start"
          onUnselect={() =>
            this.unselectDateTimeAndSubmitSearch(null, endDateTime)}
        />
      )
    }
    if (endDateTime) {
      timeBubbles.push(
        <AppliedFilterBubble
          allowRemoveFilter={allowRemoveFilter}
          backgroundColor={Theme.time.backgroundColor}
          borderColor={Theme.time.borderColor}
          text={`${_.capitalize(timeRelationship)}${timeRelationship ==
          'disjoint'
            ? ' From'
            : ''} Date Before: ${removeZeroTime(endDateTime)}`}
          key="appliedFilter::end"
          onUnselect={() =>
            this.unselectDateTimeAndSubmitSearch(startDateTime, null)}
        />
      )
    }
    if (startYear != null) {
      let startYearText = (
        <span>
          {_.capitalize(timeRelationship)}
          {timeRelationship == 'disjoint' ? ' From' : ''} Year After:{' '}
          {displayBigYears(startYear)} <abbr title="Common Era">CE</abbr>
        </span>
      )
      timeBubbles.push(
        <AppliedFilterBubble
          allowRemoveFilter={allowRemoveFilter}
          backgroundColor={Theme.time.backgroundColor}
          borderColor={Theme.time.borderColor}
          text={startYearText}
          title={`Remove After ${startYear} Common Era Filter`}
          key="appliedFilter::startYear"
          onUnselect={() => this.unselectYearAndSubmitSearch(null, endYear)}
        />
      )
    }
    if (endYear != null) {
      let endYearText = (
        <span>
          {_.capitalize(timeRelationship)}
          {timeRelationship == 'disjoint' ? ' From' : ''} Year Before:{' '}
          {displayBigYears(endYear)} <abbr title="Common Era">CE</abbr>
        </span>
      )
      timeBubbles.push(
        <AppliedFilterBubble
          allowRemoveFilter={allowRemoveFilter}
          backgroundColor={Theme.time.backgroundColor}
          borderColor={Theme.time.borderColor}
          text={endYearText}
          title={`Remove Before ${endYear} Common Era Filter`}
          key="appliedFilter::endYear"
          onUnselect={() => this.unselectYearAndSubmitSearch(startYear, null)}
        />
      )
    }
    return timeBubbles
  }

  buildSpaceBubbles = allowRemoveFilter => {
    const {bbox, excludeGlobal, geoRelationship} = this.props
    let spaceBubbles = []
    if (bbox) {
      const {west, south, east, north} = bbox
      spaceBubbles.push(
        <AppliedFilterBubble
          allowRemoveFilter={allowRemoveFilter}
          backgroundColor={Theme.map.backgroundColor}
          borderColor={Theme.map.borderColor}
          text={`${_.capitalize(geoRelationship)}${geoRelationship == 'disjoint'
            ? ' From'
            : ''} West: ${west}°, South: ${south}°, East: ${east}°, North: ${north}°`}
          key="appliedFilter::boundingBox"
          onUnselect={() => this.unselectMapAndSubmitSearch()}
        />
      )
    }

    if (excludeGlobal) {
      spaceBubbles.push(
        <AppliedFilterBubble
          allowRemoveFilter={allowRemoveFilter}
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
    const {showAppliedFilters, excludeRemoveFilter} = this.props
    const allowRemoveFilter = !!!excludeRemoveFilter

    const appliedFilters = [
      ...this.buildTextBubbles(allowRemoveFilter),
      ...this.buildSpaceBubbles(allowRemoveFilter),
      ...this.buildTimeBubbles(allowRemoveFilter),
      ...this.buildFacetBubbles(allowRemoveFilter),
    ]

    if (showAppliedFilters && appliedFilters.length !== 0) {
      return <div style={styleWrapper}>{appliedFilters}</div>
    }
    return <div style={styleHidden} />
  }
}
