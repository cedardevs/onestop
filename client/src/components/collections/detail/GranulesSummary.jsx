import React from 'react'
import Button from '../../common/input/Button'
import {SiteColors} from '../../../style/defaultStyles'

const styleGranuleSummary = {
  display: 'flex',
  justifyContent: 'center',
}

const styleLink = {
  textDecoration: 'underline',
  color: SiteColors.LINK,
  fontSize: '1em',
  background: 'transparent',
  padding: '0',
  outline: 'none',
}

const styleLinkFocus = {
  outline: '2px dashed #5C87AC',
  outlineOffset: '.3em',
  background: 'transparent',
}

const styleLinkHover = {
  background: 'transparent',
}

export default class GranulesSummary extends React.Component {
  render() {
    const {
      totalGranuleCount,
      totalGranuleFilteredCount,
      navigateToGranules,
    } = this.props

    const noGranulesSummary = (
      <div style={styleGranuleSummary}>
        Files in this collection are not currently searchable
      </div>
    )

    const linkText =
      totalGranuleFilteredCount == totalGranuleCount
        ? `Show all ${totalGranuleCount} files in collection`
        : `Show ${totalGranuleFilteredCount}  matching files of ${totalGranuleCount} in collection`

    // TODO 508 this should probably be a link, not a button
    const granulesSummary = (
      <div style={styleGranuleSummary}>
        <Button
          style={styleLink}
          styleHover={styleLinkHover}
          styleFocus={styleLinkFocus}
          onClick={navigateToGranules}
        >
          {linkText}
        </Button>
      </div>
    )

    return totalGranuleCount == 0 ? noGranulesSummary : granulesSummary
  }
}
