import React from 'react'
import SearchFieldsContainer from '../search/SearchFieldsContainer'
import TopicsMenuContainer from './TopicsMenuContainer'
import FeaturedDatasetsContainer from './FeaturedDatasetsContainer'
import stopCircle from 'fa/stop-circle-o.svg'
import defaultStyles from '../common/defaultStyles'

class Landing extends React.Component {
  constructor(props) {
    super(props)
    this.submit = props.submit
    this.updateQuery = props.updateQuery
  }

  render() {
    const styleShowcase = {
      margin: '0 auto',
      maxWidth: '80em',
      padding: '0 1.618em 10em 1.618em',
      minHeight: '100vh',
    }

    const styleHeroHeader = {
      textAlign: 'center',
      fontSize: '3.5em',
      marginTop: '0.5em',
    }

    const styleOneStopOImage = {
      position: 'relative',
      top: '.15em',
      left: '.07em',
      maxWidth: '1.1em',
      maxHeight: '1.1em',
    }

    const styleHeroText = {
      textAlign: 'center',
      fontSize: '1.5em',
      fontStyle: 'italic',
      lineHeight: '1.5',
      paddingBottom: '0.25em',
    }

    const styleCenterContent = {
      display: 'flex',
      justifyContent: 'center',
    }

    return (
      <div style={styleCenterContent}>
        <div style={styleShowcase}>
          <div style={styleHeroHeader} aria-hidden="true">
            <img style={styleOneStopOImage} alt="O" src={stopCircle} />neStop
          </div>
          <h1 style={defaultStyles.hideOffscreen}>
            OneStop: A NOAA Data Search Platform
          </h1>
          <div style={styleHeroText}>
            Geophysical, oceans, coastal, weather and climate data discovery all
            in one place.<br />
          </div>
          <SearchFieldsContainer home={true} />
          <div>
            <div aria-labelledby="searchTopics">
              <h2 id="searchTopics">Search by Topic:</h2>
              <TopicsMenuContainer />
            </div>
          </div>
          <FeaturedDatasetsContainer />
        </div>
      </div>
    )
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
