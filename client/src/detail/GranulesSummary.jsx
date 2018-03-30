import React from 'react'

const styleLink = {
  display: 'inline-block',
  color: 'rgb(85, 172, 228)',
  margin: '0 0 0.618em 0',
  textDecorationLine: 'underline',
}

export default class GranulesSummary extends React.Component {
  render() {
    const {totalGranuleCount, granuleSearch} = this.props

    const noGranulesSummary = <div>No granules in this collection</div>
    const granulesSummary = (
      <div>
        <a style={styleLink} onClick={granuleSearch}>
          Show Files Matching My Search
        </a>
      </div>
    )

    return totalGranuleCount == 0 ? noGranulesSummary : granulesSummary
  }
}
