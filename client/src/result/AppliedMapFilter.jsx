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
    let bbox = null
    if (geoJSON) {
      const coordinates = geoJSON.geometry.coordinates[0]
      const north = Number(coordinates[1][1]).toFixed(2)
      const west = Number(coordinates[1][0]).toFixed(2)
      const south = Number(coordinates[3][1]).toFixed(2)
      const east = Number(coordinates[3][0]).toFixed(2)
      bbox = geoUtils.convertGeoJsonToBbox(geoJSON)
      appliedMap = (
          <AppliedMap
            north={north}
            west={west}
            south={south}
            east={east}
            onUnselect={() => onUnselectMap()}
          />
      )
    }
    return <div style={styleAppliedFacets}>{appliedMap}</div>
  }
}
