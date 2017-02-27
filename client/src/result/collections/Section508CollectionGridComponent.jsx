import React, { PropTypes } from 'react'
import _ from 'lodash'
import CollectionTile from './CollectionTileComponent'
import styles from './collectionGrid.css'

class CollectionGrid extends React.Component {
  constructor(props) {
    super(props)
  }

  getLinks() {
    return this.props && this.props.item && this.props.item.links || []
  }

  getLinksByType(type, links) {
    return links.filter((link) => link.linkFunction === type)
  }

  renderLinks(label, links, linkRenderer) {
    if (!links || links.length === 0) {
      return <div></div>
    }

    return <div className={'pure-g'}>
      <div className={`pure-u-1-6 ${styles.linkRow}`}>
        <span>{label}</span>
      </div>
      <div className={`pure-u-5-6 ${styles.linkRow}`}>
        <ul className={'pure-g'}>{links.map(linkRenderer)}</ul>
      </div>
    </div>
  }

  renderLink(link, index) {
   return <li className={'pure-u'} key={index}>
     <a href={link.linkUrl} target="_blank"
             className={`pure-button pure-button-primary`}>
       {link.linkProtocol || 'Link'}
     </a>
   </li>
  }

  render() {
    const cards = []
    _.forOwn(this.props.results, (val, key) => {
      console.log(val)
      cards.push(
        <div key={key}>
          <li>
            <h3>{val.title}</h3>
            <div>{val.description}</div>
            <div className={`${styles.underscored}`}>
              {this.renderLinks('More Info', this.getLinksByType('information', val.links), this.renderLink)}
              {this.renderLinks('Data Access', this.getLinksByType('download', val.links), this.renderLink)}
            </div>
          </li>
        </div>
      )
    })
    return <div>
      <div>
        Showing {this.props.returnedHits} of {this.props.totalHits} matching results
      </div>
      <ul>
        {cards}
      </ul>
    </div>
  }

}

export default CollectionGrid
