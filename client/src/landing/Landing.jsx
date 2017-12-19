import React from 'react'
import styles from './landing.css'
import FeaturedItems from './FeaturedItems'
import SearchFieldsContainer from '../search/SearchFieldsContainer'
import stopCircle from 'fa/stop-circle-o.svg'
import defaultStyles from '../common/defaultStyles'

class Landing extends React.Component {
  constructor(props) {
    super(props)
    this.submit = props.submit
    this.updateQuery = props.updateQuery
  }

  search(query) {
    this.updateQuery(query)
    this.submit(query)
  }

  render() {
    let topics = [
      {
        title: 'Weather',
        term: 'weather',
        icon: require('../../img/topics/weather.png'),
      },
      {
        title: 'Climate',
        term: 'climate',
        icon: require('../../img/topics/climate.png'),
      },
      {
        title: 'Satellites',
        term: 'satellite',
        icon: require('../../img/topics/satellites.png'),
      },
      {
        title: 'Fisheries',
        term: 'fisheries',
        icon: require('../../img/topics/fisheries.png'),
      },
      {
        title: 'Coasts',
        term: 'coasts',
        icon: require('../../img/topics/coasts.png'),
      },
      {
        title: 'Oceans',
        term: 'oceans',
        icon: require('../../img/topics/oceans.png'),
      },
    ]
    topics = topics.map((topic, i) => {
      return (
        <div
          key={i}
          className={`${styles.topicItem}`}
          onClick={() => this.search(topic.term)}
        >
          <button>
            <img src={topic.icon} alt={topic.title} aria-hidden="true" />
            <div>{topic.title}</div>
          </button>
        </div>
      )
    })

    return (
      <div className={`${styles.showcase}`}>
        <div className={`${styles.heroHeader}`} aria-hidden="true">
          <img alt="O" src={stopCircle} />neStop
        </div>
        <h1 style={defaultStyles.hideOffscreen}>
          OneStop: A NOAA Data Search Platform
        </h1>
        <div className={`${styles.heroText}`}>
          Geophysical, oceans, coastal, weather and climate data discovery all
          in one place.<br />
        </div>
        <div className={`${styles.searchComponent}`}>
          <SearchFieldsContainer home={true} />
        </div>
        <div>
          <div
            className={`${styles.topicContainer}`}
            aria-labelledby="searchTopics"
          >
            <h2 id="searchTopics">Search by Topic:</h2>
            <ul>{topics}</ul>
          </div>
        </div>
        {this.renderFeatured()}
      </div>
    )
  }

  renderFeatured() {
    if (this.props.featured) {
      return (
        <div className={`pure-u-1`} aria-labelledby="featuredDatasets">
          <h2 id="featuredDatasets">Featured Data Sets:</h2>
          <div className={`${styles.featuredContainer}`}>
            <FeaturedItems
              doSearch={this.search.bind(this)}
              items={this.props.featured}
            />
          </div>
        </div>
      )
    }
  }

  componentDidMount() {
    const evt = document.createEvent('UIEvents')
    evt.initUIEvent('resize', true, false, window, 0)
    setTimeout(() => {
      window.dispatchEvent(evt)
    }, 0)
  }
}

export default Landing
