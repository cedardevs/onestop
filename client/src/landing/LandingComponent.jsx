import React from 'react'
import ReactCSSTransitionGroup from 'react-addons-css-transition-group'
import Slider from 'react-slick'
import styles from './landing.css'
import tsunami from '../../img/tsunami.jpg'
import CollectionTile from '../result/collections/CollectionTileComponent.jsx'
import SearchFieldsContainer from '../search/SearchFieldsContainer'

class LandingComponent extends React.Component {
  constructor(props) {
    super(props)
    this.submit = props.submit
    this.updateQuery = props.updateQuery
    this.showAbout = props.showAbout
    this.showHelp = props.showHelp
  }

  search(query) {
    this.updateQuery(query);
    this.submit(query);
  }

  componentWillUpdate(nextProps) {
    this.showAbout = nextProps.showAbout
    this.showHelp = nextProps.showHelp
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
        <img src={topic.icon}/>
        <h2>{topic.title}</h2>
      </div>
    })


    const sliderSettings = {
      autoplay: true,
      autoplaySpeed: 5000,
      dots: true,
      infinite: true,
      speed: 500,
      slidesToShow: 1,
      slidesToScroll: 1
    }
    let featured = [
      {title: 'Port Townsend DEM', term: 'title:"Port Townsend"', image: require('../../img/dem.jpg')},
      {title: 'Tsunami', term: 'tsunami', image: require('../../img/ghrsst1.jpg')},
      {title: 'GHRSST', term: 'ghrsst', image: require('../../img/ghrsst2.jpg')}
    ]
    featured = featured.map((feature, i) => {
      return <div key={i}>
        <CollectionTile margin="auto" title={feature.title} height={200} width={400}
                        onCardClick={()=>this.search(feature.term)} thumbnail={feature.image} />
      </div>
    })

    // TODO Populate this panel
    let aboutContainer
    if(this.showAbout) {
      aboutContainer = (
        <div>
          <div className={`pure-u-1 ${styles.aboutText}`}>
            <p>What Is OneStop?</p>
            The OneStop Project is designed to improve NOAA's data discovery and access framework. Focusing on all layers of the framework
            and not just the user interface, OneStop is addressing data format and metadata best practices, ensuring more data are available
            through modern web services, working to improve the relevance of dataset searches, and advancing both collection-level metadata
            management and granule level metadata systems to accommodate the wide variety and vast scale of NOAA's data.
          </div>
        </div>
      )
    }

    let helpContainer
    if(this.showHelp) {
      helpContainer = (
        <div>
          <div className={`pure-u-1 ${styles.helpText}`}>
            <b>A simple search term will suffice to start your data discovery within the OneStop portal. However,
              a few useful querying tips can help narrow down the initial returned results:</b>
            <ul className="fa-ul">
              <li className={styles.helpItem}><i className={`fa-li fa fa-check-square-o`}></i>
                Use the <i className={`fa fa-clock-o ${styles.highlightB}`}></i> time and <i
                  className={`fa fa-globe ${styles.highlightB}`}></i> space
                filters (located to the right of the input box) to limit results to only those that <u>intersect</u> the
                given temporal and/or spatial
                constraints. If a filter has been applied, the button background color will change from <i
                  className={styles.highlightB}>blue</i> to
                <i className={styles.highlightP}> purple</i>.
              </li>
              <li className={styles.helpItem}><i className="fa-li fa fa-check-square-o"></i>
                If you're searching for a phrase, wrap it in double quotes for an exact match. Note, capitalization is
                not important.<br/>
                Example search: <i className={styles.highlightP}>"sea surface temperature"</i>
              </li>
              <li className={styles.helpItem}><i className="fa-li fa fa-check-square-o"></i>
                Use boolean operators to specify whether terms in your query are optional, required, or must not appear
                in results. The most straightforward
                operators to use are <i className={styles.highlightB}>+ (must be present) and - (must not be
                  present)</i>. A lack of an operator designates
                a term as optional. Usage of double quotes allows for multi-word terms. The operators <i
                  className={styles.highlightB}>OR, AND, and AND NOT </i>
                can also be used; however, these introduce operator precedence which makes for a more complicated query
                structure. Note that a hyphen character
                within a word will be ignored and the query will be treated like two terms.<br/>
                Example search: <i className={styles.highlightP}>temperature pressure +air -sea</i><br/>
                Example search (same logic as above): <i className={styles.highlightP}>((temperature AND air) OR
                  (pressure AND air) OR air) AND NOT sea</i>
              </li>
              <li className={styles.helpItem}><i className="fa-li fa fa-check-square-o"></i>
                Not sure if you misspelled something? Not to worry, simply place the fuzzy operator after the word
                you're unsure on.<br/>
                Example search: <i className={styles.highlightP}>ghrst~</i>
              </li>
              <li className={styles.helpItem}><i className="fa-li fa fa-check-square-o"></i>
                The title, description, and keywords of a data set's metadata can be searched directly by appending the
                field name and a colon to
                the beginning of your search term (remember -- no spaces before or after the colon and wrap multi-word
                terms in parentheses). Exact
                matches can be requested here as well.<br/>
                Example search 1: <i className={styles.highlightP}>description:lakes</i><br/>
                Example search 2: <i className={styles.highlightP}>title:"Tsunami Inundation"</i><br/>
                Example search 3: <i className={styles.highlightP}>keywords:(ice deformation)</i>
              </li>
            </ul>
          </div>
        </div>
      )
    }

    return (
      <div className={`pure-g ${styles.showcase}`}>
        <ReactCSSTransitionGroup
          transitionName={ {
            enter: styles['infoPanel-enter'],
            enterActive: styles['infoPanel-enter-active'],
            leave: styles['infoPanel-leave'],
            leaveActive: styles['infoPanel-leave-active']
          } } transitionEnterTimeout={2000} transitionLeaveTimeout={2000}>
          {helpContainer}
        </ReactCSSTransitionGroup>
        <ReactCSSTransitionGroup
          transitionName={ {
            enter: styles['infoPanel-enter'],
            enterActive: styles['infoPanel-enter-active'],
            leave: styles['infoPanel-leave'],
            leaveActive: styles['infoPanel-leave-active']
          } } transitionEnterTimeout={2000} transitionLeaveTimeout={2000}>
          {aboutContainer}
        </ReactCSSTransitionGroup>
        <div className={`pure-u-1 ${styles.heroHeader}`}><i className={`fa fa-stop-circle-o`}></i>neStop</div>
        <div className={`pure-u-1 ${styles.heroText}`}>
          Geophysical, oceans, coastal, weather and climate data discovery all in one place.
        </div>
        <div className={`pure-u-1 ${styles.searchComponent}`}>
          <SearchFieldsContainer/>
        </div>
        <div className={`pure-u-1`}>
          <div className={`${styles.topicContainer}`}>
            <h2>Search by Topic:</h2>
            {topics}
          </div>
        </div>
        <div className={`pure-u-1`}>
          <h2>Featured Data Sets:</h2>
          <div className={styles.carouselContainer}>
            <div className='container'>
              <Slider {...{sliderSettings}}>
                {featured}
              </Slider>
            </div>
          </div>
        </div>
      </div>
    )
  }

  componentDidMount() {
    setTimeout(() => {
      window.dispatchEvent(new Event('resize'));
    }, 0);
  }
}

export default LandingComponent
