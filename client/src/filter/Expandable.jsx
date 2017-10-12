import React, { Component } from 'react';
import ExpandableSection from './ExpandableSection';

const styleExpandable = {
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
				<ExpandableSection
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

		return <div style={styleExpandable}>{expandableSections}</div>;
	}
}
