import React from 'react'
import SearchFieldsContainer from '../search/SearchFieldsContainer'
import TopicsMenuContainer from './TopicsMenuContainer'
import FeaturedDatasetsContainer from './FeaturedDatasetsContainer'
import defaultStyles from '../common/defaultStyles'

import {stop_circle_o, SvgIcon} from '../common/SvgIcon'

const styleCenterContent = {
  display: 'flex',
  justifyContent: 'center',
  color: '#222',
  fill: '#222',
}

const styleShowcase = {
  maxWidth: '80em',
  padding: '0 1.618em 10em 1.618em',
  minHeight: '100vh',
}

const styleHeroHeader = {
  textAlign: 'center',
  fontSize: '3.5em',
  marginTop: '0.5em',
}

const styleOneStopOImageWrapper = {
  position: 'relative',
  top: '.15em',
  left: '.07em',
}

const styleHeroText = {
  textAlign: 'center',
  fontSize: '1.5em',
  fontStyle: 'italic',
  lineHeight: '1.5',
  paddingBottom: '0.25em',
}

class Landing extends React.Component {
  constructor(props) {
    super(props)
    this.submit = props.submit
    this.updateQuery = props.updateQuery
  }

  render() {
    return (
      <div style={styleCenterContent}>
        <div style={styleShowcase}>
          <h1 style={styleHeroHeader} aria-label="One Stop">
            <span style={styleOneStopOImageWrapper}>
              <SvgIcon size="1.1em" path={stop_circle_o} />
            </span>neStop
            <div style={defaultStyles.hideOffscreen}>
              A NOAA Data Search Platform
            </div>
          </h1>
          <div style={styleHeroText}>
            Geophysical, oceans, coastal, weather and climate data discovery all
            in one place.<br />
          </div>
          <SearchFieldsContainer home={true} />
          <TopicsMenuContainer />
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
