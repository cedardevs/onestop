import React, {Component} from 'react'

import AppliedFacetFilter from './AppliedFacetFilter'
import AppliedTimeFilter from './AppliedTimeFilter'
import AppliedMapFilter from './AppliedMapFilter'

import {styleResult} from './ResultStyles'

export default class Result extends Component {
  constructor(props) {
    super(props)

    this.selectedFacets = props.selectedFacets
    this.startDateTime = props.startDateTime
    this.endDateTime = props.endDateTime
    this.geoJSON = props.geoJSON
    this.excludeGlobal = props.excludeGlobal
  }

  componentWillUpdate(nextProps) {
    this.selectedFacets = nextProps.selectedFacets
    this.startDateTime = nextProps.startDateTime
    this.endDateTime = nextProps.endDateTime
    this.geoJSON = nextProps.geoJSON
    this.excludeGlobal = nextProps.excludeGlobal
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

  render() {
    return (
      <div style={styleResult}>
        <AppliedFacetFilter
          selectedFacets={this.selectedFacets}
          onUnselectFacet={this.unselectFacetAndSubmitSearch}
        />

        <AppliedTimeFilter
          startDateTime={this.startDateTime}
          endDateTime={this.endDateTime}
          onUnselectDateTime={this.unselectDateTimeAndSubmitSearch}
        />

        <AppliedMapFilter
          geoJSON={this.geoJSON}
          onUnselectMap={this.unselectMapAndSubmitSearch}
          excludeGlobal={this.excludeGlobal}
          onUnselectExcludeGlobal={this.unselectExcludeGlobal}
        />

        {this.props.children}
      </div>
    )
  }
}
