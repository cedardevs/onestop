import React, { PropTypes } from 'react'
import _ from 'lodash'
import CollectionTile from './CollectionTileComponent'
import styles from './collectionGrid.css'

class CollectionGrid extends React.Component {
  constructor(props) {
    super(props)
  }

  getLinksByType(type, links) {
    return links.filter((link) => link.linkFunction === type)
  }

  renderLinks(label, links, linkRenderer) {
    if (!links || links.length === 0) { return <div></div> }

    return <div>
      <div>
        <span>{label}</span>
      </div>
      <div>
        {links.map(linkRenderer)}
      </div>
    </div>
  }

  renderLink(link, index) {
    const { linkName, linkProtocol, linkUrl } = link
    return <div key={index} className={`${styles.linkRow}`}>
      <a href={linkUrl} target="_blank"
             className={`pure-button pure-button-primary`}>
       {linkProtocol || linkName || 'Link'}
      </a>
   </div>
  }

  getKeywordsByType(keywords) {
    return keywords
      .map((k) => k.split('>')) // split GCMD keywords apart
      .reduce((list, keys) => list.concat(keys), []) // flatten
      .map((k) => k.toLowerCase().trim()) // you can figure this one out
      .filter((k, i, a) => a.indexOf(k) === i) // dedupe
  }

  renderKeyword(keyword, index) {
    return <div key={index} className={`${styles.linkRow}`}>
      <a className={`pure-button ${styles['button-secondary']}`}
         onClick={() => this.props.textSearch(keyword)}>
        {keyword}
      </a>
    </div>
  }

  render() {
    const cards = []
    _.forOwn(this.props.results, (val, key) => {
      cards.push(
        <div key={key}>
          <li>
            <h3>{val.title}</h3>
            <div>{val.description}</div>
            <div>
              {this.renderLinks('More Info', this.getLinksByType('information', val.links), this.renderLink)}
              {this.renderLinks('Data Access', this.getLinksByType('download', val.links), this.renderLink)}
            </div>
            <div>
              {this.renderLinks('Themes', this.getKeywordsByType(val['gcmdScience']), this.renderKeyword)}
              {this.renderLinks('Places', this.getKeywordsByType(val['gcmdLocations']), this.renderKeyword)}
            </div>
            <a onClick={() => this.props.showGranules(key)} className={`pure-button pure-button-primary ${styles.granulesButton}`}>
              Show Matching Files
            </a>
          </li>
        </div>
      )
    })
    return <div>
      <div>
        Showing {this.props.returnedHits} of {this.props.totalHits} matching results
      </div>
      <ul className={styles.collectionsList}>
        {cards}
      </ul>
    </div>
  }

}

export default CollectionGrid
