import React from 'react'
import Carousel from 'nuka-carousel'
import styles from './landing.css'
import 'font-awesome/css/font-awesome.css'

class LandingComponent extends React.Component {
  constructor(props) {
    super(props)
    this.submit = props.submit
    this.updateQuery = props.updateQuery
    this.state = {}
  }

  render() {
    let featured = [
      '//placehold.it/700x500/ffffff/c0392b/&text=featured1',
      '//placehold.it/700x500/ffffff/c0392b/&text=featured2',
      '//placehold.it/700x500/ffffff/c0392b/&text=featured3',
      '//placehold.it/700x500/ffffff/c0392b/&text=featured4',
      '//placehold.it/700x500/ffffff/c0392b/&text=featured5'
    ]
    let trending = [
      '//placehold.it/700x500/ffffff/c0392b/&text=trending1',
      '//placehold.it/700x500/ffffff/c0392b/&text=trending2',
      '//placehold.it/700x500/ffffff/c0392b/&text=trending3',
      '//placehold.it/700x500/ffffff/c0392b/&text=trending4',
      '//placehold.it/700x500/ffffff/c0392b/&text=trending5'
    ]
    const mapToSliderItem = (url, i) => {
      return <img key={i} src={url} className={`${styles.carouselImg}`}/>
    }
    featured = featured.map(mapToSliderItem)
    trending = trending.map(mapToSliderItem)

    const sliderSettings = {
      cellAlign: 'center',
      dragging: true,
      slidesToShow: 1,
      slidesToScroll: 1,
    }

    let topics = [
      {title: 'Economy', icon: 'money'},
      {title: 'Climate', icon: 'globe'},
      {title: 'Safety', icon: 'medkit'},
      {title: 'Weather', icon: 'cloud'},
      {title: 'Oceans', icon: 'anchor'},
      {title: 'Air', icon: 'plane'},
      {title: 'Solar', icon: 'sun-o'},
      {title: 'Space', icon: 'rocket'}
    ]
    topics = topics.map((topic, i) => {
      return <div key={i} className={`${styles.topicItem}`}>
        <i className={`fa fa-5x fa-${topic.icon}`} aria-hidden="true"/>
        <h3>{topic.title}</h3>
      </div>
    })

    return <div className={`pure-g`}>
      <div className={`pure-u-1`}>
        <div className={`${styles.topicContainer}`}>
          <h2>Search by Topic:</h2>
          {topics}
        </div>
      </div>
      <div className={`pure-u-1 pure-u-md-1-2`}>
        <div className={`${styles.carouselContainer}`}>
          <h2>Featured Data Sets:</h2>
          <Carousel {...sliderSettings}>
            {featured}
          </Carousel>
        </div>
      </div>
      <div className={`pure-u-1 pure-u-md-1-2`}>
        <div className={`${styles.carouselContainer}`}>
          <h2>Trending Data Sets:</h2>
          <Carousel {...sliderSettings}>
            {trending}
          </Carousel>
        </div>
      </div>
    </div>
  }
}

export default LandingComponent
