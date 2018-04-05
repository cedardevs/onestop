import React from 'react'
import Button from '../common/input/Button'

const styleGranuleSummary = {
  display: 'flex',
  // margin: 0,
  // padding: 0,
  // fontSize: '1.3em',
  // fontWeight: 'bold',
  // color: 'black',
  justifyContent: 'center',
}

const styleLink = {
  textDecoration: 'underline',
  color: '#7777EE',
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

    const linkText = `Show ${totalGranuleCount} matching files`

    const granulesSummary = (
      <div style={styleGranuleSummary}>
        <a style={styleLink} onClick={navigateToGranules}>
          {linkText}
        </a>
        {/*<Button*/}
        {/*text={`Show ${totalGranuleCount} matching files`}*/}
        {/*icon={filesIcon}*/}
        {/*onClick={navigateToGranules}*/}
        {/*style={styleGranulesButton}*/}
        {/*styleHover={styleGranulesButtonHover}*/}
        {/*styleIcon={styleIcon}*/}
        {/*/>*/}
      </div>
    )

    return totalGranuleCount == 0 ? noGranulesSummary : granulesSummary
  }
}
