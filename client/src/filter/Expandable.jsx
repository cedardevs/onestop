import React, { Component } from 'react';
import FilterSection from './ExpandableSection';

const styleFilterMenu = {
	color: '#FFFFFF',
	backgroundColor: '#1C577F'
};

export default class Expandable extends Component {

	render() {
		const expandableSections = this.props.sections.map((section, key) => {
			let isLeaf = false;
			if(section.content === null) {
				isLeaf = true;
			}
			return (
				<FilterSection
					key={key}
					isLeaf={isLeaf}
					heading={section.heading}
					content={section.content}
					count={section.count}
					term={section.term}
					isSubsection={this.props.isSubsection}
				/>
			);
		});

		return <div style={styleFilterMenu}>{expandableSections}</div>;
	}
}
