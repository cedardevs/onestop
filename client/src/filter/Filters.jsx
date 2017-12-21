import React, {Component} from 'react'

import Expandable from '../common/Expandable'
import FilterHeading from './FilterHeading'
import TimeFilterContainer from './time/TimeFilterContainer'
import FacetFilterContainer from './facet/FacetFilterContainer'
import MapFilter from './MapFilter'

import mapFilterIcon from '../../img/font-awesome/white/svg/globe.svg'
import timeFilterIcon from '../../img/font-awesome/white/svg/calendar.svg'
import facetFilterIcon from '../../img/font-awesome/white/svg/key.svg'

import defaultStyles from '../common/defaultStyles'

const styleFilters = {
  borderTop: '1px solid white',
}

const styleFilterHeadings = {
  fontWeight: 'bold',
  backgroundColor: '#222C37',
  padding: '0.618em',
  borderBottom: '1px solid white',
}

const styleFilterContents = {
  borderBottom: '1px solid white',
}

const styleFacetFilterContents = {
  marginNest: '1em',
  backgroundColor: '#3E97D1',
}

class Filters extends Component {
  constructor(props) {
    super(props)

    this.filters = [
      // TODO: reintroduce these filters when we officially move them from the top menu search component
      // {
      //  name: "map",
      //   heading: <FilterHeading icon={mapFilterIcon} text="Map Filter" />,
      //   content: <MapFilter />,
      // },
      {
       name: "time",
      	heading: <FilterHeading icon={timeFilterIcon} text="Time Filter" />,
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

  handleFilterToggle = event => {
    this.setState(prevState => ({
      ...prevState,
      openIndex: event.open
        ? this.filters.findIndex((filter, index) => index === event.value)
        : -1,
    }))
  }

  render() {
    const expandableFilters = this.filters.map((filter, index) => {
      return (
        <div key={index} style={styleFilters}>
          <Expandable
            key={index}
            value={index}
            open={
              index === this.state.openIndex || filter.name === 'keywords'
            } /* force keywords open */
            onToggle={this.handleFilterToggle}
            heading={filter.heading}
            styleHeading={styleFilterHeadings}
            content={filter.content}
            styleContent={styleFilterContents}
          />
        </div>
      )
    })

    return <div>{expandableFilters}</div>
  }
}

export default Filters
