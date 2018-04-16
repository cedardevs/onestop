import React, {Component} from 'react'
import PropTypes from 'prop-types'
import _ from 'lodash'
import A from '../common/link/Link'
import FlexRow from '../common/FlexRow'
import Expandable from '../common/Expandable'
import {
  star,
  star_o,
  star_half_o,
  info_circle,
  SvgIcon,
} from '../common/SvgIcon'

const styleStar = {
  fill: 'goldenrod',
}

const styleMissingDSMM = {
  color: 'grey',
}

class DSMMRating extends Component {
  render() {
    const {item} = this.props
    const dsmmScore = item.dsmmAverage
    const fullStars = Math.floor(dsmmScore)
    const halfStar = dsmmScore % 1 >= 0.5
    const dsmmDesc = _.round(dsmmScore, 2).toFixed(2)

    const stars = []
    if (dsmmScore === 0) {
      stars.push(
        <span key={42} className={styleMissingDSMM}>
          DSMM Rating Unavailable
        </span>
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

    return (
      <FlexRow
        items={[
          <FlexRow
            key="dsmm-stars"
            items={[
              stars,
              <span
                key="dsmm-text-value"
                style={{fontSize: '0px'}}
              >{`${dsmmDesc} DSMM rating`}</span>,
            ]}
          />,
          <Expandable
            key="dsmm-info"
            heading={
              <div aria-label="DSMM info">
                <SvgIcon path={info_circle} />
              </div>
            }
            open={false}
            content={
              <div>
                {' '}
                This is the average DSMM rating of this collection. The{' '}
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
            }
          />,
        ]}
      />
    )
  }
}

DSMMRating.propTypes = {
  id: PropTypes.string,
  item: PropTypes.object,
}

export default DSMMRating
