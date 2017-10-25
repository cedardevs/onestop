import React, { Component } from 'react';

import AppliedFilters from './AppliedFilters';
import CollectionGrid from './collections/CollectionGrid';

const styleResult = {

}

export default class Result extends Component {

	unselectFacetAndSubmitSearch = (category, term) => {
        this.props.toggleFacet(category, term, false);
		this.props.submit();
	}

	render() {
	    console.log("Result:this.props:", this.props)
		return (
			<div style={styleResult}>
				<AppliedFilters
					location={this.props.location}
					selectedFacets={this.props.selectedFacets}
					onUnselectFacet={this.unselectFacetAndSubmitSearch}
				/>
				<CollectionGrid />
			</div>
		);
	}
}
