import React, { PropTypes } from 'react'
import _ from 'lodash'
import MapContainer from '../MapContainer'
import styles from './list.css'

class GranuleList extends React.Component {

  constructor(props) {
    super(props)
    this.loadLinks = this.loadLinks.bind(this)
    this.state = {
      results: [],
      protocols: {
        'download': {id: 'D', color: 'blue',   label: 'Download'},
        'ftp':      {id: 'F', color: 'red',    label: 'FTP'},
        'noaa:las': {id: 'L', color: 'aqua',   label: 'NOAA Live Access Server'},
        'opendap':  {id: 'O', color: 'green',  label: 'OPeNDAP'},
        'thredds':  {id: 'T', color: 'grey',   label: 'THREDDS'},
        'http':     {id: 'W', color: 'purple', label: 'Web'},
        'https':    {id: 'W', color: 'purple', label: 'Web'},
      }
    }
  }

  componentWillReceiveProps(nextProps) {
    this.setState({results: nextProps.results || []})
  }

  loadLinks(links) {
    if (!links || !links.length) { return <div></div> }
    return <div className={`pure-u-1`} style={{display: 'flex', flexWrap: 'wrap'}}>
      {links.map(link => this.linkBadge(link))}
    </div>
  }

  linkBadge(link) {
    const protocol = this.identifyProtocol(link)
    const url = link.linkUrl
    if (protocol) {
      return <a href={url} key={url} title={url}
                className={`${styles.letterCircle}`}
                style={{background: protocol.color}}>
        {protocol.id}
      </a>
    }
    else { return <div key={url}></div> }
  }

  identifyProtocol(link) {
    const name = _.toLower(link.linkName || '')
    return this.state.protocols[name]
  }

  render() {
    const usedProtocols = new Set()

    const tableRows = _.map(this.state.results, (value, key) => {
      _.forEach(value.links, (link) => usedProtocols.add(this.identifyProtocol(link)))
      return <tr key={key} onMouseOver={() => this.props.onMouseOver(key)} onMouseLeave={() => this.props.onMouseOver(key)}>
        <td>{value.title}</td>
        <td className={styles.badgeCell}>{this.loadLinks(value.links)}</td>
      </tr>
    })

    const legendRows = _.chain(_.toArray(usedProtocols))
        .filter(_.identity)
        .map((protocol, i) => {
          return <div key={i} className={styles.legendRow}>
            <div className={`${styles.letterCircle}`} style={{background: protocol.color}}>{protocol.id}</div>
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
            <div className={`${styles.granuleInfo}`}>
              <div className={`${styles.title}`}>
                {this.props.selectedCollection.title}
              </div>
              <div className={`${styles.description}`}>
                {this.props.selectedCollection.description}
              </div>
              <div className='pure-g'>
                <div className={`pure-u-1 ${styles.legend}`}>
                  {legendRows}
                </div>
              </div>
              <table className={`pure-table ${styles.table}`}>
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
}

GranuleList.propTypes = {
  results: PropTypes.object,
  focusedIds: PropTypes.array,
  selectedCollection: PropTypes.object,
  onMouseOver: PropTypes.func,
}

export default GranuleList
