import React, { PropTypes } from 'react'
import styles from './featuredItems.css'

class FeaturedItemsComponent extends React.Component {
  constructor(props) {
    super(props)

    this.featured = [
      {title: 'Port Townsend DEM', term: 'title:"Port Townsend"', image: require('../../img/dem.jpg')},
      {title: 'Tsunami', term: 'tsunami', image: require('../../img/tsunami.jpg')},
      {title: 'GHRSST', term: 'ghrsst', image: require('../../img/ghrsst2.jpg')}
    ]

    this.state = {current: 0}
  }

  render() {
    return <div className={`pure-g ${styles.featured}`}>
      <div className={`pure-u-1 pure-u-md-1-4 ${styles.titles}`}>
        <ul className={`${styles.titles}`}>
          {this.featured.map((f, i) =>
              <li key={i} className={`${this.selectedClass(i)}`}
                  onClick={() => this.onClick(i)}
                  onMouseEnter={() => this.onEnter(i)}
                  onMouseLeave={() => this.onLeave()}>
                {f.title}
              </li>
          )}
        </ul>
      </div>
      <div className={`pure-u-md-3-4 ${styles.images}`}>
        {this.featured.map((f, i) =>
            <img key={i} src={f.image} title={f.title}
                 className={`${styles.image} ${this.selectedClass(i)}`}
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
      this.props.doSearch(this.featured[i].term)
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
    this.timer = setTimeout((self) => {
      const newCurrent = (self.state.current + 1) % self.featured.length
      self.setState({current: newCurrent})
      self.setupTimer()
    }, 4000, this)
  }

  cancelTimer() {
    clearTimeout(this.timer)
  }

  componentWillMount() {
    this.setupTimer()
  }

  componentWillUnmount() {
    this.cancelTimer()
  }
}

FeaturedItemsComponent.propTypes = {
  doSearch: PropTypes.func.isRequired
}

export default FeaturedItemsComponent
