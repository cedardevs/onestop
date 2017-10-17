import React, { Component } from 'react';

import Expandable from '../common/Expandable';
import FilterHeading from './FilterHeading';
import TimeFilter from './TimeFilter';
import FacetFilterContainer from './FacetFilterContainer';
import MapFilter from './MapFilter';

import mapFilterIcon from '../../img/font-awesome/white/svg/globe.svg';
import timeFilterIcon from '../../img/font-awesome/white/svg/calendar.svg';
import facetFilterIcon from '../../img/font-awesome/white/svg/key.svg';

const styleFilterHeadings = {
	fontWeight: 'bold',
	backgroundColor: '#222C37',
	padding: '0.618em',
	borderBottom: '1px solid white',
};

const styleFilterContents = {
	borderBottom: '1px solid white',
};

class Filters extends Component {
	constructor(props) {
		super(props);

		this.filters = [
			{
				heading: <FilterHeading icon={mapFilterIcon} text="Map Filter" />,
				content: <MapFilter />,
			},
			{
				heading: <FilterHeading icon={timeFilterIcon} text="Time Filter" />,
				content: <TimeFilter />,
			},
			{
				heading: <FilterHeading icon={facetFilterIcon} text="Keywords" />,
				content: <FacetFilterContainer submit={props.submit} />,
			},
		];

		this.state = {
			openIndex: -1,
		};
		this.handleFilterToggle = this.handleFilterToggle.bind(this);
	}

	handleFilterToggle = event => {
		this.setState(prevState => ({
			...prevState,
			openIndex: event.open
				? this.filters.findIndex((filter, index) => index === event.value)
				: -1,
		}));
	};

	render() {
		const expandableFilters = this.filters.map((filter, index) => {
			return (
				<Expandable
					key={index}
					value={index}
					open={index === this.state.openIndex}
					onToggle={this.handleFilterToggle}
					heading={filter.heading}
					styleHeading={styleFilterHeadings}
					content={filter.content}
					styleContent={styleFilterContents}
				/>
			);
		});

		return <div>{expandableFilters}</div>;
	}
}

export default Filters;
