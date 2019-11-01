import React from 'react'
import PropTypes from 'prop-types'
import _ from 'lodash'
import A from '../../common/link/Link'
import FlexRow from '../../common/ui/FlexRow'
import Expandable from '../../common/ui/Expandable'
import {
  star,
  star_o,
  star_half_o,
  info_circle,
  SvgIcon,
} from '../../common/SvgIcon'

const styleStar = {
  // default to text color for stars
}

const styleMissingDSMM = {
  color: 'grey',
}

const styleInfoButton = {
  marginLeft: '0.309em',
  padding: '0 0.309em',
  border: 0,
  boxSizing: 'content-box',
  background: 'none',
  color: 'inherit',
  font: 'inherit',
  lineHeight: 'normal',
  overflow: 'visible',
  userSelect: 'none',
}

const styleInfoButtonFocused = {
  outline: '2px dashed #00002c',
}

const styleShowHideText = {
  marginLeft: '0.309em',
  textDecoration: 'underline',
}

const styleExpandableInfoContent = {
  background: '#efefef',
  borderRadius: '0.309em',
  padding: '0.618em',
}

class DSMMRating extends React.Component {
  UNSAFE_componentWillMount() {
    this.setState({
      showInfo: false,
      focusingShowInfo: false,
    })
  }

  handleShowInfo = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        showInfo: true,
      }
    })
  }

  handleHideInfo = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        showInfo: false,
      }
    })
  }

  handleShowInfoFocus = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusingShowInfo: true,
      }
    })
  }

  handleShowInfoBlur = event => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusingShowInfo: false,
      }
    })
  }

  render() {
    const {item} = this.props
    const dsmmScore = item.dsmmAverage
    const fullStars = Math.floor(dsmmScore)
    const halfStar = dsmmScore % 1 >= 0.5
    const dsmmDesc = _.round(dsmmScore, 2).toFixed(2)

    const stars = []
    if (dsmmScore === 0) {
      stars.push(
        <div key={42} className={styleMissingDSMM}>
          DSMM Rating Unavailable
        </div>
      )
    }
    else {
      for (let i = 0; i < 5; i++) {
        let starType
        if (i < fullStars) {
          starType = star
        }
        else if (i === fullStars && halfStar) {
          starType = star_half_o
        }
        else {
          starType = star_o
        }
        stars.push(
          <SvgIcon key={`dsmm-star-${i}`} style={styleStar} path={starType} />
        )
      }
    }

    const styleInfoButtonMerged = {
      ...styleInfoButton,
      ...(this.state.focusingShowInfo ? styleInfoButtonFocused : {}),
    }

    const infoButton = (
      <button
        key="dsmm-info-button"
        aria-label="DSMM info"
        style={styleInfoButtonMerged}
        aria-expanded={this.state.showInfo}
        onClick={
          this.state.showInfo ? this.handleHideInfo : this.handleShowInfo
        }
        onFocus={this.handleShowInfoFocus}
        onBlur={this.handleShowInfoBlur}
      >
        <SvgIcon path={info_circle} size="1em" />
        <span style={styleShowHideText}>
          {this.state.showInfo ? 'hide ' : 'show '}info
        </span>
      </button>
    )

    const expandableInfo = (
      <Expandable
        key="dsmm-info"
        open={this.state.showInfo}
        content={
          <div style={styleExpandableInfoContent}>
            <p>The average DSMM rating of this collection is {dsmmDesc}.</p>
            <div>
              The{' '}
              <A
                href="http://doi.org/10.2481/dsj.14-049"
                target="_blank"
                title="Data Stewardship Maturity Matrix Information"
              >
                Data Stewardship Maturity Matrix (DSMM)
              </A>{' '}
              is a unified framework that defines criteria for the following
              nine components based on measurable practices:
              <ul>
                <li>Accessibility</li>
                <li>Data Integrity</li>
                <li>Data Quality Assessment</li>
                <li>Data Quality Assurance</li>
                <li>Data Quality Control Monitoring</li>
                <li>Preservability</li>
                <li>Production Sustainability</li>
                <li>Transparency Traceability</li>
                <li>Usability</li>
              </ul>
            </div>
          </div>
        }
      />
    )

    return (
      <div>
        <FlexRow
          style={{marginBottom: '0.618em'}}
          items={[
            <div key="dsmm-stars" title={`${dsmmDesc} DSMM rating`}>
              {stars}
            </div>,
            infoButton,
          ]}
        />
        {expandableInfo}
      </div>
    )
  }
}

DSMMRating.propTypes = {
  id: PropTypes.string,
  item: PropTypes.object,
}

export default DSMMRating
