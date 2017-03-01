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
      <div className={`pure-u-1-4 ${styles.titles}`}>
        {this.featured.map((f, i) =>
            <div key={i} className={`${styles.title} ${this.selectedClass(i)}`}
                 onMouseEnter={() => this.setState({current: i})}
                 onClick={() => this.props.doSearch(f.term)}>
              {f.title}
            </div>
        )}
      </div>
      <div className={`pure-u-3-4 ${styles.images}`}>
        {this.featured.map((f, i) =>
            <img key={i} src={f.image} title={f.title}
                 className={`${styles.image} ${this.selectedClass(i)}`}
                 onClick={() => this.props.doSearch(f.term)}/>
        )}
      </div>
    </div>
  }

  selectedClass(i) {
    return i === this.state.current ? styles.selected : ''
  }
}

FeaturedItemsComponent.propTypes = {
  doSearch: PropTypes.func.isRequired
}

export default FeaturedItemsComponent
