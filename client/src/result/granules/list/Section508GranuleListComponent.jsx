import React, { PropTypes } from 'react'
import _ from 'lodash'
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
    const granuleList = _.map(this.props.results, (value, key) =>
      <li key={key} className={styles.listItem}>
        <span title="Title">{value.title}</span>
        <ul title="Access Links" className={styles.granuleList508}>{this.renderLinks(value.links)}</ul>
      </li>
    )

    return (
      <div>
        <a onClick={this.props.showCollections} title="Return To Collection Results" className={styles.links}>Return To Collection Results</a>
        <div className={styles.descriptionContainer}>
          <h1>{this.props.selectedCollection.title}</h1>
          <p title="Description" className={styles.descriptionParagraph}>
            {this.props.selectedCollection.description}</p>
        </div>
        <ul title="Granule List" className={styles.granuleList508}>
          {granuleList}
        </ul>
      </div>
    )
  }

  renderLinks(links) {
    return _.chain(links)
        .map((link) => ({protocol: this.identifyProtocol(link), url: link.linkUrl, name: link.linkName}))
        .filter((info) => info.protocol)
        .sortBy((info) => info.protocol.id)
        .uniqBy((info) => info.url)
        .map(({protocol, url, name}) => <li key={url} className={styles.linkRow}>
          <a href={url} title={`${protocol.label} link`} className={styles.links}>{name}</a>
        </li>)
        .value()
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
