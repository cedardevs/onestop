import React from 'react'
import Slider from 'react-slick'
import styles from './landing.css'
import image1 from '../../img/ghrsst2.jpg'
import image4 from '../../img/tsunami04.jpg'
import dem2 from '../../img/dem2.jpg'
import ghrsst1 from '../../img/ghrsst1.jpg'

class LandingComponent extends React.Component {
  constructor(props) {
    super(props)
    this.submit = props.submit
    this.updateQuery = props.updateQuery
    this.state = {}
  }

  search(query) {
    this.updateQuery(query);
    this.submit(query);
  }

  render() {
    const mapToSliderItem = (url, i) => {
      return <img key={i} src={url} className={`${styles.carouselImg}`}/>
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
      return <div key={i} className={`${styles.topicItem}`} onClick={()=>this.search(topic.title.toLowerCase())}>
        <i className={`fa fa-5x fa-${topic.icon}`} aria-hidden="true"/>
        <h3>{topic.title}</h3>
      </div>
    })

    var settings = {
      autoplay: true,
      dots: true,
      infinite: true,
      speed: 500,
      slidesToShow: 1,
      slidesToScroll: 1
    }
    // Hard-coded for display, TODO: dynamically pull from API
    const featuredContainer = <div className='container'>
      	<Slider { ...{ ...settings, autoplaySpeed: 5000} }>
            <div>
              <p onClick={()=>this.search('title:"Port Townsend"')} className={`${styles.containerItemTitle}`}>  Port Townsend DEM </p>
              <img onClick={()=>this.search('title:"Port Townsend"')} className={`${styles.containerItemImage}`} src={dem2} />
            </div>
            <div>
                <p onClick={()=>this.search('tsunami')} className={`${styles.containerItemTitle}`}> Tsunami  </p>
                <img onClick={()=>this.search('tsunami')} className={`${styles.containerItemImage}`} src={image4} />
            </div>
            <div>
              <p onClick={()=>this.search('GHRSST')} className={`${styles.containerItemTitle}`}> GHRSST </p>
              <img onClick={()=>this.search('GHRSST')} className={`${styles.containerItemImage}`} src={ghrsst1} />
            </div>
        </Slider>
      </div>
    const trendingContainer = <div className='container'>
      	<Slider { ...{ ...settings, autoplaySpeed: 5100} }>
        	<div>
              <p onClick={()=>this.search('GHRSST')} className={`${styles.containerItemTitle}`}> GHRSST </p>
              <img onClick={()=>this.search('GHRSST')} className={`${styles.containerItemImage}`} src={image1} />
            </div>
            <div>
                <p onClick={()=>this.search('title:"Port Townsend"')} className={`${styles.containerItemTitle}`}>  Port Townsend DEM </p>
                <img onClick={()=>this.search('title:"Port Townsend"')} className={`${styles.containerItemImage}`} src={dem2} />
            </div>
            <div>
              <p onClick={()=>this.search('tsunami')} className={`${styles.containerItemTitle}`}> Tsunami  </p>
              <img onClick={()=>this.search('tsunami')} className={`${styles.containerItemImage}`} src={image4} />
            </div>
        </Slider>
      </div>

    return <div className={`pure-g ${styles.showcase}`}>
      <div className={`pure-u-1`}>
        <div className={`${styles.topicContainer}`}>
          <h2>Search by Topic:</h2>
            {topics}
        </div>
      </div>
      <div className={`pure-u-1 pure-u-md-1-2`}>
          <h2>Featured Data Sets:</h2>
          <div className={styles.carouselContainer}>
            {featuredContainer}
          </div>
      </div>
      <div className={`pure-u-1 pure-u-md-1-2`}>
          <h2>Trending Data Sets:</h2>
          <div className={styles.carouselContainer}>
            {trendingContainer}
          </div>
      </div>
    </div>
  }

  componentDidMount() {
    setTimeout(() => {
      window.dispatchEvent(new Event('resize'));
    }, 0);
  }
}

export default LandingComponent
