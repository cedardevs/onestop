import React from 'react'
import CollectionSearchContainer from '../filters/collections/CollectionSearchContainer'
import TopicsMenuContainer from './TopicsMenuContainer'
import FeaturedDatasetsContainer from './FeaturedDatasetsContainer'
import {fontFamilySerif} from '../../utils/styleUtils'
import {stop_circle_o, SvgIcon} from '../common/SvgIcon'
import Meta from '../helmet/Meta'

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
  marginBottom: '0.25em',
  fontFamily: fontFamilySerif(),
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
  marginBottom: '1.3em',
  fontFamily: fontFamilySerif(),
}

class Landing extends React.Component {
  render() {
    return (
      <div style={styleCenterContent}>
        <Meta rootSearchAction={true} />
        <div style={styleShowcase}>
          <h1 style={styleHeroHeader} aria-label="One Stop">
            <span>
              <span style={styleOneStopOImageWrapper}>
                <SvgIcon
                  size="1.1em"
                  verticalAlign="initial"
                  path={stop_circle_o}
                />
              </span>
              <span style={{display: 'none'}}>O</span>neStop
            </span>
          </h1>
          <div style={styleHeroText}>
            A NOAA Data Search Platform<br />
          </div>
          <CollectionSearchContainer home={true} />
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
