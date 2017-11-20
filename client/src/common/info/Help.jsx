import React from 'react'
import clock from 'fa/clock-o.svg'
import globe from 'fa/globe.svg'
import search from 'fa/search.svg'
import A from '../../common/link/Link'
import styles from './Help.css'

export default class Help extends React.Component {
  render() {
    const accessibleVersion = window.location.hash.includes('508')

    const mainSiteSnippet = (
      <li>
        Use the time{' '}
        <span className={styles.icon} aria-hidden="true">
          <img src={clock} />
        </span>{' '}
        and space{' '}
        <span className={styles.icon} aria-hidden="true">
          <img src={globe} />
        </span>{' '}
        filters (to the right of the input box) to limit results to only those
        that <u>intersect</u> the given constraints.
        <br />
        If a filter has been applied, the button will change from
        <span className={styles.blue} aria-hidden="true">
          {' '}
          blue{' '}
        </span>{' '}
        to
        <span className={styles.purple} aria-hidden="true">
          {' '}
          purple
        </span>.
      </li>
    )

    const accessibleSiteSnippet = (
      <li>
        Use the Start Date, End Date, and Bounding Box text boxes to limit
        results to only those that <u>intersect</u> the given constraints.
      </li>
    )

    return (
      <div className={styles.wrapper}>
        <section className={styles.help}>
          <h1>How to use this interface:</h1>
          <p>
            <b>
              To get started, just type a term into the Search{' '}
              {accessibleVersion ? 'Text ' : ''}Box on the home page and click
              the Search Button
            </b>
            {accessibleVersion ? (
              ''
            ) : (
              <i className={styles.icon} aria-hidden="true">
                <img src={search} />
              </i>
            )}
          </p>

          <p>
            <b>
              Here are a few querying tips to help narrow your results down
              further:
            </b>
          </p>

          <ul className={styles.tips}>
            {accessibleVersion ? accessibleSiteSnippet : mainSiteSnippet}

            <li>
              Wrap a search phrase in double quotes for an exact match:
              <ul className={styles.examples}>
                <li>"sea surface temperature"</li>
              </ul>
              <p>
                <span className={styles.note}>Note:</span> Capitalization is
                ignored.
              </p>
            </li>

            <li>
              Use <em>+</em> to indicate that a search term <em>must</em> appear
              in the results and <em>-</em> to indicate that it{' '}
              <em>must not</em>. Terms without a <em>+</em> or <em>-</em> are
              considered optional.
              <ul className={styles.examples}>
                <li>temperature pressure +air -sea</li>
              </ul>
              <p>
                <span className={styles.note}>Note:</span> This means hyphens
                within terms will be treated as spaces; use double quotes to
                search for a term with a hyphen in it.
              </p>
            </li>

            <li>
              Use of <em>AND</em>, <em>OR</em>, and <em>AND NOT</em> provides
              similar logic to <em>+</em> and <em>-</em>, but introduces
              operator precedence which makes for a more complicated query
              structure. The following example gives the same results as the
              previous one:
              <ul className={styles.examples}>
                <li>
                  ((temperature AND air) OR (pressure AND air) OR air) AND NOT
                  sea
                </li>
              </ul>
            </li>

            <li>
              Not sure if you misspelled something? Not to worry, simply place a
              tilde after the word you're unsure on:
              <ul className={styles.examples}>
                <li>ghrst~</li>
              </ul>
            </li>

            <li>
              The title, description, and keywords of a data set's metadata can
              be searched directly by appending the field name and a colon to
              the beginning of your search term (remember -- no spaces before or
              after the colon and wrap multi-word terms in parentheses). Exact
              matches can be requested here as well:
              <ul className={styles.examples}>
                <li>description:lakes</li>
                <li>title:"Tsunami Inundation"</li>
                <li>keywords:(ice deformation)</li>
              </ul>
            </li>
          </ul>

          <p>
            <b>
              If you'd prefer to interact directly with the OneStop API, you can
              find more information about it{' '}
              <A
                target="_blank"
                href="https://github.com/cedardevs/onestop/wiki/OneStop-Search-API"
                style={{ color: '#277cb2' }}
              >
                here
              </A>
            </b>
          </p>
        </section>
      </div>
    )
  }
}
