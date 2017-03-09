import React, { PropTypes } from 'react'
import { processUrl } from '../utils/urlUtils'
import styles from './featuredItems.css'

class FeaturedItemsComponent extends React.Component {
  constructor(props) {
    super(props)

    this.state = {current: 0}
  }

  render() {
    if (this.props.items.length === 0) {
      return null
    }

    return <div className={`pure-g ${styles.featured}`}>
      <div className={`pure-u-1 pure-u-md-1-4 ${styles.titles}`}>
        <ul className={`${styles.titles}`}>
          {this.props.items.map((f, i) =>
              <li key={i} className={`${this.selectedTextClass(i)}`}
                  onClick={() => this.onClick(i)}
                  onMouseEnter={() => this.onEnter(i)}
                  onMouseLeave={() => this.onLeave()}>
                <a onClick={() => this.onClick(i)}>{f.title}</a>
              </li>
          )}
        </ul>
      </div>
      <div className={`pure-u-md-3-4 ${styles.imagesContainer}`}>
        {this.props.items.map((f, i) =>
        <div key={i} className={`${styles.imageContent} ${this.selectedImageClass(i)}`} style={this.renderImageStyle(i, processUrl(f.imageUrl))}>
          <div className={styles.overlay} title={f.title}
               onClick={() => this.onClick(i)}
               onMouseEnter={() => this.onEnter(i)}
               onMouseLeave={() => this.onLeave()}>
          </div>
        </div>
        )}
      </div>
    </div>
  }

  selectedTextClass(i) {
    return i === this.state.current ? styles.selectedText : ''
  }

  selectedImageClass(i) {
    return i === this.state.current ? styles.selectedImage : ''
  }

  renderImageStyle(i, imageUrl) {
    if(this.state.current === i) {
      return {
        background: `url('${imageUrl}')`,
        backgroundRepeat: 'no-repeat',
        backgroundSize: 'contain',
        backgroundPosition: 'center center'
      }
    }
  }

  onClick(i) {
    if (this.state.current === i) {
      this.props.doSearch(this.props.items[i].searchTerm)
    }
  }

  onEnter(i) {
    this.cancelTimer()
    this.setState({current: i})
  }

  onLeave() {
    this.setupTimer()
  }

  setupTimer() {
    if (!this.timer && this.props.items.length > 0) {
      this.timer = setTimeout((self) => {
        const newCurrent = (self.state.current + 1) % self.props.items.length
        self.setState({current: newCurrent})
        this.timer = undefined
        self.setupTimer()
      }, 5000, this)
    }
  }

  cancelTimer() {
    if (this.props.items.length > 0 && this.timer) {
      clearTimeout(this.timer)
    }
  }

  componentDidUpdate() {
    this.setupTimer()
  }

  componentWillUnmount() {
    this.cancelTimer()
  }
}

FeaturedItemsComponent.propTypes = {
  doSearch: PropTypes.func.isRequired,
  items: PropTypes.arrayOf(PropTypes.shape({
    title: PropTypes.string,
    searchTerm: PropTypes.string,
    imageUrl: PropTypes.string
  })).isRequired
}

export default FeaturedItemsComponent
