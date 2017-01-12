import React, {PropTypes} from 'react'
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
          <div className={`pure-u-7-8 ${styles.title}`}>{item.title}</div>
          <div className={'pure-u-1-8'}>
            <span className={styles.close} onClick={this.close}>x</span>
          </div>
        </div>
        <div className={'pure-g'}>
          <div className={`pure-u-1 pure-u-md-1-3`}>
            {this.renderImage()}
            {this.renderGranulesLink()}
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
       {link.linkName || 'Link'}
     </a>
   </li>
  }

  renderImage() {
    const imgUrl = this.props.item.thumbnail && this.props.item.thumbnail.replace(/^https?:/, '')
    return this.props.item.thumbnail ?
        <img className={styles.previewImg} src={imgUrl}/> :
        <h3 style={{textAlign: 'center'}}>No Image Available</h3>
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
         onClick={() => this.props.textSearch(keyword)}>
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
    return <a onClick={() => this.props.showGranules(this.props.id)} className={`pure-button pure-button-primary`}>
      Show Matching Files
    </a>
  }
}

Detail.propTypes = {
  id: PropTypes.string,
  item: PropTypes.object,
}

export default Detail
