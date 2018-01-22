import React, {Component} from 'react'

import AppliedFacetFilter from './AppliedFacetFilter'
import AppliedTimeFilter from './AppliedTimeFilter'
import AppliedMapFilter from './AppliedMapFilter'

import {styleResult} from './ResultStyles'

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
}
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
          backgroundColor={Theme.facet.backgroundColor}
          borderColor={Theme.facet.borderColor}
        />

        <AppliedTimeFilter
          startDateTime={this.startDateTime}
          endDateTime={this.endDateTime}
          onUnselectDateTime={this.unselectDateTimeAndSubmitSearch}
          backgroundColor={Theme.time.backgroundColor}
          borderColor={Theme.time.borderColor}
        />

        <AppliedMapFilter
          geoJSON={this.geoJSON}
          onUnselectMap={this.unselectMapAndSubmitSearch}
          excludeGlobal={this.excludeGlobal}
          onUnselectExcludeGlobal={this.unselectExcludeGlobal}
          backgroundColor={Theme.map.backgroundColor}
          borderColor={Theme.map.borderColor}
        />

        {this.props.children}
      </div>
    )
  }
}
