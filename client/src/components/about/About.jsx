import React from 'react'
import {fontFamilySerif} from '../../utils/styleUtils'
import {SiteColors} from '../../style/defaultStyles'
import {stop_circle_o, SvgIcon} from '../common/SvgIcon'
import A from '../common/link/Link'
import Meta from '../helmet/Meta'

const styleWrapper = {
  width: '100%',
  color: '#111',
}

const styleAbout = {
  fontSize: '1.318em',
  padding: '1.618em',
  minHeight: '100vh',
  margin: '0 auto',
  maxWidth: '45em',
}

const styleH1 = {
  fontFamily: fontFamilySerif(),
  fontSize: '1.5em',
  margin: '0 0 0.618em 0',
}

const styleH2 = {
  fontFamily: fontFamilySerif(),
  fontSize: '1.1em',
  margin: '0 0 0.618em 0',
}

const styleAccessibilityStatement = {
  margin: '1.618em 0',
  border: 'gray solid',
  padding: '1em',
  background: '#F9F9F9',
}

const styleOneStopOImageWrapper = {
  position: 'relative',
  top: '.15em',
  left: '.07em',
}

export default class About extends React.Component {
  render() {
    return (
      <div style={styleWrapper}>
        <Meta title="About NOAA OneStop" />
        <section style={styleAbout}>
          <h1 style={styleH1} aria-label="One Stop Overview">
            <span>
              <span style={styleOneStopOImageWrapper}>
                <SvgIcon
                  size="1.1em"
                  verticalAlign="initial"
                  path={stop_circle_o}
                />
              </span>
              <span style={{display: 'none'}}>O</span>neStop Overview
            </span>
          </h1>
          <p>
            A NOAA Data Search Platform. Geophysical, oceans, coastal, weather
            and climate data discovery all in one place.
          </p>
          <p>
            The OneStop Project is designed to improve NOAA's data discovery and
            access framework. Focusing on all layers of the framework and not
            just the user interface, OneStop is addressing data format and
            metadata best practices, ensuring more data are available through
            modern web services, working to improve the relevance of dataset
            searches, and advancing both collection-level metadata management
            and granule-level metadata systems to accommodate the wide variety
            and vast scale of NOAA's data.
          </p>
          <h2 style={styleH2}>Catalog Information</h2>
          <p>{this.buildCountString()}</p>
          <div style={styleAccessibilityStatement}>
            <h2 style={styleH2}>Accessibility Statement</h2>
            <p>
              NOAA OneStop is committed to providing access to all individuals
              who are seeking information from our website. We strive to meet or
              exceed requirements of Section 508 of the Rehabilitation Act, as
              amended in 1998.
            </p>
            <p>
              We have completed a 3rd party review that confirms the site meets
              all WCAG 2.1 Level A accessibility requirements, we expect all of
              our features to meet Level AA standards by our next release, and
              we will continue to make improvements across our entire site
              aiming for AAA compliance where possible.
            </p>
            <p>
              If you experience any challenges while accessing parts of our
              site, please contact{' '}
              <a
                href={'mailto:ncei.info@noaa.gov'}
                style={{color: SiteColors.LINK}}
              >
                ncei.info@noaa.gov
              </a>
            </p>
          </div>
          <h2 style={styleH2} id="attribution">
            Image Attribution
          </h2>
          <p>
            Background image,{' '}
            <A
              href={
                'https://www.toptal.com/designers/subtlepatterns/topography/'
              }
              style={{color: SiteColors.LINK}}
            >
              'Topography', made by Shankar Ganesh
            </A>,{' '}
            <A
              href={'https://creativecommons.org/licenses/by-sa/3.0/'}
              style={{color: SiteColors.LINK}}
            >
              CC BY-SA 3.0
            </A>{' '}
            - Subtle Patterns © Toptal Designers
          </p>
        </section>
      </div>
    )
  }

  buildCountString() {
    let hasCollections = this.props.collectionsCount !== 0
    let hasGranules = this.props.granulesCount !== 0

    let granulesString = hasGranules
      ? `and ${this.props.granulesCount.toLocaleString()} granules `
      : ''
    let countString = hasCollections
      ? `Currently, OneStop has ${this.props.collectionsCount.toLocaleString()} collections ${granulesString} available to search.`
      : ''
    return countString
  }
}
