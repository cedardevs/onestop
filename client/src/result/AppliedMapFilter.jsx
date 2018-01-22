import React, {Component} from 'react'
import AppliedFilterBubble from './AppliedFilterBubble'
import * as geoUtils from '../../src/utils/geoUtils'

const styleAppliedFacets = {
  display: 'flex',
  flexFlow: 'row wrap',
  padding: '0 2em 1em',
}

export default class AppliedMapFilter extends Component {
  render() {
    const {
      geoJSON,
      onUnselectMap,
      excludeGlobal,
      onUnselectExcludeGlobal,
      backgroundColor,
      borderColor,
    } = this.props

    let appliedMap = []
    if (geoJSON && geoJSON.geometry && geoJSON.geometry.coordinates) {
      let bbox = geoUtils.convertGeoJsonToBbox(geoJSON)
      const {west, south, east, north} = bbox
      const name = `West: ${west}°, South: ${south}°, East: ${east}°, North: ${north}°`
      appliedMap.push(
        <AppliedFilterBubble
          backgroundColor={backgroundColor}
          borderColor={borderColor}
          text={name}
          key="appliedFilter::boundingBox"
          onUnselect={() => onUnselectMap()}
        />
      )
    }
    if (excludeGlobal) {
      const name = 'Exclude Global'
      appliedMap.push(
        <AppliedFilterBubble
          backgroundColor={backgroundColor}
          borderColor={borderColor}
          text={name}
          key="appliedFilter::excludeGlobal"
          onUnselect={() => onUnselectExcludeGlobal()}
        />
      )
    }

    if (appliedMap.length > 0) {
      return <div style={styleAppliedFacets}>{appliedMap}</div>
    }
    return null
  }
}
