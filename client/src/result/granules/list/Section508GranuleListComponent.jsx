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
    const granuleList = _.map(this.props.results, (value, key) => {
      _.forEach(value.links, (link) => usedProtocols.add(this.identifyProtocol(link)))
      return <div key={key}>
          <li>
            <h3>{value.title}</h3>
            <div>{this.renderBadges(value.links)}</div>
          </li>
        </div>
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
        <div className={`${styles.mainWindow}`}>

            <div className={`${styles.granuleInfo}`}>
              <div className={`${styles.title}`}>
                {this.props.selectedCollection.title}
              </div>
              <div className={`${styles.description}`}>
                {this.props.selectedCollection.description}
              </div>
              <div className={`${styles.legend-508}`}>
                <h3>Access Protocols:</h3>
                {legendItems}
              </div>
              <ul className={styles.granuleList}>
                {granuleList}
              </ul>
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
    const name = _.toLower(link.linkProtocol || '')
    return this.protocols[name]
  }
}

GranuleList.propTypes = {
  results: PropTypes.object,
  focusedIds: PropTypes.array,
  selectedCollection: PropTypes.object,
  toggleFocus: PropTypes.func,
  showCollections: PropTypes.func
}

export default GranuleList
