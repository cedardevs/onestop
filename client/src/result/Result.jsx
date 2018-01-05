import React, { Component } from 'react'

import AppliedFacetFilter from './AppliedFacetFilter'
import AppliedTimeFilter from './AppliedTimeFilter'
import AppliedMapFilter from './AppliedMapFilter'

import { styleResult } from './ResultStyles'

export default class Result extends Component {
  constructor(props) {
    super(props)

    this.location = props.location.pathname
    this.selectedFacets = props.selectedFacets
    this.startDateTime = props.startDateTime
    this.endDateTime = props.endDateTime
    this.geoJSON = props.geoJSON
    this.toggleFacet = props.toggleFacet
    this.updateDateRange = props.updateDateRange
    this.removeGeometry = props.removeGeometry,
    this.submit = props.submit
  }

  componentWillUpdate(nextProps) {
    this.location = nextProps.location.pathname
    this.selectedFacets = nextProps.selectedFacets
    this.startDateTime = nextProps.startDateTime
    this.endDateTime = nextProps.endDateTime
    this.geoJSON = nextProps.geoJSON
  }

  unselectFacetAndSubmitSearch = (category, term) => {
    this.toggleFacet(category, term, false)
    this.submit()
  }

  unselectDateTimeAndSubmitSearch = (start, end) => {
    this.updateDateRange(start, end)
    this.submit()
  }

  unselectMapAndSubmitSearch = () => {
    this.removeGeometry()
    this.submit()
  }

  render() {
    let appliedMapFilter = null
    if (this.geoJSON) {
      appliedMapFilter = (
        <AppliedMapFilter
          geoJSON={this.geoJSON}
          onUnselectMap={this.unselectMapAndSubmitSearch}
        />
      )
    }

    return (
      <div style={styleResult}>
        <AppliedFacetFilter
          location={this.location}
          selectedFacets={this.selectedFacets}
          onUnselectFacet={this.unselectFacetAndSubmitSearch}
        />

        {/*
				 TODO: Rendering time or map filters will require drill-down behavior on result view instead of currently
				 present new-search behavior (otherwise applied filters update store but don't modify the search until a new search
				 is sent -- i.e., time filter appears but doesn't apply)
				 */}

        {/*<AppliedTimeFilter*/}
        {/*location={this.location}*/}
        {/*startDateTime={this.startDateTime}*/}
        {/*endDateTime={this.endDateTime}*/}
        {/*onUnselectDateTime={this.unselectDateTimeAndSubmitSearch}*/}
        {/*/>*/}
        {appliedMapFilter}

        {this.props.children}
      </div>
    )
  }
}
