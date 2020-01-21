import React from 'react'
import {styleBadge} from '../../utils/resultUtils'
import {renderBadgeIcon} from '../../utils/resultComponentUtils'

const styleLegendList = {
  justifyContent: 'flex-start',
  display: 'flex',
  flexFlow: 'row wrap',
  paddingBottom: '1em',
  listStyle: 'none',
  margin: 0,
  padding: 0,
}

const styleLegendItem = {
  display: 'inline-flex',
  marginLeft: '1em',
  alignItems: 'center',
}

const styleLegendLabel = {
  font: '1.2em Arial, sans-serif',
  margin: '0.6em 0',
}

export default class AccessProtocolFilter extends React.Component {
  render() {
    const {usedProtocols} = this.props

    const legendItems = _.chain(_.toArray(usedProtocols))
      .sortBy('id')
      .uniqBy('id')
      .map((protocol, i) => {
        return (
          <li key={i} style={styleLegendItem}>
            <div style={styleBadge(protocol)} aria-hidden="true">
              {renderBadgeIcon(protocol)}
            </div>
            <div
              id={`protocol::legend::${protocol.id}`}
              style={styleLegendLabel}
            >
              {protocol.label}
            </div>
          </li>
        )
      })
      .value()

    return <ul style={styleLegendList}>{legendItems}</ul>
  }
}
