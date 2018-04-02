import React from 'react'

const styleGranuleSummary = {
  display: 'flex',
  margin: 0,
  padding: '0.618em 1em',
  fontSize: '1.3em',
  fontWeight: 'bold',
  backgroundColor: '#222',
  color: 'white',
  justifyContent: 'center'
}

const styleLink = {
  color: 'rgb(85, 172, 228)',
  textDecorationLine: 'underline',
}

export default class GranulesSummary extends React.Component {
  render() {
    const {totalGranuleCount, granuleSearch} = this.props

    const noGranulesSummary = (
        <div style={styleGranuleSummary}>
          No files in this collection
        </div>
    )
    const granulesSummary = (
      <div style={styleGranuleSummary}>
        <a style={styleLink} onClick={granuleSearch}>
          Show matching files
        </a>
      </div>
    )

    return totalGranuleCount == 0 ? noGranulesSummary : granulesSummary
  }
}
