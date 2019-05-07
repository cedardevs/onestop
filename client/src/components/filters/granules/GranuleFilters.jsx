import React from 'react'
import ReactDOM from 'react-dom'

import Expandable from '../../common/ui/Expandable'
import FlexRow from '../../common/ui/FlexRow'
import Button from '../../common/input/Button'
import FilterHeading from '../FilterHeading'

import GranuleTimeFilterContainer from './GranuleTimeFilterContainer'
// TODO: replace these with granule specific redux containers for new granule filtering
import GranuleFacetFilterContainer from './GranuleFacetFilterContainer'
// import CollectionMapFilterContainer from './spatial/CollectionMapFilterContainer'

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

const styleFocusDefault = {
  outline: 'none',
  border: `.1em dashed ${SiteColors.HEADER}`, // ems so it can be calculated into the total size easily - border + padding + margin of this style must total the same as padding in styleOverallHeading, or it will resize the element when focus changes
  padding: '.259em',
  margin: '.259em',
}

class GranuleFilters extends React.Component {
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

  componentDidMount() {
    ReactDOM.findDOMNode(this.headerRef).focus()
  }

  createFilters = () => {
    return [
      {
        name: 'location',
        heading: <FilterHeading icon={mapFilterIcon} text="Location" />,
        // TODO: replace w/new granule specific container
        // content: <CollectionMapFilterContainer isOpen={this.state.location} />,
        content: 'put granule map filter here',
      },
      {
        name: 'time',
        heading: <FilterHeading icon={timeFilterIcon} text="Date" />,
        content: <GranuleTimeFilterContainer />,
      },
      {
        name: 'keywords',
        heading: <FilterHeading icon={facetFilterIcon} text="Keywords" />,
        content: <GranuleFacetFilterContainer/>
      },
    ]
  }

  handleFocus = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: true,
      }
    })
  }

  handleBlur = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusing: false,
      }
    })
  }

  render() {
    const {closeLeft} = this.props

    const styleFocused = {
      ...(this.state.focusing ? styleFocusDefault : {}),
    }

    const styleOverallHeadingApplied = {
      ...styleOverallHeading,
      ...styleFocused,
    }

    const heading = (
      <h1
        key="filtersH1"
        tabIndex={-1}
        ref={header => (this.headerRef = header)}
        onFocus={this.handleFocus}
        onBlur={this.handleBlur}
        style={styleOverallHeadingApplied}
      >
        Filters
      </h1>
    )

    const buttonHide = (
      <Button
        key="granuleFiltersButtonHide"
        icon={arrowLeft}
        style={{borderRadius: 0}}
        styleIcon={{width: '1em', height: '1em'}}
        onClick={() => {
          closeLeft()
        }}
        title={'Hide Granule Filter Menu'}
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

export default GranuleFilters
