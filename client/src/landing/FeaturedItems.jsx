import React from 'react'
import PropTypes from 'prop-types'
import { processUrl } from '../utils/urlUtils'
import styles from './featuredItems.css'

class FeaturedItems extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      current: 0,
      carouselLength: 0,
      timer: undefined
    }
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
            <div key={i} className={`${styles.imageContent} ${this.selectedImageClass(i)}`}
                 style={this.renderImageStyle(i, processUrl(f.imageUrl))}>
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
    if (this.state.current === i) {
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
    this.state.timer.pause()
    this.setState({current: i})
  }

  onLeave() {
    this.state.timer.resume()
  }

  setupTimer(items) {
    const {carouselLength, timer} = this.state

    if (items && (items.length || carouselLength !== items.length)) {
      this.setState({carouselLength: items.length})

      function Timer(callback, delay) {
        var timerId, start, remaining = delay

        this.pause = () => {
          window.clearTimeout(timerId)
          remaining -= new Date() - start
        }

        this.resume = () => {
          start = new Date()
          window.clearTimeout(timerId)
          timerId = window.setTimeout(callback, remaining)
        }

        this.resume()
      }

      const self = this
      if (!timer) (function setTimerState() {
        self.setState({
          timer: new Timer(() => {
            const {carouselLength, current} = self.state
            const newCurrent = (current + 1) % carouselLength
            self.setState({current: newCurrent})
            setTimerState()
          }, 5000)
        })
      })()
    }
  }

  componentWillReceiveProps({items}) {
    this.setupTimer(items)
  }

  componentDidMount() {
    this.setupTimer(this.props.items)
  }

  componentWillUnmount() {
    this.state.timer.pause()
  }
}

FeaturedItems.propTypes = {
  doSearch: PropTypes.func.isRequired,
  items: PropTypes.arrayOf(PropTypes.shape({
    title: PropTypes.string,
    searchTerm: PropTypes.string,
    imageUrl: PropTypes.string
  })).isRequired
}

export default FeaturedItems
