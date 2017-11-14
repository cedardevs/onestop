import React from 'react'
import clock from 'fa/clock-o.svg'
import globe from 'fa/globe.svg'
import search from 'fa/search.svg'
import chevronRight from 'fa/chevron-right.svg'
import A from '../../common/link/Link'
import styles from './info.css'

export default class Help extends React.Component {

  render() {
    const accessibleVersion = window.location.hash.includes('508')

    const mainSiteSnippet = <li className={styles.helpItem}><img src={chevronRight}></img>
      Use the <i className={styles.iconButton} aria-hidden="true"><img src={clock}></img></i> time and
      <i className={styles.iconButton} aria-hidden="true"><img src={globe}></img></i> space filters
      (to the right of the input box) to limit results to only those that <u>intersect</u> the given constraints.
      <br/>
      If a filter has been applied, the button will change from
      <i className={styles.highlightB} aria-hidden="true"> blue </i> to
      <i className={styles.highlightP} aria-hidden="true"> purple</i>.
    </li>

    const accessibleSiteSnippet = <li className={styles.helpItem}><img src={chevronRight}></img>Use the start/end date
      and bounding box text boxes to limit results to only those that <u>intersect</u> the given constraints.</li>

    return <div className={`pure-u-1 ${styles.infoText}`} ref={help => this.help = help}>
      <button onClick={this.props.toggleHelp} title='Close Help' type='button'
              className={`${styles.modalButton} pure-button`}
              id='modal_close' aria-label='close'>X
      </button>
      <h1>How to use this interface:</h1>
      <p>
        <b>To get started, just type a term into the Search {accessibleVersion ? 'Text ' : ''}box on the home page and
          hit the Search Button</b>
        {accessibleVersion ? '' :
          <i className={styles.iconButton} aria-hidden="true"><img src={search}></img></i>}
      </p>
      <p>
        <b>Here are a few querying tips to help narrow your results down further:</b>
      </p>
      <ul className="fa-ul">
        {accessibleVersion ? accessibleSiteSnippet : mainSiteSnippet}
        <li className={styles.helpItem}><img src={chevronRight}></img>
          Wrap a search phrase in double quotes for an exact match:
          <ul className={`${styles.examples}`}>
            <li>"sea surface temperature"</li>
          </ul>
          Note that capitalization is ignored.
        </li>
        <li className={styles.helpItem}><img src={chevronRight}></img>
          Use <em>+</em> to indicate that a search term <em>must</em> appear in the results and <em>-</em> to
          indicate that it <em>must not</em>. Terms without a <em>+</em> or <em>-</em> are considered optional.
          <ul className={`${styles.examples}`}>
            <li>temperature pressure +air -sea</li>
          </ul>
          Note that this causes <em>-</em> characters within terms to be ignored;
          use double quotes to search for a term with a hyphen in it.
        </li>
        <li className={styles.helpItem}><img src={chevronRight}></img>
          Using <em>AND</em>, <em>OR</em>, and <em>AND NOT</em> provides similar logic to <em>+</em> and <em>-</em>,
          but they introduce operator precedence which makes for a more complicated query structure.
          The following example gives the same results as the previous one:
          <ul className={`${styles.examples}`}>
            <li>((temperature AND air) OR (pressure AND air) OR air) AND NOT sea</li>
          </ul>
        </li>
        <li className={styles.helpItem}><img src={chevronRight}></img>
          Not sure if you misspelled something? Not to worry, simply place the fuzzy operator after the word
          you're unsure on:
          <ul className={`${styles.examples}`}>
            <li>ghrst~</li>
          </ul>
        </li>
        <li className={styles.helpItem}><img src={chevronRight}></img>
          The title, description, and keywords of a data set's metadata can be searched directly by appending the
          field name and a colon to the beginning of your search term (remember -- no spaces before or after the
          colon and wrap multi-word terms in parentheses). Exact matches can be requested here as well:
          <ul className={`${styles.examples}`}>
            <li>description:lakes</li>
            <li>title:"Tsunami Inundation"</li>
            <li>keywords:(ice deformation)</li>
          </ul>
        </li>
      </ul>
      <b>If you'd prefer to interact directly with the OneStop API, you can find more information about it <A
        target="_blank" href='https://github.com/cedardevs/onestop/wiki/OneStop-Search-API'
        style={{color: '#277cb2'}}>here</A></b>
    </div>
  }
}