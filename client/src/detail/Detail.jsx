import React from 'react';
import PropTypes from 'prop-types';
import ShowMore from 'react-show-more';
// import A from '../common/link/Link'
import styles from './DetailStyles.css';
import Tabs from './Tabs';
import SummaryView from './SummaryView';
import DescriptionView from './DescriptionView';
import GranuleViewContainer from './GranuleTab/GranuleViewContainer';
import AccessView from './AccessView'

class Detail extends React.Component {
	constructor(props) {
		super(props);

		this.close = this.close.bind(this);
		this.handleKeyDown = this.handleKeyDown.bind(this);
		this.showGranules = this.showGranules.bind(this);
	}

	render() {
		if (!this.props.id || !this.props.item) {
			return <div style={{ display: 'none' }} />;
		}
		const item = this.props.item;

		console.log("item:",item)

		let tabData = [
			{
				title: 'Summary',
				content: <SummaryView id={this.props.id} item={this.props.item} />,
			},
			{
				title: 'Description',
				content: <DescriptionView id={this.props.id} item={this.props.item} />,
			},
			{
				title: 'Matching Files',
				content: (
					<GranuleViewContainer id={this.props.id} item={this.props.item} />
				),
				action: this.showGranules,
			},
			{
			    title: 'Access',
                content: (
                    <AccessView id={this.props.id} item={this.props.item} />
                )
            },
		];

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
	}

	showGranules() {
		if (!this.state.granulesLoaded) {
			this.props.showGranules(this.props.id);
			// change the state to indicate granules have been loaded so we don't reload if the user flips back and forth between tabs.
			this.setState(prevState => {
				return {
					...prevState,
					granulesLoaded: true,
				};
			});
		}
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
		if (nextProps.id && nextProps.id != this.props.id) {
			// reset state of granules loaded each time the collection changes
			this.setState(prevState => {
				return {
					...prevState,
					granulesLoaded: false,
				};
			});
		}
	}

	// None of this links stuff is being used in the new version of the collection view.
	// Preserving it for now because it may be needed for one of the tabs that we haven't added yet.

	// getLinks() {
	//   return (this.props && this.props.item && this.props.item.links) || [];
	// }

	// {/*{this.renderLinks('More Info', this.getLinksByType('information'), this.renderLink)}*/}
	// {/*{this.renderLinks('Data Access', this.getLinksByType('download'), this.renderLink)}*/}

	// getLinksByType(type) {
	//   return this.getLinks().filter(link => link.linkFunction === type);
	// }

	// renderLinks(label, links, linkRenderer) {
	//   if (!links || links.length === 0) {
	//     return <div />;
	//   }
	//
	//   return (
	//     <div className={'pure-g'}>
	//       <div className={`pure-u-1-6 ${styles.linkRow}`}>
	//         <span>{label}</span>
	//       </div>
	//       <div className={`pure-u-5-6 ${styles.linkRow}`}>
	//         <ul className={'pure-g'}>{links.map(linkRenderer)}</ul>
	//       </div>
	//     </div>
	//   );
	// }
	//
	// renderLink(link, index) {
	//   return (
	//     <li className={'pure-u'} key={index}>
	//       <A
	//         href={link.linkUrl}
	//         target="_blank"
	//         className={`pure-button pure-button-primary`}
	//       >
	//         {link.linkProtocol || 'Link'}
	//       </A>
	//     </li>
	//   );
	// }
}

Detail.propTypes = {
	id: PropTypes.string,
	item: PropTypes.object,
};

export default Detail;
