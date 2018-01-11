import React, { Component } from 'react'
import AppliedMap from './AppliedMap'
import * as geoUtils from '../../src/utils/geoUtils'

const styleAppliedFacets = {
  display: 'flex',
  flexFlow: 'row wrap',
  padding: '0 2em 1.618em 2em',
}

export default class AppliedMapFilter extends Component {
  render() {
    const { geoJSON, onUnselectMap } = this.props

    let appliedMap = null
    if (geoJSON && geoJSON.geometry && geoJSON.geometry.coordinates) {
      let bbox = geoUtils.convertGeoJsonToBbox(geoJSON)
      appliedMap = (
          <AppliedMap
            north={bbox.north}
            west={bbox.west}
            south={bbox.south}
            east={bbox.east}
            onUnselect={() => onUnselectMap()}
          />
      )
    }
    return <div style={styleAppliedFacets}>{appliedMap}</div>
  }
}
