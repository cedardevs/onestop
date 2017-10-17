import React, { Component } from 'react';
import Expandable from '../common/Expandable';
import Facet from './Facet';

export default class FacetFilter extends Component {
	constructor(props) {
		super(props);
		this.facetMap = props.facetMap;
		this.selectedFacets = props.selectedFacets;
		this.toggleFacet = props.toggleFacet;
		this.submit = props.submit;
		this.updateStoreAndSubmitSearch = this.updateStoreAndSubmitSearch.bind(
			this,
		);
		this.state = { activeFacets: [], openExpandables: {} };
	}

	componentWillUpdate(nextProps) {
		this.facetMap = nextProps.facetMap;
		this.selectedFacets = nextProps.selectedFacets;
	}

	updateStoreAndSubmitSearch(e) {
		if (event.checked) {
			// the term was checked, so we add it to internal state.activeFacets array
			this.setState(prevState => {
				return {
					activeFacets: prevState.activeFacets.concat([
						{ term: e.value.term, category: e.value.category },
					]),
				};
			});
		} else {
			// the term was unchecked, so we remove it from internal state.activeFacets array
			this.setState(prevState => {
				return {
					activeFacets: prevState.activeFacets.filter(
						facet =>
							facet.term !== e.value.term &&
							facet.category !== e.value.category,
					),
				};
			});
		}

		const category = e.value.category;
		const term = e.value.term;
		const selected = e.checked;

		this.toggleFacet(category, term, selected);
		this.submit();
	}

	isSelected(category, facet) {
		return (
			(this.selectedFacets[category] &&
				this.selectedFacets[category].includes(facet)) ||
			false
		);
	}

	handleExpandableToggle = event => {
		this.setState(prevState => {
			let openExpandables = Object.assign({}, prevState.openExpandables);
			if (event.open) {
				openExpandables[event.value] = true;
			} else {
				openExpandables[event.value] = false;
			}

			return {
				...prevState,
				openExpandables: openExpandables,
			};
		});
	};

	createFacetComponent = facet => {
		// handle any nulls that might get into this function
		let facetComponent = null;
		if (!facet) {
			return facetComponent;
		}

		// parent facets (has expandable sub-facets)
		if ('children' in facet && !_.isEmpty(facet.children)) {
			const expandableKey = `${facet.category}-${facet.term}`;
			const facetKey = `facet-${facet.category}-${facet.term}`;

			facetComponent = (
				<Expandable
					open={!!this.state.openExpandables[expandableKey]}
					key={expandableKey}
					value={expandableKey}
					heading={
						<Facet
							key={facetKey}
							term={facet.term}
							category={facet.category}
							count={facet.count}
							onChange={this.updateStoreAndSubmitSearch}
						/>
					}
					styleHeading={{
						padding: '0.618em',
						backgroundColor: this.props.backgroundColor
							? this.props.backgroundColor
							: 'initial',
					}}
					content={this.createFacetComponent(facet.children)}
					styleContent={{
						marginLeft: this.props.marginNest ? this.props.marginNest : '1em',
					}}
					showArrow={true}
					onToggle={this.handleExpandableToggle}
				/>
			);
		} else if ('children' in facet && _.isEmpty(facet.children)) {
			// leaf facet (contains no sub-layer facets)
			const leafFacetKey = `facet-${facet.category}-${facet.term}`;

			facetComponent = (
				<Facet
					key={leafFacetKey}
					term={facet.term}
					category={facet.category}
					count={facet.count}
					style={{
						padding: '0.618em',
						backgroundColor: this.props.backgroundColor
							? this.props.backgroundColor
							: 'initial',
					}}
					onChange={this.updateStoreAndSubmitSearch}
				/>
			);
		} else {
			// for each key recurse
			let facetComponents = [];
			Object.keys(facet).forEach(subFacet => {
				facetComponents.push(this.createFacetComponent(facet[subFacet]));
			});
			return facetComponents;
		}
		return facetComponent;
	};

	render() {
		let expandableCategories = [];
		const categories = Object.keys(this.facetMap);

		categories.forEach((category, categoryIndex) => {
			// show hamburger menu for high-level categories
			const highLevelHeading = <span>&#9776;&nbsp;{category}</span>;

			// do recursive magic for nested expandables
			const expandableFacets = this.createFacetComponent(
				this.facetMap[category],
			);

			const expandableKey = `${categoryIndex}-${category}`;

			// high-level categories (e.g. - "Data Themes" | "Platforms" | "Projects" | "Data Centers" | "Data Resolution")
			expandableCategories.push(
				<Expandable
					open={!!this.state.openExpandables[expandableKey]}
					key={expandableKey}
					value={expandableKey}
					heading={highLevelHeading}
					styleHeading={{
						backgroundColor: '#17478F',
						padding: '0.618em',
					}}
					content={expandableFacets}
					styleContent={{
						marginLeft: this.props.marginNest ? this.props.marginNest : '1em',
						marginBottom: '1px'
					}}
					onToggle={this.handleExpandableToggle}
				/>,
			);
		});

		return <div>{expandableCategories}</div>;
	}
}
