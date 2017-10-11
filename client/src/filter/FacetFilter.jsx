import React, { Component } from 'react';
import PropTypes from 'prop-types';
import FilterMenu from './Expandable';
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
				sections.push({
					count: content.count,
                    term: content.term ? content.term : null,
					heading: heading,
					content: null
				})
			} else {
				// High-Level Facet Section
                isSubsection = false;
                let headingHighLevel = null;
                switch (heading) {
                    case 'science':
                        headingHighLevel = "Data Theme";
                        break;
                    case 'instruments':
                        headingHighLevel = "Instruments";
                        break;
                    case 'platforms':
                        headingHighLevel = "Platforms";
                        break;
                    case 'projects':
                        headingHighLevel = "Projects";
                        break;
                    case 'dataCenters':
                        headingHighLevel = "Data Centers";
                        break;
                    default:
                        headingHighLevel = heading;
                }
				sections.push({
					count: null,
					term: content.term ? content.term : null,
					heading: headingHighLevel,
					content: <FacetFilter facets={content} />
				})
			}
		});

		return <FilterMenu sections={sections} isSubsection={isSubsection} />
	}
}
