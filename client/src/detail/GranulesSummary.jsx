import React from 'react'
import Button from '../common/input/Button'

const styleGranuleSummary = {
  display: 'flex',
  justifyContent: 'center',
}

const styleLink = {
  textDecoration: 'underline',
  color: '#2f668a',
}

export default class GranulesSummary extends React.Component {
  render() {
    const {totalGranuleCount, navigateToGranules} = this.props

    const noGranulesSummary = (
      <div style={styleGranuleSummary}>No files in this collection</div>
    )

    const linkText = `Show ${totalGranuleCount} matching files`

    const granulesSummary = (
      <div style={styleGranuleSummary}>
        <a style={styleLink} onClick={navigateToGranules}>
          {linkText}
        </a>
      </div>
    )

    return totalGranuleCount == 0 ? noGranulesSummary : granulesSummary
  }
}
