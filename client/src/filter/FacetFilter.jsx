import React, { Component } from 'react';
import PropTypes from 'prop-types';
import FilterMenu from './FilterMenu';
import _ from 'lodash';

export default class FacetFilter extends Component {
	// static propTypes = {
	// 	/** facets object returned from MetaFacetsConverter, provided meta.facets from search */
	// 	facets: PropTypes.object,
	// };
	// static defaultProps = {
	// 	facets: {}
	// };

	render() {
		let sections = [];
        let isSubsection = true;

        Object.keys(this.props.facets).forEach( heading => {
			const content = this.props.facets[heading]
			console.log("content", content);
			if(!_.isObject(content)) {
				return;
			}
			if("children" in content && !_.isEmpty(content.children)) {
				// Facet with Children
				sections.push({
					count: content.count,
					term: content.term ? content.term : null,
					heading: heading,
					content: <FacetFilter facets={content.children}/>
				})
            } else if("children" in content && _.isEmpty(content.children)) {
				// Leaf Facet
				sections.push({
					count: content.count,
                    term: content.term ? content.term : null,
					heading: heading,
					content: null
				})
			} else {
				isSubsection = false;
				// High-Level Facet Section
				sections.push({
					count: null,
					term: content.term ? content.term : null,
					heading: heading,
					content: <FacetFilter facets={content} />
				})
			}
		});

		console.log("sections:", sections);

		return <FilterMenu sections={sections} isSubsection={isSubsection} />
	}
}
