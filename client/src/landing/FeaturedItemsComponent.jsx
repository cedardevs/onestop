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
              <li key={i} className={`${this.selectedClass(i)}`}
                  onMouseEnter={() => this.onEnter(i)}
                  onMouseLeave={() => this.onLeave()}>
                <a onClick={() => this.onClick(i)}>{f.title}</a>
              </li>
          )}
        </ul>
      </div>
      <div className={`pure-u-md-3-4 ${styles.images}`}>
        {this.props.items.map((f, i) =>
            <img key={i} src={processUrl(f.imageUrl)} title={f.title}
                 className={`${this.selectedClass(i)}`}
                 onClick={() => this.onClick(i)}
                 onMouseEnter={() => this.onEnter(i)}
                 onMouseLeave={() => this.onLeave()}/>
        )}
      </div>
    </div>
  }

  selectedClass(i) {
    return i === this.state.current ? styles.selected : ''
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
    if (this.props.items.length > 0) {
      this.timer = setTimeout((self) => {
        const newCurrent = (self.state.current + 1) % self.props.items.length
        self.setState({current: newCurrent})
        self.setupTimer()
      }, 5000, this)
    }
  }

  cancelTimer() {
    if (this.props.items.length > 0 && this.timer) {
      clearTimeout(this.timer)
    }
  }

  componentWillMount() {
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
