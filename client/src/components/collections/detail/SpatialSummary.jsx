import React from 'react'
import {buildCoordinatesString} from '../../../utils/resultUtils'

export default class SpatialSummary extends React.Component {
  render() {
    const {item} = this.props
    return <div>{buildCoordinatesString(item.spatialBounding)}</div>
  }
}
