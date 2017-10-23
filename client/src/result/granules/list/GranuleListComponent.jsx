import React from 'react'
import PropTypes from 'prop-types'
import ReactDOM from 'react-dom'
import _ from 'lodash'
import MapContainer from '../MapContainer'
import A from 'LinkComponent'
import styles from './list.css'

class GranuleList extends React.Component {

  constructor(props) {
    super(props)
    this.protocols = [
      {id: 'C', names: ['ogc:wcs'],       color: 'coral',     label: 'OGC Web Coverage Service'},
      {id: 'D', names: ['download'],      color: 'blue',      label: 'Download'},
      {id: 'F', names: ['ftp'],           color: 'red',       label: 'FTP'},
      {id: 'L', names: ['noaa:las'],      color: 'aqua',      label: 'NOAA Live Access Server'},
      {id: 'M', names: ['ogc:wms'],       color: 'goldenrod', label: 'OGC Web Map Service'},
      {id: 'O', names: ['opendap'],       color: 'green',     label: 'OPeNDAP'},
      {id: 'T', names: ['thredds'],       color: 'grey',      label: 'THREDDS'},
      {id: 'W', names: ['http', 'https'], color: 'purple',    label: 'Web'},
    ]
  }

  componentDidUpdate() {
    const granuleFocus = ReactDOM.findDOMNode(this.granuleFocus)
    if (!_.isNull(granuleFocus)) { granuleFocus.focus() }
  }

  render() {
    const usedProtocols = new Set()
    const tableRows = _.map(this.props.results, (value, key) => {
      _.forEach(value.links, (link) => usedProtocols.add(this.identifyProtocol(link)))
      return <tr key={key} onMouseEnter={() => this.props.toggleFocus(key, true)} onMouseLeave={() => this.props.toggleFocus(key, false)}>
        <td>{value.title}</td>
        <td className={styles.badgeCell}>{this.renderBadges(value.links)}</td>
      </tr>
    })
    const legendItems = _.chain(_.toArray(usedProtocols))
        .filter()
        .sortBy('id')
        .uniqBy('id')
        .map((protocol, i) => {
          return <div key={i} className={styles.legendItem}>
            <div className={`${styles.badge}`} style={{background: protocol.color}}>{protocol.id}</div>
            <div className={`${styles.label}`}>{protocol.label}</div>
          </div>
        })
        .value()

    return (
      <div ref={granuleFocus=>this.granuleFocus=granuleFocus} tabIndex={0}>
        <a className={styles.navLink}
          tabIndex={0}
          onClick={this.props.showCollections}>Return To Collection Results</a>
        <div className={`pure-g ${styles.mainWindow}`}>
          <div className={`pure-u-1-2 ${styles.map}`}>
            <MapContainer style={styles.mapContainer}/>
          </div>
          <div className={`pure-u-1-2 ${styles.granule}`}>
            <div className={`pure-g ${styles.granuleInfo}`}>
              <div className={`pure-u-1 ${styles.title}`}>
                {this.props.selectedCollection.title}
              </div>
              <div className={`pure-u-1 ${styles.description}`}>
                {this.props.selectedCollection.description}
              </div>
              {_.isEmpty(legendItems)
                ? <div></div>
                : <div className={`pure-u-1 ${styles.legend}`}>
                <h3 className={styles.legendItem}>Access Protocols:</h3>
                {legendItems}
              </div>}
              <div className={`pure-u-1`}>
                <table className={`pure-table ${styles.table}`}>
                  <thead>
                    <tr>
                      <th>Title</th>
                      <th>Data Access</th>
                    </tr>
                  </thead>
                  <tbody>
                    {tableRows}
                    {this.renderPaginationButton()}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    )
  }

  renderBadges(links) {
    const badges = _.chain(links)
        .map((link) => ({protocol: this.identifyProtocol(link), url: link.linkUrl}))
        .filter((info) => info.protocol)
        .sortBy((info) => info.protocol.id)
        .map(this.renderBadge.bind(this))
        .value()
    return _.isEmpty(badges) ? <div>N/A</div> : <div className={styles.badgeLayout}>{badges}</div>
  }

  renderBadge({protocol, url}) {
    return (
        <A href={url} key={url} title={url} target="_blank"
           className={`${styles.badge}`} style={{background: protocol.color}}>
          {protocol.id}
        </A>
    )
  }

  identifyProtocol(link) {
    const name = _.toLower(link.linkProtocol || '')
    return _.find(this.protocols, p => p.names.includes(name))
  }

  renderPaginationButton() {
    if (_.size(this.props.results) < this.props.totalHits) {
      return <tr className={styles.pageButton}>
        <td colSpan="2">
          <button className="pure-button" onClick={() => this.props.fetchMoreResults()}>
            Show More Results
          </button>
        </td>
      </tr>
    }
  }
}

GranuleList.propTypes = {
  results: PropTypes.object,
  focusedIds: PropTypes.array,
  selectedCollection: PropTypes.object,
  toggleFocus: PropTypes.func,
  showCollections: PropTypes.func,
  fetchMoreResults: PropTypes.func,
  totalHits: PropTypes.number
}

export default GranuleList
