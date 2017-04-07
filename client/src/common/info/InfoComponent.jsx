import React from 'react'
import ReactDOM from 'react-dom'
import ReactCSSTransitionGroup from 'react-addons-css-transition-group'
import styles from './info.css'

class InfoComponent extends React.Component {
  constructor(props) {
    super(props)
    this.showAbout = props.showAbout
    this.showHelp = props.showHelp
  }

  componentWillUpdate(nextProps) {
    this.showAbout = nextProps.showAbout
    this.showHelp = nextProps.showHelp
  }

  render() {
    const { modalMode } = this.props
    const showCaseStyle = `pure-g ${styles.showcase} ${modalMode ? styles.modalMode : ''}`
    const overlay = (this.showAbout || this.showHelp) && modalMode ? styles.overlay : ''
    return (
      <div id='overlay' className={overlay}>
        <div className={showCaseStyle}>
          <ReactCSSTransitionGroup
            transitionName={ {
              enter: styles['infoPanel-enter'],
              enterActive: styles['infoPanel-enter-active'],
              leave: styles['infoPanel-leave'],
              leaveActive: styles['infoPanel-leave-active']
            } } transitionEnterTimeout={500} transitionLeaveTimeout={500}>
            {this.renderInfo()}
          </ReactCSSTransitionGroup>
        </div>
      </div>
    )
  }

  componentDidMount() {
    document.addEventListener('click', this.handleClickOutside.bind(this), true)
  }

  componentWillUnmount() {
    document.removeEventListener('click', this.handleClickOutside.bind(this), true)
  }

  handleClickOutside(event) {
    const domNode = ReactDOM.findDOMNode(this)
    const { title, id } = event.path[0]
    if ((!domNode || !domNode.contains(event.target)
     && title !== 'Help' && title !== 'About')
     || id === 'overlay') {
      if (this.showAbout) this.props.toggleAbout()
      if (this.showHelp) this.props.toggleHelp()
    }
  }

  renderInfo() {
    if (this.showAbout) { return this.renderAbout() }
    if (this.showHelp) { return this.renderHelp() }
  }

  renderAbout() {
    return <div>
      <div className={`pure-u-1 ${styles.infoText}`}>
        <h1>What Is OneStop?</h1>
        The OneStop Project is designed to improve NOAA's data discovery and access framework. Focusing on all layers of the framework
        and not just the user interface, OneStop is addressing data format and metadata best practices, ensuring more data are available
        through modern web services, working to improve the relevance of dataset searches, and advancing both collection-level metadata
        management and granule level metadata systems to accommodate the wide variety and vast scale of NOAA's data.
      </div>
    </div>
  }

  renderHelp() {
    const accessibleVersion = window.location.hash.includes('508')

    const mainSiteSnippet = <li className={styles.helpItem}><i className="fa-li fa fa-chevron-right" aria-hidden="true"></i>
            Use the <i className={`fa fa-clock-o ${styles.iconButton}`} aria-hidden="true"></i> time and
            <i className={`fa fa-globe ${styles.iconButton}`} aria-hidden="true"></i> space filters
            (to the right of the input box) to limit results to only those that <u>intersect</u> the given constraints.
            <br/>
            If a filter has been applied, the button will change from
            <i className={styles.highlightB} aria-hidden="true"> blue </i> to
            <i className={styles.highlightP} aria-hidden="true"> purple</i>.
          </li>

    const accessibleSiteSnippet = <li className={styles.helpItem}><i className="fa-li fa fa-chevron-right" aria-hidden="true"></i>Use the start/end date and bounding box text boxes to limit results to only those that <u>intersect</u> the given constraints.</li>

    return <div className={`pure-u-1 ${styles.infoText}`}>
        <h1>How to use this interface:</h1>
        <p>
          <b>To get started, just type a term into the Search {accessibleVersion ? 'Text ' : ''}box below and hit the Search Button</b>
          {accessibleVersion ? '' :
          <i className={`${styles.iconButton} fa fa-search`} aria-hidden="true"></i>}
        </p>
        <p>
          <b>Here are a few querying tips to help narrow your results down further:</b>
        </p>
        <ul className="fa-ul">
          {accessibleVersion ? accessibleSiteSnippet : mainSiteSnippet}
          <li className={styles.helpItem}><i className="fa-li fa fa-chevron-right" aria-hidden="true"></i>
            Wrap a search phrase in double quotes for an exact match:
            <ul className={`${styles.examples}`}>
              <li>"sea surface temperature"</li>
            </ul>
            Note that capitalization is ignored.
          </li>
          <li className={styles.helpItem}><i className="fa-li fa fa-chevron-right" aria-hidden="true"></i>
            Use <em>+</em> to indicate that a search term <em>must</em> appear in the results and <em>-</em> to
            indicate that it <em>must not</em>. Terms without a <em>+</em> or <em>-</em> are considered optional.
            <ul className={`${styles.examples}`}>
              <li>temperature pressure +air -sea</li>
            </ul>
            Note that this causes <em>-</em> characters within terms to be ignored;
            use double quotes to search for a term with a hyphen in it.
          </li>
          <li className={styles.helpItem}><i className="fa-li fa fa-chevron-right" aria-hidden="true"></i>
            Using <em>AND</em>, <em>OR</em>, and <em>AND NOT</em> provides similar logic to <em>+</em> and <em>-</em>,
            but they introduce operator precedence which makes for a more complicated query structure.
            The following example gives the same results as the previous one:
            <ul className={`${styles.examples}`}>
              <li>((temperature AND air) OR (pressure AND air) OR air) AND NOT sea</li>
            </ul>
          </li>
          <li className={styles.helpItem}><i className="fa-li fa fa-chevron-right" aria-hidden="true"></i>
            Not sure if you misspelled something? Not to worry, simply place the fuzzy operator after the word
            you're unsure on:
            <ul className={`${styles.examples}`}>
              <li>ghrst~</li>
            </ul>
          </li>
          <li className={styles.helpItem}><i className="fa-li fa fa-chevron-right" aria-hidden="true"></i>
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
      </div>
  }
}

export default InfoComponent
