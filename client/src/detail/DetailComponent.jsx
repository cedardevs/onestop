import React, {PropTypes} from 'react'
import MapThumbnailComponent from '../common/MapThumbnailComponent'
import {processUrl} from '../utils/urlUtils'
import styles from './detail.css'

class Detail extends React.Component {
  constructor(props) {
    super(props)

    this.close = this.close.bind(this)
    this.handleKeyDown = this.handleKeyDown.bind(this)
    this.renderKeyword = this.renderKeyword.bind(this)
  }

  render() {
    if (!this.props.id || !this.props.item) {
      return <div style={{display: 'none'}}></div>
    }

    const item = this.props.item
    return <div className={styles.modal}>
      <div className={styles.modalContent}>
        <div className={`pure-g ${styles.header} ${styles.underscored}`}>
          <div className={`pure-u-7-8 ${styles.title}`} title={`${item.title}`}>{item.title}</div>
          <div className={'pure-u-1-8'}>
            <span className={styles.close} onClick={this.close}>x</span>
          </div>
        </div>
        <div className={'pure-g'}>
          <div className={`pure-u-1 pure-u-md-1-3`} style={{textAlign: 'center'}}>
            {this.renderImage()}
            {this.renderGranulesLink()}
            {this.renderDSMMRating()}
          </div>
          <div className={`pure-u-1 pure-u-md-2-3`}>
            <div className={`pure-g`}>
              <div className={`pure-u-1 ${styles.underscored}`}>
                <p>{item.description}</p>
              </div>
            </div>
            <div className={`${styles.underscored}`}>
              {this.renderLinks('More Info', this.getLinksByType('information'), this.renderLink)}
              {this.renderLinks('Data Access', this.getLinksByType('download'), this.renderLink)}
            </div>
            <div>
              {this.renderLinks('Themes', this.getKeywordsByType('gcmdScience'), this.renderKeyword)}
              {this.renderLinks('Places', this.getKeywordsByType('gcmdLocations'), this.renderKeyword)}
            </div>
          </div>
        </div>
      </div>
    </div>
  }

  getLinks() {
    return this.props && this.props.item && this.props.item.links || []
  }

  getLinksByType(type) {
    return this.getLinks().filter((link) => link.linkFunction === type)
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

  renderImage() {
    const imgUrl = processUrl(this.props.item.thumbnail)
    return imgUrl ?
        <img className={styles.previewImg} src={imgUrl}/> :
        <div className={styles.previewMap}>
          <MapThumbnailComponent geometry={this.props.geometry}/>
        </div>
  }

  getKeywordsByType(type) {
    const keywords = this.props.item && this.props.item[type] || []
    return keywords
        .map((k) => k.split('>')) // split GCMD keywords apart
        .reduce((list, keys) => list.concat(keys), []) // flatten
        .map((k) => k.toLowerCase().trim()) // you can figure this one out
        .filter((k, i, a) => a.indexOf(k) === i) // dedupe
  }

  renderKeyword(keyword, index) {
    return <li className={'pure-u'} key={index}>
      <a className={`pure-button ${styles['button-secondary']}`}
         onClick={() => this.props.textSearch(`"${keyword}"`)}>
        {keyword}
      </a>
    </li>
  }

  close() {
    this.props.dismiss()
  }

  handleKeyDown(event) {
    if (event.keyCode === 27) { // esc
      this.close()
    }
  }

  componentWillUpdate(nextProps, nextState) {
    if (nextProps.id) {
      document.addEventListener("keydown", this.handleKeyDown, false);
    }
    else {
      document.removeEventListener("keydown", this.handleKeyDown, false);
    }
  }

  renderGranulesLink() {
    return <a onClick={() => this.props.showGranules(this.props.id)} className={`pure-button pure-button-primary ${styles.granulesButton}`}>
      Show Matching Files
    </a>
  }

  renderDSMMRating() {
    const dsmmScore = this.props.item.dsmmAverage
    const fullStars = Math.floor(dsmmScore)
    const halfStar = dsmmScore % 1 >= 0.5

    const stars = []
    if (dsmmScore === 0) {
      stars.push(<span key={42} className={styles.dsmmMissing}>DSMM Rating Unavailable</span>)
    }
    else {
      for (let i = 0; i < 5; i++) {
        if (i < fullStars) {
          stars.push(this.renderFullStar(i))
        }
        else if (i === fullStars && halfStar) {
          stars.push(this.renderHalfStar(i))
        }
        else {
          stars.push(this.renderEmptyStar(i))
        }
      }
    }

    return (
        <div>
          {stars}
          <div className={`${styles.dsmmInfo}`}>
            <i className={`fa fa-info-circle`}></i>
            <div className={`${styles.text}`}> This is the average DSMM rating of this collection.
              The <a href="http://doi.org/10.2481/dsj.14-049" target="_blank" title="Data Stewardship Maturity Matrix Information">
                Data Stewardship Maturity Matrix (DSMM)</a> is a unified framework that defines criteria for the following nine components based on measurable practices:
              <ul>
                <li>Accessibility</li>
                <li>Data Integrity</li>
                <li>Data Quality Assessment</li>
                <li>Data Quality Assurance</li>
                <li>Data Quality Control Monitoring</li>
                <li>Preservability</li>
                <li>Production Sustainability</li>
                <li>Transparency Traceability</li>
                <li>Usability</li>
              </ul>
            </div>
          </div>
        </div>
    )
  }

  renderFullStar(i) {
    return <i className={`${styles.star} fa fa-star`} key={i}></i>
  }

  renderHalfStar(i) {
    return <i className={`${styles.star} fa fa-star-half-o`} key={i}></i>
  }

  renderEmptyStar(i) {
    return <i className={`${styles.star} fa fa-star-o`} key={i}></i>
  }
}

Detail.propTypes = {
  id: PropTypes.string,
  item: PropTypes.object,
}

export default Detail
