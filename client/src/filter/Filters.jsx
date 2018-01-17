import React, {Component} from 'react'

import Expandable from '../common/Expandable'
import FlexRow from '../common/FlexRow'
import Button from '../common/input/Button'
import FilterHeading from './FilterHeading'
import TimeFilterContainer from './time/TimeFilterContainer'
import FacetFilterContainer from './facet/FacetFilterContainer'
import MapFilterContainer from './spatial/MapFilterContainer'

import mapFilterIcon from '../../img/font-awesome/white/svg/globe.svg'
import timeFilterIcon from '../../img/font-awesome/white/svg/calendar.svg'
import facetFilterIcon from '../../img/font-awesome/white/svg/key.svg'

import arrowLeft from '../../img/font-awesome/white/svg/arrow-left.svg'

const styleFilters = {
  borderTop: '1px solid white',
}

const styleFilterHeadings = {
  fontWeight: 'bold',
  backgroundColor: '#0E274E',
  padding: '0.618em',
  borderBottom: '1px solid white',
}

const styleFilterContents = {
  borderBottom: '1px solid white',
}

const styleFacetFilterContents = {
  marginNest: '1em',
  backgroundColor: '#327CAC',
}

class Filters extends Component {
  constructor(props) {
    super(props)

    this.filters = [
      {
        name: 'location',
        heading: <FilterHeading icon={mapFilterIcon} text="Location" />,
        content: <MapFilterContainer />,
      },
      {
        name: 'time',
        heading: <FilterHeading icon={timeFilterIcon} text="Time" />,
        content: <TimeFilterContainer />,
      },
      {
        name: 'keywords',
        heading: <FilterHeading icon={facetFilterIcon} text="Keywords" />,
        content: (
          <FacetFilterContainer
            submit={props.submit}
            marginNest={styleFacetFilterContents.marginNest}
            backgroundColor={styleFacetFilterContents.backgroundColor}
          />
        ),
      },
    ]

    this.state = {
      openIndex: -1,
    }
  }

  render() {
    const {showLeft, toggleLeft} = this.props

    const heading = (
      <h1
        key="filtersH1"
        style={{
          fontSize: '1.309em',
          color: 'white',
          padding: '0.618em',
          margin: 0,
        }}
      >
        Filters
      </h1>
    )
    const buttonHide = (
      <Button
        key="filtersButtonHide"
        icon={arrowLeft}
        style={{borderRadius: 0}}
        styleIcon={{width: '1em', height: 'initial'}}
        onClick={() => {
          if (showLeft) {
            toggleLeft()
          }
        }}
        title={'Hide Filter Menu'}
        ariaExpanded={true}
      />
    )

    const expandableFilters = this.filters.map((filter, index) => {
      return (
        <div key={index} style={styleFilters}>
          <Expandable
            key={index}
            value={index}
            showArrow={true}
            heading={filter.heading}
            styleHeading={styleFilterHeadings}
            content={filter.content}
            styleContent={styleFilterContents}
          />
        </div>
      )
    })

    return (
      <div>
        <FlexRow
          items={[ heading, buttonHide ]}
          style={{
            justifyContent: 'space-between',
            backgroundColor: '#242C36',
            borderTop: '1px solid #FFF',
          }}
        />
        {expandableFilters}
      </div>
    )
  }
}

export default Filters
