import React from 'react'
import ReactDOM from 'react-dom'

import Expandable from '../../common/ui/Expandable'
import FlexRow from '../../common/ui/FlexRow'
import Button from '../../common/input/Button'
import FilterHeading from '../FilterHeading'
import CollectionTimeFilterContainer from './CollectionTimeFilterContainer'
import CollectionFacetFilterContainer from './CollectionFacetFilterContainer'
import CollectionMapFilterContainer from './CollectionMapFilterContainer'

import mapFilterIcon from '../../../../img/font-awesome/white/svg/globe.svg'
import timeFilterIcon from '../../../../img/font-awesome/white/svg/calendar.svg'
import facetFilterIcon from '../../../../img/font-awesome/white/svg/key.svg'

import arrowLeft from '../../../../img/font-awesome/white/svg/arrow-left.svg'
import {fontFamilySerif} from '../../../utils/styleUtils'

import {
  FilterColors,
  FilterStyles,
  SiteStyles,
  SiteColors,
} from '../../../style/defaultStyles'

const styleFilters = {
  borderTop: `1px solid ${FilterColors.MEDIUM}`,
}

const styleFilterHeadings = {
  ...FilterStyles.DARKEST,
  ...{
    fontWeight: 'bold',
    padding: '0.618em',
  },
}

const styleOverallHeading = {
  ...SiteStyles.HEADER,
  ...{
    fontFamily: fontFamilySerif(),
    fontSize: '1.2em',
    fontWeight: 'normal',
    letterSpacing: '0.05em',
    padding: '0.618em',
    margin: 0,
  },
}

class CollectionFilters extends React.Component {
  constructor(props) {
    super(props)
    this.state = this.initialState()
  }

  initialState() {
    return {
      location: false,
      time: false,
      keywords: false,
    }
  }

  handleFilterToggle = event => {
    let toggledFilter = event.value
    this.setState({
      [toggledFilter]: event.open,
    })
  }

  createFilters = () => {
    return [
      {
        name: 'location',
        heading: <FilterHeading icon={mapFilterIcon} text="Location" />,
        content: <CollectionMapFilterContainer isOpen={this.state.location} />,
      },
      {
        name: 'time',
        heading: <FilterHeading icon={timeFilterIcon} text="Date" />,
        content: <CollectionTimeFilterContainer />,
      },
      {
        name: 'keywords',
        heading: <FilterHeading icon={facetFilterIcon} text="Attributes" />,
        content: <CollectionFacetFilterContainer marginNest={'1em'} />,
      },
    ]
  }

  render() {
    const {closeLeft} = this.props

    const heading = (
      <h2 key="filtersH1" style={styleOverallHeading}>
        Collection Filters
      </h2>
    )

    const buttonHide = (
      <Button
        key="filtersButtonHide"
        icon={arrowLeft}
        style={{borderRadius: 0}}
        styleIcon={{width: '1em', height: '1em'}}
        onClick={() => {
          closeLeft()
        }}
        title={'Hide Filter Menu'}
        ariaExpanded={true}
      />
    )

    let filters = this.createFilters()

    const expandableFilters = filters.map((filter, index) => {
      return (
        <div key={index} style={styleFilters}>
          <Expandable
            key={index}
            value={filter.name}
            open={this.state[filter.name]}
            showArrow={true}
            heading={filter.heading}
            styleHeading={styleFilterHeadings}
            content={filter.content}
            onToggle={this.handleFilterToggle}
          />
        </div>
      )
    })

    return (
      <div>
        <FlexRow
          items={[ heading, buttonHide ]}
          style={{
            ...SiteStyles.HEADER,
            ...{
              justifyContent: 'space-between',
              borderTop: `1px solid ${SiteColors.HEADER_TEXT}`,
            },
          }}
        />
        {expandableFilters}
      </div>
    )
  }
}

export default CollectionFilters
