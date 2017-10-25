import React, { Component } from 'react';
import AppliedFacet from './AppliedFacet';
import _ from 'lodash';

// .temporal {
//     background-color: maroon;
// }
//
// .spatial {
//     background-color: darkolivegreen;
// }

const styleAppliedFilters = {
    padding: "2em 2em 0 2em"
}

const styleAppliedFacets = {
	display: 'flex',
	flexFlow: 'row wrap'
};

export default class AppliedFilters extends Component {
	render() {
		const { location, selectedFacets, onUnselectFacet } = this.props;

		if (!location.pathname.includes('files')) {
			let appliedFacets = [];

			_.forEach(selectedFacets, (terms, category) => {
				_.forEach(terms, term => {
                    appliedFacets.push(
						<AppliedFacet
							key={term}
							category={category}
							term={term}
							onUnselect={onUnselectFacet}
						/>,
					);
				});
			});

			return (
			    <div style={styleAppliedFilters}>
                    {/*<div style={styleAppliedMapFilter}>Applied Map Filter</div>*/}
                    {/*<div style={styleAppliedTimeFilter}>Applied Time Filter</div>*/}
			        <div style={styleAppliedFacets}>{appliedFacets}</div>
                </div>
            )

		}
		return null;
	}
}
