import React, {Component} from 'react'
import { SvgIcon } from '../../common/SvgIcon'
import PropTypes from 'prop-types'

const styleLegend = {
  margin: '1.618em'
}

const styleHeading = {
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
  padding: 0
}

const styleLegendItem = {
  display: 'inline-flex',
  marginLeft: '1em',
  alignItems: 'center',
}

const styleBadge = protocol => {
  return {
    borderRadius: '50%',
    width: '1em',
    height: '1em',
    lineHeight: '1em',
    padding: '0.25em',
    margin: '0.25em',
    font: 'Arial, sans-serif',
    color: 'white',
    fill: 'white',
    textAlign: 'center',
    textDecoration: 'none',
    background: `${protocol.color}`,
  }
}

const styleLegendLabel = {
  font: '1.2em Arial, sans-serif',
  margin: '0.6em 0',
}

export default class GranuleListLegend extends Component {

  renderBadgeIcon = protocol => {
    if (protocol.svgPath) {
      return <SvgIcon path={protocol.svgPath} />
    }
    return <span>{protocol.id}</span>
  }

  render() {

    const { usedProtocols } = this.props

    const legendItems = _.chain(_.toArray(usedProtocols))
        .filter()
        .sortBy('id')
        .uniqBy('id')
        .map((protocol, i) => {
          return (
              <li key={i} style={styleLegendItem}>
                <div style={styleBadge(protocol)}>
                  {this.renderBadgeIcon(protocol)}
                </div>
                <div style={styleLegendLabel}>{protocol.label}</div>
              </li>
          )
        })
        .value()

    return (
        <div style={styleLegend}>
          <h3 style={styleHeading}>Access Protocols:</h3>
          <ul style={styleLegendList}>{legendItems}</ul>
        </div>
    )
  }
}

GranuleListLegend.propTypes = {
  usedProtocols: PropTypes.object.isRequired,
}
