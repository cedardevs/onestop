import React from 'react'
import Carousel from 'nuka-carousel'
import styles from './landing.css'

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
      return <img key={i} src={url} className={`${styles.sliderImg}`}/>
    }
    featured = featured.map(mapToSliderItem)
    trending = trending.map(mapToSliderItem)

    const sliderSettings = {
      cellAlign: 'center',
      dragging: true,
      slidesToShow: 1,
      slidesToScroll: 1,
    }

    return <div className={`pure-g`}>
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
