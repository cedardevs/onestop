import React from 'react'
import search from 'fa/search.svg'
import A from '../common/link/Link'
import {fontFamilySerif, fontFamilyMonospace} from '../../utils/styleUtils'
import Meta from '../helmet/Meta'

const styleWrapper = {
  width: '100%',
  color: '#111',
}

const styleHelp = {
  fontSize: '1.318em',
  padding: '1.618em',
  minHeight: '100vh',
  margin: '0 auto',
  maxWidth: '45em',
}

const styleHelpH1 = {
  fontFamily: fontFamilySerif(),
  fontSize: '1.5em',
  margin: '0 0 0.618em 0',
}

const styleHelpH2 = {
  fontFamily: fontFamilySerif(),
  fontSize: '1.1em',
  margin: '0 0 0.618em 0',
}

const styleIcon = {
  margin: 0,
  padding: '0 0.309em',
}

const styleIconImg = {
  backgroundColor: '#3e7bad',
  borderRadius: '0.309em',

  padding: '0.309em',
  position: 'relative',
  top: '0.15em',
  maxWidth: '1em',
  maxHeight: '1em',

  height: '1em',
  width: '1em',
}

const styleTipsListItem = {
  marginBottom: '1.618em',
}

const styleExamples = {
  fontFamily: fontFamilyMonospace(),
  marginTop: '1em',
}

const styleExamplesListItem = {
  margin: '0 0 0.618em 0',
  padding: 0,
  color: '#555',
}

const styleNote = {
  color: 'mediumseagreen',
  fontWeight: 'bold',
}

const styleHR = {
  border: 0,
  borderBottom: '1px dashed #ccc',
  background: '#999',
  margin: '1.618em 0',
}

export default class Help extends React.Component {
  render() {
    return (
      <div style={styleWrapper}>
        <Meta title="Help for NOAA OneStop" />
        <section style={styleHelp}>
          <h1 style={styleHelpH1}>How to use this interface:</h1>
          <p>
            To get started, type a term into the Search Box on the home page and
            submit or click the Search Button
            <i style={styleIcon} aria-hidden="true">
              <img
                alt="search icon"
                src={search}
                style={styleIconImg}
                width="1.1em"
                height="1.1em"
              />
            </i>
          </p>

          <hr style={styleHR} />

          <h2 style={styleHelpH2}>
            Here are a few querying tips to help narrow your results down
            further:
          </h2>

          <ul>
            <li>
              Use the filters menu (available on the search results page) to
              limit results to only those that <u>intersect</u> the given
              constraints.
              <br />
              Once a filter has been applied, a tag will be placed above the
              search results.
            </li>

            <li style={styleTipsListItem}>
              Wrap a search phrase in double quotes for an exact match:
              <ul style={styleExamples}>
                <li style={styleExamplesListItem}>"sea surface temperature"</li>
              </ul>
              <p>
                <span className={styleNote}>Note:</span> Capitalization is
                ignored.
              </p>
            </li>

            <li>
              Use <em>+</em> to indicate that a search term <em>must</em> appear
              in the results and <em>-</em> to indicate that it{' '}
              <em>must not</em>. Terms without a <em>+</em> or <em>-</em> are
              considered optional.
              <ul style={styleExamples}>
                <li style={styleExamplesListItem}>
                  temperature pressure +air -sea
                </li>
              </ul>
              <p>
                <span className={styleNote}>Note:</span> This means hyphens
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
              <ul style={styleExamples}>
                <li style={styleExamplesListItem}>
                  ((temperature AND air) OR (pressure AND air) OR air) AND NOT
                  sea
                </li>
              </ul>
            </li>

            <li>
              Not sure if you misspelled something? Not to worry, simply place a
              tilde after the word you're unsure on:
              <ul style={styleExamples}>
                <li style={styleExamplesListItem}>ghrst~</li>
              </ul>
            </li>

            <li>
              The title, description, and keywords of a data set's metadata can
              be searched directly by appending the field name and a colon to
              the beginning of your search term (remember -- no spaces before or
              after the colon and wrap multi-word terms in parentheses). Exact
              matches can be requested here as well:
              <ul style={styleExamples}>
                <li style={styleExamplesListItem}>description:lakes</li>
                <li style={styleExamplesListItem}>
                  title:"Tsunami Inundation"
                </li>
                <li style={styleExamplesListItem}>
                  keywords:(ice deformation)
                </li>
              </ul>
            </li>

            <li>
              Metadata can also be searched by using a regex. Regex terms can be
              written using two different methods. The easiest method is to use
              double quotes to capture the regex along with the reserved
              characters literally. Regex terms must be enclosed with forward
              slashes. Regex terms not enclosed with double quotes must have
              every reserved character escaped by a backslash.
              <ul style={styleExamples}>
                <li style={styleExamplesListItem}>"/^GHRSST"</li>
                <li style={styleExamplesListItem}>"/^GHRSST.*(Version 2)$"</li>
              </ul>
              <ul style={styleExamples}>
                <li style={styleExamplesListItem}>\/\^GHRSST</li>
                <li style={styleExamplesListItem}>
                  \/\^GHRSST\.\*(Version 2)\$\/
                </li>
              </ul>
            </li>
          </ul>

          <hr style={styleHR} />

          <h2 style={styleHelpH2}>More Information</h2>
          <p>
            Find more information about interacting directly with the{' '}
            <A
              target="_blank"
              href="https://github.com/cedardevs/onestop/wiki/OneStop-Search-API-Requests"
              style={{color: '#277cb2'}}
            >
              OneStop API
            </A>.
          </p>
        </section>
      </div>
    )
  }
}
