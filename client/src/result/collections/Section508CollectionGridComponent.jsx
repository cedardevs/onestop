import React from 'react'
import PropTypes from 'prop-types'
import ReactDOM from 'react-dom'
import _ from 'lodash'
import A from 'LinkComponent'
import styles from './collectionGrid.css'

class Section508CollectionGridComponent extends React.Component {
  constructor(props) {
    super(props)
  }

  componentDidUpdate() {
    const focusCard = ReactDOM.findDOMNode(this.focusCard)
    if (_.isNull(focusCard)) {
      ReactDOM.findDOMNode(this.resultCount).focus()
    } else {
      focusCard.focus()
    }
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
      <A href={linkUrl} target="_blank" title={linkProtocol || linkName || 'Link'}>
        {linkProtocol || linkName || 'Link'}
      </A>
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
      <button title={keyword} onClick={() => this.props.textSearch(`"${keyword}"`)}>
        {keyword}
      </button>
    </li>
  }

  render() {
    const collections = []
    const { returnedHits, totalHits, pageSize } = this.props
    let focusCardNum
    if (returnedHits === totalHits) {
      focusCardNum = returnedHits - (returnedHits % pageSize)
    }
    else {
      focusCardNum = returnedHits - pageSize
    }
    _.forOwn(this.props.results, (val, key) => {
      let listItemProps = {
        key,
        className: styles.listItem,
        tabIndex: 0
      }
      if (focusCardNum-- == 0  && pageSize !== returnedHits) {
        listItemProps.ref = focusCard=>this.focusCard=focusCard
      }
      collections.push(
          <li {...listItemProps}>
            <h3 title="Title">{val.title}</h3>
            <p title="Description">{val.description}</p>
            <div title="Related Links">
              <h4>Related Links:</h4>
              {this.renderLinks('More Info', this.getLinksByType('information', val.links), this.renderLink)}
              {this.renderLinks('Data Access', this.getLinksByType('download', val.links), this.renderLink)}
            </div>
            <div title="Associated Keywords">
              <h4>Associated Keywords:</h4>
              {this.renderLinks('Themes', this.getKeywordsByType(val['gcmdScience']), this.renderKeyword.bind(this))}
              {this.renderLinks('Places', this.getKeywordsByType(val['gcmdLocations']), this.renderKeyword.bind(this))}
            </div>
            <div title="Associated Files">
              <h4>Associated Files: </h4>
              <button onClick={() => this.props.showGranules(key)} title="Show matching files" className={styles.links}>
                Show Matching Files
              </button>
            </div>
          </li>
      )
    })
    if (this.props.returnedHits < this.props.totalHits) {
      collections.push(<li key="showMore" className={styles.listItem}>
        <div className={`${styles.showMore}`}>
          <button className={`pure-button`}
                  title="Show More Results"
                  onClick={() => this.props.fetchMoreResults()}>
            Show More Results
          </button>
        </div>
      </li>)
    }

    return <div>
      <div className={styles.resultCount} tabIndex={0}
        ref={resultCount=>this.resultCount=resultCount}>
        <h2>Search Results (showing {this.props.returnedHits} of {this.props.totalHits})</h2>
      </div>
      <div className={styles.listContainer}>
        <ol className={styles.collectionList508} aria-live="polite">
          {collections}
        </ol>
      </div>
    </div>
  }
}

Section508CollectionGridComponent.propTypes = {
  textSearch: PropTypes.func.isRequired,
  showGranules: PropTypes.func.isRequired,
  fetchMoreResults: PropTypes.func,
  returnedHits: PropTypes.number.isRequired,
  totalHits: PropTypes.number.isRequired,
  results: PropTypes.object.isRequired
}

export default Section508CollectionGridComponent
