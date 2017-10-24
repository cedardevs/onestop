import React from 'react';
import PropTypes from 'prop-types';
import ShowMore from 'react-show-more';
import A from 'LinkComponent';
import styles from './DetailStyles.css';
import Tabs from './Tabs';
import SummaryView from './SummaryView';
import DescriptionView from './DescriptionView';
import { processUrl } from '../utils/urlUtils';

class Detail extends React.Component {
	constructor(props) {
		super(props);

		this.close = this.close.bind(this);
		this.handleKeyDown = this.handleKeyDown.bind(this);
	}

	render() {
		if (!this.props.id || !this.props.item) {
			return <div style={{ display: 'none' }} />;
		}
		const item = this.props.item;

		let tabData = [];
		tabData.push({
			title: 'Summary',
			content: <SummaryView id={this.props.id} item={this.props.item} />,
		});
		tabData.push({
			title: 'Description',
			content: <DescriptionView />,
		});

		return (
			<div className={styles.modal}>
				<div className={styles.modalContent}>
					<div className={`pure-g ${styles.header} ${styles.underscored}`}>
						<div
							className={`pure-u-11-12 ${styles.title}`}
							title={`${item.title}`}
						>
							<ShowMore lines={1} anchorClass={`${styles.showMore}`}>
								{item.title}
							</ShowMore>
						</div>
						<div className={'pure-u-1-12'}>
							<span className={styles.close} onClick={this.close}>
								x
							</span>
						</div>
					</div>
					<Tabs data={tabData} activeIndex={0} />
				</div>
			</div>
		);

		// console.log('geometry', this.props.item.spatialBounding)
		// const imgUrl = processUrl(this.props.item.thumbnail)
		// return imgUrl ?
		//   <img className={styles.previewImg} src={imgUrl}/> :
		// {/*{this.renderLinks('More Info', this.getLinksByType('information'), this.renderLink)}*/}
		// {/*{this.renderLinks('Data Access', this.getLinksByType('download'), this.renderLink)}*/}
	}

	close() {
		this.props.dismiss();
	}

	handleKeyDown(event) {
		if (event.keyCode === 27) {
			// esc
			this.close();
		}
	}

	componentWillUpdate(nextProps, nextState) {
		if (nextProps.id) {
			document.addEventListener('keydown', this.handleKeyDown, false);
		} else {
			document.removeEventListener('keydown', this.handleKeyDown, false);
		}
	}

	getLinks() {
		return (this.props && this.props.item && this.props.item.links) || [];
	}

	getLinksByType(type) {
		return this.getLinks().filter(link => link.linkFunction === type);
	}

	renderLinks(label, links, linkRenderer) {
		if (!links || links.length === 0) {
			return <div />;
		}

		return (
			<div className={'pure-g'}>
				<div className={`pure-u-1-6 ${styles.linkRow}`}>
					<span>{label}</span>
				</div>
				<div className={`pure-u-5-6 ${styles.linkRow}`}>
					<ul className={'pure-g'}>{links.map(linkRenderer)}</ul>
				</div>
			</div>
		);
	}

	renderLink(link, index) {
		return (
			<li className={'pure-u'} key={index}>
				<A
					href={link.linkUrl}
					target="_blank"
					className={`pure-button pure-button-primary`}
				>
					{link.linkProtocol || 'Link'}
				</A>
			</li>
		);
	}

	renderGranulesLink() {
		return (
			<a
				onClick={() => this.props.showGranules(this.props.id)}
				className={`pure-button pure-button-primary ${styles.granulesButton}`}
			>
				Show Matching Files
			</a>
		);
	}
}

Detail.propTypes = {
	id: PropTypes.string,
	item: PropTypes.object,
};

export default Detail;
