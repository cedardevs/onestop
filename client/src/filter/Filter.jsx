import React, { Component } from 'react';

import FilterMenu from './Expandable'
import SpatialFilterContainer from './SpatialFilterContainer';
import TimeFilter from './TimeFilter';
import FacetFilterContainer from './FacetFilterContainer';


class Filter extends Component {
  constructor(props) {
    super(props)
    this.submit = props.submit
  }


  render() {
    const filterSections = [
      {
        heading: 'Space',
        content: <SpatialFilterContainer submit={this.submit} />
      },
      {
        heading: 'Time',
        content: <TimeFilter />
      },
      {
        heading: 'Keywords',
        content: <FacetFilterContainer submit={this.submit} />
      },
    ];

    return (
      <div className="App">
          <FilterMenu sections={filterSections} />
      </div>
    );
  }
}

export default Filter;
