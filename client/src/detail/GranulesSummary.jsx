import React from 'react'
import Button from '../common/input/Button'
import filesIcon from 'fa/files-o.svg'

const styleGranuleSummary = {
  display: 'flex',
  margin: 0,
  padding: 0,
  fontSize: '1.3em',
  fontWeight: 'bold',
  color: 'black',
}

const styleGranulesButton = {
  fontSize: '1em',
}

const styleGranulesButtonHover = {
  cursor: 'pointer',
}

const styleIcon = {
  width: '1.2em',
  height: '1.2em',
  padding: '0 0.309em 0 0',
}

export default class GranulesSummary extends React.Component {
  render() {
    const {totalGranuleCount, navigateToGranules} = this.props

    const noGranulesSummary = (
      <div style={styleGranuleSummary}>No files in this collection</div>
    )
    const granulesSummary = (
      <div style={styleGranuleSummary}>
        <Button
          text="Show matching files"
          icon={filesIcon}
          onClick={navigateToGranules}
          style={styleGranulesButton}
          styleHover={styleGranulesButtonHover}
          styleIcon={styleIcon}
        />
      </div>
    )

    return totalGranuleCount == 0 ? noGranulesSummary : granulesSummary
  }
}
