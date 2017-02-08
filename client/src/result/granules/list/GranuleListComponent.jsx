import React, { PropTypes } from 'react'
import _ from 'lodash'
import MapContainer from '../MapContainer'
import styles from './list.css'

class GranuleList extends React.Component {

  constructor(props) {
    super(props)
    this.loadLinks = this.loadLinks.bind(this)
    this.identifyFileSource = this.identifyFileSource.bind(this)
    this.updateState = this.updateState.bind(this)
    this.state = {
      records: [],
      dataSource: [
        { id: 'OPeNDAP', letter: 'O', color: 'green', legend: false,
          names: ['dap', 'opendap']},
        { id: 'Download', letter: 'D', color: 'blue', legend: false,
          names: ['download']},
        { id: 'FTP', letter: 'F', color: 'red', legend: false,
          names: ['ftp']},
        { id: 'Web', letter: 'W', color: 'purple', legend: false,
          names: ['http', 'https']},
        { id: 'THREDDS', letter: 'T', color: 'grey', legend: false,
          names: ['thredds']}]}
  }

  componentWillReceiveProps(nextProps) {
    this.updateState(nextProps)
  }

  updateState(props) {
    const rows = []
    _.forEach(props.results, (value, key) => {
      rows.push(
        <tr key={key} onMouseOver={() => this.props.onMouseOver(key)} onMouseLeave={() => this.props.onMouseOver(key)}>
          <td>{value.title}</td>
          <td>{this.loadLinks(value.links)}</td>
        </tr>
      )
    })
    this.setState({records: rows})
  }

  loadLinks(links) {
    if (!links || !links.length) { return <div></div> }
    return <div className={'pure-g'}>{links.map((link) => this.linkBadge(link))}</div>
  }

  linkBadge({linkName, linkUrl}) {
    const protocol = linkName ? _.toLower(linkName) : ''
    const {letter, color} = this.identifyFileSource(protocol)
    return <a href={linkUrl}
              className={`pure-u-1 pure-u-md-1-6 pure-u-lg-1-12
                ${styles.letterCircle}`}
                style={{background: color}}>
              {letter}
            </a>
  }

  identifyFileSource(protocol) {
    const { dataSource } = this.state
    let finalSource
    _.forEach(dataSource, (source, idx)  => {
      if (_.includes(source.names, protocol)) {
        finalSource = source
        dataSource[idx].legend = true
        this.setState(dataSource)
      }
    })
    return finalSource || {letter: '?', color: 'maroon'}
  }

  render() {
    const { records, dataSource } = this.state
    console.log(dataSource)
    const legendRows = _.filter(dataSource, source => source.legend )
    console.log(legendRows)
    const legend = <div className={styles.legend}>{legendRows.map( row => {
      return <div className={`pure-u-sm-1-3 pure-u-md-1-6`}>
        <div className={`${styles.letterCircle} ${styles.legendRow}`}
          style={{background: row.color}}>
          {row.letter}
        </div>
        <div className={styles.legendRow}>{row.id}</div>
      </div>
    })}</div>

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
              <div className='pure-g'>{legend}</div>
              <table className={`pure-table ${styles.table}`}>
                <thead>
                <tr>
                  <th>Title</th>
                  <th>Data Access</th>
                </tr>
                </thead>
                <tbody>{records}</tbody>
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
