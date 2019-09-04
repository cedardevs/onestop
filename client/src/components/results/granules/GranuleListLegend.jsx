import React from 'react'
import PropTypes from 'prop-types'
import {styleBadge, renderBadgeIcon} from '../../../utils/resultUtils'
import {fontFamilySerif} from '../../../utils/styleUtils'
import {identifyProtocol} from '../../../utils/resultUtils'

const styleLegend = {
  color: 'black',
  margin: '0 1.618em 1.618em 1.618em',
}

const styleHeading = {
  fontFamily: fontFamilySerif(),
  fontSize: '1.2em',
  margin: '0 0 0.618em 0',
  padding: 0,
}

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

export default class GranuleListLegend extends React.Component {
  render() {
    // keep track of used protocols in results to avoid unnecessary legend keys
    const usedProtocols = new Set()
    _.forEach(this.props.results, value => {
      //

      _.forEach(value.links, link => {
        // if(link.linkFunction.toLowerCase() === 'download' || link.linkFunction.toLowerCase() === 'fileaccess') {
        return usedProtocols.add(identifyProtocol(link))
        // }
      })
    })

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

    return (
      <div style={styleLegend}>
        <h2 style={styleHeading} aria-label="Access Protocols Legend">
          Access Protocols:
        </h2>
        <ul style={styleLegendList}>{legendItems}</ul>
      </div>
    )
  }
}

GranuleListLegend.propTypes = {
  results: PropTypes.object.isRequired,
}
