import React from 'react';
import clock from 'fa/clock-o.svg';
import globe from 'fa/globe.svg';
import search from 'fa/search.svg';
import A from '../../common/link/Link';
import styles from './Help.css';

export default class Help extends React.Component {
	render() {
		const accessibleVersion = window.location.hash.includes('508');

		return (
			<section className={styles.help}>
				<h1>How to use this interface:</h1>
				<p>
					<b>
						Type a term into the `Search` box and hit the Search Button
					</b>
					<span className={styles.icon} aria-hidden>
						<img src={search} />
					</span>
				</p>
				<p>
					<b>
						Here are a few tips to narrow your results further:
					</b>
				</p>

				<ul className={styles.tips}>
					<li>
						Use the{' '}
						<span className={styles.icon} aria-hidden>
							<img src={clock} />
						</span>{' '}
						time and{' '}
						<span className={styles.icon} aria-hidden>
							<img src={globe} />
						</span>{' '}
						filters (to the right of the input box) to limit results to only
						those which <u>intersect</u> the given constraints. If a filter has
						been applied, the button will change from <span className={styles.blue}>blue</span> to{' '}
						<span className={styles.purple}>purple</span>.
					</li>
					<li>
						Wrap a search phrase in double quotes for an exact match:
						<ul className={styles.examples}>
							<li>
								<i>"sea surface temperature"</i>
							</li>
						</ul>
						<p>
							<span className={styles.note}>Note:</span> capitalization is
							ignored.
						</p>
					</li>
					<li>
						Use <i>+</i> to indicate that a search term <i>must</i> appear in
						the results and <i>-</i> to indicate that it <i>must not</i>. Terms
						without a <i>+</i> or <i>-</i> are considered optional.
						<ul className={styles.examples}>
							<li>
								<i>temperature pressure +air -sea</i>
							</li>
						</ul>
						<p>
							<span className={styles.note}>Note:</span> this causes <i>-</i>{' '}
							characters within terms to be ignored; use double quotes to search
							for a term with a hyphen in it.
						</p>
					</li>
					<li>
						Using <i>AND</i>, <i>OR</i>, and <i>AND NOT</i> provides similar
						logic to <i>+</i> and <i>-</i>, but they introduce operator
						precedence whichi makes for a more complicated query structure. The
						following example gives the same results as the previous one:
						<ul className={styles.examples}>
							<li>
								<i>
									((temperature AND air) OR (pressure AND air) OR air) AND NOT
									sea
								</i>
							</li>
						</ul>
					</li>
					<li>
						The title, description, and keywords of a data set's metadata can be
						searched directly by appending the field name and a colon to the
						beginning of your search term (remember &mdash; no spaces before or
						after the colon and wrap multi-word terms in parentheses). Exact
						matches can be requested here as well:
						<ul className={styles.examples}>
							<li>
								<i>description:lakes</i>
							</li>
							<li>
								<i>title:"Tsunami Inundation"</i>
							</li>
							<li>
								<i>keywords:(ice deformation)</i>
							</li>
						</ul>
					</li>
				</ul>
				<p>
					If you'd prefer to interact directly with the OneStop API, you can
					find more information about it{' '}
					<A
						target="_blank"
						href="https://github.com/cedardevs/onestop/wiki/OneStop-Search-API"
						style={{ color: '#4286F4' }}
					>
						here
					</A>.
				</p>
			</section>
		);
	}
}
