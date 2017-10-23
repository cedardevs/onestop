import React, { Component } from 'react';
import Checkbox from '../common/input/Checkbox';

const styleContainer = {
	display: 'flex',
};

const styleCheckbox = {
	display: 'flex',
	alignItems: 'center',
	marginRight: '0.616em',
};

const styleTerm = {
	width: '100%',
	color: '#FFF'
};

export default class Facet extends Component {
	labelForTerm = term => {
		const termHierarchy = term.split('>').map(t => {
			return t.trim();
		});
		return termHierarchy[termHierarchy.length - 1];
	};

	render() {
		return (
			<div style={{ ...styleContainer, ...this.props.style }}>
				<div style={styleCheckbox}>
					<Checkbox
						checked={this.props.selected}
						value={{ term: this.props.term, category: this.props.category }}
						onChange={this.props.onChange}
					/>
				</div>
				<div style={styleTerm}>
					{this.labelForTerm(this.props.term)} ({this.props.count})
				</div>
			</div>
		);
	}
}
