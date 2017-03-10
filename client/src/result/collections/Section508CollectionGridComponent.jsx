import React, { PropTypes } from 'react'
import _ from 'lodash'
import styles from './collectionGrid.css'

class Section508CollectionGridComponent extends React.Component {
  constructor(props) {
    super(props)
  }

  getLinksByType(type, links) {
    return links.filter((link) => link.linkFunction === type)
  }

  renderLinks(label, links, linkRenderer) {
    if (!links || links.length === 0) { return <ul></ul> }

    return <ul title={label} className={styles.collectionList508}>{links.map(linkRenderer)}</ul>
  }

  renderLink(link, index) {
    const { linkName, linkProtocol, linkUrl } = link
    return <li key={index} className={styles.links}>
      <a href={linkUrl} target="_blank" title={linkProtocol || linkName || 'Link'}>
        {linkProtocol || linkName || 'Link'}
      </a>
    </li>
  }

  getKeywordsByType(keywords) {
    return keywords
      .map((k) => k.split('>')) // split GCMD keywords apart
      .reduce((list, keys) => list.concat(keys), []) // flatten
      .map((k) => k.toLowerCase().trim()) // you can figure this one out
      .filter((k, i, a) => a.indexOf(k) === i) // dedupe
  }

  renderKeyword(keyword, index) {
    return <li key={index} className={styles.keywords}>
      <div title={keyword} onClick={() => this.props.textSearch(`"${keyword}"`)}>
        {keyword}
      </div>
    </li>
  }

  render() {
    const collections = []
    _.forOwn(this.props.results, (val, key) => {
      collections.push(
          <li key={key} className={styles.listItem}>
            <h3 title="Title">{val.title}</h3>
            <p title="Description">{val.description}</p>
            <div title="Related Links">
              <span>Related Links:</span>
              {this.renderLinks('More Info', this.getLinksByType('information', val.links), this.renderLink)}
              {this.renderLinks('Data Access', this.getLinksByType('download', val.links), this.renderLink)}
            </div>
            <div title="Associated Keywords">
              <span>Associated Keywords:</span>
              {this.renderLinks('Themes', this.getKeywordsByType(val['gcmdScience']), this.renderKeyword.bind(this))}
              {this.renderLinks('Places', this.getKeywordsByType(val['gcmdLocations']), this.renderKeyword.bind(this))}
            </div>
            <div title="Associated Files">
              <span>Associated Files: </span>
              <a onClick={() => this.props.showGranules(key)} title="Show matching files" className={styles.links}>
                Show Matching Files
              </a>
            </div>
          </li>
      )
    })
    return <div>
      <div className={styles.resultCount}>
        Showing {this.props.returnedHits} of {this.props.totalHits} matching results
      </div>
      <div className={styles.listContainer}>
        <ul className={styles.collectionList508}>
          {collections}
        </ul>
      </div>
    </div>
  }
}

Section508CollectionGridComponent.propTypes = {
  textSearch: PropTypes.func.isRequired,
  showGranules: PropTypes.func.isRequired,
  returnedHits: PropTypes.number.isRequired,
  totalHits: PropTypes.number.isRequired,
  results: PropTypes.object.isRequired
}

export default Section508CollectionGridComponent
