import React, {Component} from 'react'
import FlexScroll from '../../common/FlexScroll'
import FlexColumn from '../../common/FlexColumn'
import Button from '../../common/input/Button'
import _ from 'lodash'
import MapContainer from './GranuleMapContainer'
import A from '../../common/link/Link'

const styleLegendHeading = {
  margin: '0 0 0.618em 0',
  padding: 0,
}

const styleLegend = {
  justifyContent: 'flex-start',
  display: 'flex',
  flexFlow: 'row wrap',
  paddingBottom: '1em',
}

const styleLegendItem = {
  display: 'inline-flex',
  marginLeft: '1em',
  alignItems: 'center',
}

const styleTable = {
  padding: '2px'
}

const styleTableHeadRow = {
  backgroundColor: '#E0E0E0',
  color: 'black',
  textAlign: 'left',
}

const styleTableBodyRow = even => {
  return {
    backgroundColor: even ? '#222' : '#111',
    color: 'white',
  }
}

const styleTableCell = {
  padding: '0.618em',
  borderRight: '1px solid #CBCBCB',
}

const styleBadgesCell = {
  width: '25%',
  minWidth: '4em',
  padding: '0.309em',
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
    textAlign: 'center',
    textDecoration: 'none',
    background: `${protocol.color}`,
  }
}

const styleBadgeLayout = {
  display: 'flex',
  flexFlow: 'row wrap',
  justifyContent: 'flex-end',
}

const styleLegendLabel = {
  font: '1.2em Arial, sans-serif',
  margin: '0.6em 0',
}

export default class GranuleView extends Component {
  constructor(props) {
    super(props)

    this.protocols = [
      {
        id: 'C',
        names: [ 'ogc:wcs' ],
        color: 'coral',
        label: 'OGC Web Coverage Service',
      },
      {id: 'D', names: [ 'download' ], color: 'blue', label: 'Download'},
      {id: 'F', names: [ 'ftp' ], color: 'red', label: 'FTP'},
      {
        id: 'H',
        names: [ 'http', 'https' ],
        color: 'purple',
        label: 'HTTP/HTTPS',
      },
      {
        id: 'L',
        names: [ 'noaa:las' ],
        color: 'aqua',
        label: 'NOAA Live Access Server',
      },
      {
        id: 'M',
        names: [ 'ogc:wms' ],
        color: 'goldenrod',
        label: 'OGC Web Map Service',
      },
      {id: 'O', names: [ 'opendap' ], color: 'green', label: 'OPeNDAP'},
      {id: 'T', names: [ 'thredds' ], color: 'grey', label: 'THREDDS'},
      {id: 'W', names: [ '' ], color: '#e69500', label: 'Web'},
    ]
  }

  render() {
    const {results, toggleFocus} = this.props
    const usedProtocols = new Set()
    let rowNumber = -1
    const tableRows = _.map(results, (value, key) => {
      const rowEven = rowNumber++ % 2 === 0
      _.forEach(value.links, link =>
        usedProtocols.add(this.identifyProtocol(link))
      )
      return (
        <tr
          key={key}
          style={styleTableBodyRow(rowEven)}
          onMouseEnter={() => toggleFocus(key, true)}
          onMouseLeave={() => toggleFocus(key, false)}
        >
          <td style={styleTableCell}>{value.title}</td>
          <td style={styleBadgesCell}>{this.renderBadges(value.links)}</td>
        </tr>
      )
    })
    const legendItems = _.chain(_.toArray(usedProtocols))
      .filter()
      .sortBy('id')
      .uniqBy('id')
      .map((protocol, i) => {
        return (
          <div key={i} style={styleLegendItem}>
            <div style={styleBadge(protocol)}>{protocol.id}</div>
            <div style={styleLegendLabel}>{protocol.label}</div>
          </div>
        )
      })
      .value()

    const map = <MapContainer key="granuleMap" forceShow={true} />

    const granuleLoadingMessage = this.renderLoadingMessage()

    const granuleLegend = (
      <div key="granuleLegend">
        <h3 style={styleLegendHeading}>Access Protocols:</h3>
        <div style={styleLegend}>{legendItems}</div>
      </div>
    )

    const granuleInfo = (
      <FlexColumn items={[ granuleLoadingMessage, granuleLegend ]} />
    )

    const granuleTable = (
      <table style={styleTable}>
        <thead>
          <tr style={styleTableHeadRow}>
            <th style={styleTableCell}>Title</th>
            <th style={styleTableCell}>Data Access</th>
          </tr>
        </thead>
        <tbody>{tableRows}</tbody>
      </table>
    )

    return (
      <FlexScroll
        left={map}
        styleLeft={{marginRight: '1.618em'}}
        rightTop={granuleInfo}
        rightScroll={granuleTable}
        rightBottom={this.renderPaginationButton()}
      />
    )
  }

  renderLoadingMessage() {
    const styleShowMessage = _.isEmpty(this.props.results)
      ? {padding: '1em'}
      : {display: 'none'}
    return (
      <div
        key="granuleLoadingMessage"
        style={styleShowMessage}
      >
        Please wait a moment while the results load...
      </div>
    )
  }

  renderBadges(links) {
    const badges = _.chain(links)
      .map(link => ({protocol: this.identifyProtocol(link), url: link.linkUrl}))
      .filter(info => info.protocol)
      .sortBy(info => info.protocol.id)
      .map(this.renderBadge.bind(this))
      .value()
    return _.isEmpty(badges) ? (
      <div>N/A</div>
    ) : (
      <div style={styleBadgeLayout}>{badges}</div>
    )
  }

  renderBadge({protocol, url}) {
    return (
      <A
        href={url}
        key={url}
        title={url}
        target="_blank"
        style={styleBadge(protocol)}
      >
        {protocol.id}
      </A>
    )
  }

  identifyProtocol(link) {
    const name = _.toLower(link.linkProtocol || '')
    return _.find(this.protocols, p => p.names.includes(name))
  }

  renderPaginationButton() {
    const {results, totalHits, fetchMoreResults} = this.props
    if (_.size(results) < totalHits) {
      const moreResultsButton = (
        <Button
          text="Show More Results"
          onClick={fetchMoreResults}
          style={{display: 'inherit', width: '100%', borderRadius: 0}}
        />
      )
      return <div style={{padding:"2px"}}>{moreResultsButton}</div>
    }
  }
}
