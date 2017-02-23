import React from 'react'
import Slider from 'react-slick'
import styles from './landing.css'
import tsunami from '../../img/tsunami.jpg'
import dem from '../../img/dem.jpg'
import ghrsst1 from '../../img/ghrsst1.jpg'
import ghrsst2 from '../../img/ghrsst2.jpg'
import CollectionTile from '../result/collections/CollectionTileComponent.jsx'
import SearchFieldsContainer from '../search/SearchFieldsContainer'

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
                <CollectionTile height={200} width={400} margin="auto" title="Port Townsend DEM"
                                onCardClick={()=>this.search('title:"Port Townsend"')} thumbnail={dem} />
            </div>
            <div>
                <CollectionTile height={200} width={400} margin="auto" title="Tsunami"
                                onCardClick={()=>this.search('Tsunami')} thumbnail={tsunami} />
            </div>
            <div>
                <CollectionTile height={200} width={400} margin="auto" title="GHRSST"
                                onCardClick={()=>this.search('GHRSST')} thumbnail={ghrsst1} />
            </div>
        </Slider>
      </div>
    const trendingContainer = <div className='container'>
      	<Slider { ...{ ...settings, autoplaySpeed: 5100} }>
            <div>
                <CollectionTile height={200} width={400} margin="auto" title="Tsunami"
                                onCardClick={()=>this.search('tsunami')} thumbnail={tsunami} />
            </div>
            <div>
                <CollectionTile height={200} width={400} margin="auto" title="GHRSST"
                                onCardClick={()=>this.search('ghrsst')} thumbnail={ghrsst2} />
            </div>
            <div>
                <CollectionTile height={200} width={400} margin="auto" title="Port Townsend DEM"
                                onCardClick={()=>this.search('title:"Port Townsend"')} thumbnail={dem} />
            </div>
        </Slider>
      </div>

    return (
      <div className={`pure-g ${styles.showcase}`}>
        <div className={`pure-u-1 ${styles.helpInfo}`}>
          <i className={`fa fa-info-circle`}></i> Search Help
          <div className={`${styles.helpText}`}>
            A simple search term will suffice to start your data discovery within the OneStop portal. However,
            a few useful querying tips can help narrow down the initial returned results:
            <ul className="fa-ul">
              <li><i className={`fa-li fa fa-cogs fa-spin`}></i>
                Use the <i className={`fa fa-clock-o ${styles.highlightB}`}></i> time and <i className={`fa fa-globe ${styles.highlightB}`}></i> space
                filters (located to the right of the input box) to limit results to only those that <u>intersect</u> the given temporal and/or spatial
                constraints. If a filter has been applied, the button background color will change from <i className={styles.highlightB}>blue</i> to
                <i className={styles.highlightP}> purple</i>.
              </li>
              <li><i className="fa-li fa fa-cogs fa-spin"></i>
                If you're searching for a phrase, wrap it in double quotes for an exact match. Note, capitalization is not important.<br/>
                Example search: <i className={styles.highlightB}>"sea surface temperature"</i>
              </li>
              <li><i className="fa-li fa fa-cogs fa-spin"></i>
                Use boolean operators to specify whether terms in your query are optional, required, or must not appear in results. The most straightforward
                operators to use are <i className={styles.highlightB}>+ (must be present) and - (must not be present)</i>. A lack of an operator designates
                a term as optional. Usage of double quotes allows for multi-word terms. The operators <i className={styles.highlightB}>OR, AND, and AND NOT </i>
                can also be used; however, these introduce operator precedence which makes for a more complicated query structure. Note that a hyphen character
                within a word will be ignored and the query will be treated like two terms.<br/>
                Example search: <i className={styles.highlightB}>temperature pressure +air -sea</i><br/>
                Example search (same logic as above): <i className={styles.highlightB}>((temperature AND air) OR (pressure AND air) OR air) AND NOT sea</i>
              </li>
              <li><i className="fa-li fa fa-cogs fa-spin"></i>
                Not sure if you misspelled something? Not to worry, simply place the fuzzy operator after the word you're unsure on.<br/>
                Example search: <i className={styles.highlightB}>ghrst~</i>
              </li>
              <li><i className="fa-li fa fa-cogs fa-spin"></i>
                The title, description, and keywords of a data set's metadata can be searched directly by appending the field name and a colon to
                the beginning of your search term (remember -- no spaces before or after the colon and wrap multi-word terms in parentheses). Exact
                matches can be requested here as well.<br/>
                Example search 1: <i className={styles.highlightB}>description:lakes</i><br/>
                Example search 2: <i className={styles.highlightB}>title:"Tsunami Inundation"</i><br/>
                Example search 3: <i className={styles.highlightB}>keywords:(ice deformation)</i>
              </li>
            </ul>
          </div>
        </div>
        <div className={`pure-u-1 ${styles.heroHeader}`}><i className={`fa fa-stop-circle-o`}></i>neStop</div>
        <div className={`pure-u-1 ${styles.heroText}`}>
          Geophysical, oceans, coastal, weather and climate data discovery all in one place.
        </div>
        <div className={`pure-u-1 ${styles.searchComponent}`}>
          <SearchFieldsContainer/>
        </div>>
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
    )
  }

  componentDidMount() {
    setTimeout(() => {
      window.dispatchEvent(new Event('resize'));
    }, 0);
  }
}

export default LandingComponent
