import React from 'react'

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
  margin: '0 0 0.618em 0',
}

const styleH2 = {
  margin: '0 0 0.618em 0',
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
