import React from 'react'
import styles from './landing.css'
import FeaturedItemsComponent from './FeaturedItemsComponent'
import SearchFieldsContainer from '../search/SearchFieldsContainer'

class LandingComponent extends React.Component {
  constructor(props) {
    super(props)
    this.submit = props.submit
    this.updateQuery = props.updateQuery
  }

  search(query) {
    this.updateQuery(query);
    this.submit(query);
  }

  render() {
    let topics = [
      {title: 'Weather', term: "weather", icon: require('../../img/topics/weather.png')},
      {title: 'Climate', term: "climate", icon: require('../../img/topics/climate.png')},
      {title: 'Satellites', term: "satellite", icon: require('../../img/topics/satellites.png')},
      {title: 'Fisheries', term: "fisheries", icon: require('../../img/topics/fisheries.png')},
      {title: 'Coasts', term: "coasts", icon: require('../../img/topics/coasts.png')},
      {title: 'Oceans', term: "oceans", icon: require('../../img/topics/oceans.png')}
    ]
    topics = topics.map((topic, i) => {
      return <div key={i} className={`${styles.topicItem}`} onClick={()=>this.search(topic.term)}>
        <img src={topic.icon} aria-hidden="true"/>
        <button title={`${topic.title}`}>{topic.title}</button>
      </div>
    })

    return (
      <div className={`pure-g ${styles.showcase}`}>
        <div className={`pure-u-1 ${styles.heroHeader}`} aria-hidden="true"><i className={`fa fa-stop-circle-o`}></i>neStop
        </div>
        <h1 className={styles.hiddenPageTitle}>OneStop: A NOAA Data Search Platform</h1>
        <div className={`pure-u-1 ${styles.heroText}`}>
          Geophysical, oceans, coastal, weather and climate data discovery all in one place.<br/>
          {this.buildCountString()}
        </div>
        <div className={`pure-u-1 ${styles.searchComponent}`}>
          <SearchFieldsContainer/>
        </div>
        <div className={`pure-u-1`}>
          <div className={`${styles.topicContainer}`} aria-labelledby="searchTopics">
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
      return <div className={`pure-u-1`} aria-labelledby="featuredDatasets">
        <h2 id="featuredDatasets">Featured Data Sets:</h2>
        <div className={`${styles.featuredContainer}`}>
          <FeaturedItemsComponent doSearch={this.search.bind(this)} items={this.props.featured}/>
        </div>
      </div>
    }
  }

  componentDidMount() {
    const evt = document.createEvent('UIEvents')
    evt.initUIEvent('resize', true, false, window, 0)
    setTimeout(() => {
      window.dispatchEvent(evt)
    }, 0)
  }

  buildCountString() {
    let hasCollections = this.props.collectionsCount !== 0
    let hasGranules = this.props.granulesCount !== 0

    let granulesString = hasGranules ? `and ${this.props.granulesCount.toLocaleString()} granules ` : ''
    let countString = hasCollections ? `${this.props.collectionsCount.toLocaleString()} collections ${granulesString} available to search` : ''
    return countString
  }
}

export default LandingComponent
