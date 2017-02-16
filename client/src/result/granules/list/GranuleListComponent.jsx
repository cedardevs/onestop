import React, { PropTypes } from 'react'
import _ from 'lodash'
import MapContainer from '../MapContainer'
import styles from './list.css'

class GranuleList extends React.Component {

  constructor(props) {
    super(props)
    this.protocols = {
      'download': {id: 'D', color: 'blue',   label: 'Download'},
      'ftp':      {id: 'F', color: 'red',    label: 'FTP'},
      'noaa:las': {id: 'L', color: 'aqua',   label: 'NOAA Live Access Server'},
      'opendap':  {id: 'O', color: 'green',  label: 'OPeNDAP'},
      'thredds':  {id: 'T', color: 'grey',   label: 'THREDDS'},
      'http':     {id: 'W', color: 'purple', label: 'Web'},
      'https':    {id: 'W', color: 'purple', label: 'Web'},
    }
  }

  render() {
    const usedProtocols = new Set()
    const tableRows = _.map(this.props.results, (value, key) => {
      _.forEach(value.links, (link) => usedProtocols.add(this.identifyProtocol(link)))
      return <tr key={key} onMouseOver={() => this.props.onMouseOver(key)} onMouseLeave={() => this.props.onMouseOver(key)}>
        <td>{value.title}</td>
        <td className={styles.badgeCell}>{this.renderBadges(value.links)}</td>
      </tr>
    })
    const legendItems = _.chain(_.toArray(usedProtocols))
        .filter()
        .sortBy('id')
        .map((protocol, i) => {
          return <div key={i} className={styles.legendItem}>
            <div className={`${styles.badge}`} style={{background: protocol.color}}>{protocol.id}</div>
            <div className={`${styles.label}`}>{protocol.label}</div>
          </div>
        })
        .value()

    return (
      <div>
        <a className={styles.navLink} onClick={this.props.showCollections}>Return To Collection Results</a>
        <div className={`pure-g ${styles.mainWindow}`}>
          <div className={`pure-u-1-2 ${styles.map}`}>
            <MapContainer style={styles.mapContainer}/>
          </div>
          <div className={`pure-u-1-2`}>
            <div className={`pure-g ${styles.granuleInfo}`}>
              <div className={`pure-u-1 ${styles.title}`}>
                {this.props.selectedCollection.title}
              </div>
              <div className={`pure-u-1 ${styles.description}`}>
                {this.props.selectedCollection.description}
              </div>
              <div className={`pure-u-1 ${styles.legend}`}>
                <h3 className={styles.legendItem}>Access Protocols:</h3>
                {legendItems}
              </div>
              <table className={`pure-u-1 pure-table ${styles.table}`}>
                <thead>
                <tr>
                  <th>Title</th>
                  <th>Data Access</th>
                </tr>
                </thead>
                <tbody>{tableRows}</tbody>
              </table>
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
    return <div>{badges}</div>
  }

  renderBadge({protocol, url}) {
    return (
        <a href={url} key={url} title={url}
           className={`${styles.badge}`}
           style={{background: protocol.color}}>
          {protocol.id}
        </a>
    )
  }

  identifyProtocol(link) {
    const name = _.toLower(link.linkName || '')
    return this.protocols[name]
  }
}

GranuleList.propTypes = {
  results: PropTypes.object,
  focusedIds: PropTypes.array,
  selectedCollection: PropTypes.object,
  onMouseOver: PropTypes.func,
  showCollections: PropTypes.func
}

export default GranuleList
