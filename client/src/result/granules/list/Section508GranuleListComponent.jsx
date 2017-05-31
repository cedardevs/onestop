import React, { PropTypes } from 'react'
import ReactDOM from 'react-dom'
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

  componentDidUpdate() {
    const granuleFocus = ReactDOM.findDOMNode(this.granuleFocus)
    if (!_.isNull(granuleFocus)) { granuleFocus.focus() }
  }

  render() {
    const granuleList = _.map(this.props.results, (value, key) =>
      <li key={key} className={styles.listItem}>
        <span title="Title">{value.title}</span>
        <ul title="Access Links" className={styles.granuleList508}>{this.renderLinks(value.links)}</ul>
      </li>
    )
    if (granuleList.length < this.props.totalHits) {
      granuleList.push(<li key="showMore" className={styles.listItem}>
        <div className={`${styles.showMore}`}>
          <button className={`pure-button`}
                  title="Show More Results"
                  onClick={() => this.props.fetchMoreResults()}>
            Show More Results
          </button>
        </div>
      </li>)
    }

    return (
      <div ref={granuleFocus=>this.granuleFocus=granuleFocus} tabIndex={0}>
        <a onClick={this.props.showCollections}
          title="Return To Collection Results"
          tabIndex={0}
          className={styles.links}> Return To Collection Results</a>
        <div className={styles.descriptionContainer}>
          <h2>{this.props.selectedCollection.title}</h2>
          <p title="Description" className={styles.descriptionParagraph}>
            {this.props.selectedCollection.description}</p>
        </div>
        <ol title="Granule List" className={styles.granuleList508}>
          {granuleList}
        </ol>
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
  totalHits: PropTypes.number,
  toggleFocus: PropTypes.func,
  showCollections: PropTypes.func,
  fetchMoreResults: PropTypes.func
}

export default GranuleList
