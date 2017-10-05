import React, { Component } from 'react';
import PropTypes from 'prop-types';
import FilterSection from './FilterSection';

const styleFilterMenu = {
	color: '#FFFFFF',
	backgroundColor: '#1C577F'
};

export default class FilterMenu extends Component {
	// static propTypes = {
	// 	/** Array of FilterSection. */
	// 	sections: PropTypes.array,
	// };
	// static defaultProps = {
	// 	sections: [
	// 		{
	// 			heading: 'A',
	// 			content: <p>X</p>
	// 		},
	// 		{
	// 			heading: 'B',
	// 			content: <p>Y</p>
	// 		},
	// 		{
	// 			heading: 'C',
	// 			content: <p>Z</p>
	// 		},
	// 	],
	// };

	render() {
		const filterSections = this.props.sections.map((section, key) => {
			return (
				<FilterSection
					key={key}
					heading={section.heading}
					content={section.content}
					count={section.count}
					term={section.term}
					isSubsection={this.props.isSubsection}
				/>
			);
		});

		return <div style={styleFilterMenu}>{filterSections}</div>;
	}
}
