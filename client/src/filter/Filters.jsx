import React, {Component} from 'react'
import ReactDOM from 'react-dom'

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
import {fontFamilySerif} from '../utils/styleUtils'

import {FilterColors, FilterStyles, SiteTheme} from '../common/defaultStyles'

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
  ...SiteTheme.HEADER,
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
  border: `.1em dashed ${SiteTheme.HEADER.color}`, // ems so it can be calculated into the total size easily - border + padding + margin of this style must total the same as padding in styleOverallHeading, or it will resize the element when focus changes
  padding: '.259em',
  margin: '.259em',
}

class Filters extends Component {
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
        content: <MapFilterContainer isOpen={this.state.location} />,
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
          <FacetFilterContainer submit={this.props.submit} marginNest={'1em'} />
        ),
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
        key="filtersButtonHide"
        icon={arrowLeft}
        style={{borderRadius: 0}}
        styleIcon={{width: '1em', height: 'initial'}}
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
            ...SiteTheme.HEADER,
            ...{
              justifyContent: 'space-between',
              borderTop: `1px solid ${SiteTheme.HEADER.color}`,
            },
          }}
        />
        {expandableFilters}
      </div>
    )
  }
}

export default Filters
