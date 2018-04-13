import React from 'react'
import {fontFamilySerif} from '../../utils/styleUtils'
import {SiteColors} from '../../common/defaultStyles'

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
  marginTop: '1.618em',
  border: 'gray solid',
  padding: '1em',
  background: '#F9F9F9',
}

export default class Help extends React.Component {
  render() {
    return (
      <div style={styleWrapper}>
        <section style={styleAbout}>
          <h1 style={styleH1}>OneStop Overview</h1>
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
          <h2 style={styleH2}>{this.buildCountString()}</h2>
          <div style={styleAccessibilityStatement}>
            <h2 style={styleH2}>Accessibility Statement</h2>
            <p>
              NOAA OneStop is committed to providing access to all individuals
              who are seeking information from our website. We strive to meet or
              exceed requirements of Section 508 of the Rehabilitation Act, as
              amended in 1998.
            </p>
            <p>
              We recognize not all pages on our site are fully accessible at
              this time, however, we aim to meet Level AA accessibility for all
              of our features. We will continue to make improvements across our
              entire site until all pages are fully compliant.
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
