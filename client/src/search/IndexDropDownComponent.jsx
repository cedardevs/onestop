import React from 'react';
import { connect } from 'react-redux'
import styles from './search.css'

const IndexDropDown = ({indexName, onChange}) => {
  const facets = [
    {value: '', label: 'Full Text Search'},
    {value: 'title', label: 'Title'},
    {value: 'people', label: 'People'},
    {value: 'fileIdentifier', label: 'File Identifier'},
    {value: 'enddate', label: 'End Date'},
    {value: 'startdate', label: 'Start Date'}
  ];

  let i = 0;
  const dropDownEntries = facets.map((facet) => {
    return <li className={styles['pure-menu-item']} key={i++}>
    <a href="#" className={styles['pure-menu-link']} label={facet.label} >{facet.label}</a>
    </li>
  });

  const handleChange = (e, i, v) => onChange(v);

  return <div className={`${styles['pure-menu']} ${styles['pure-menu-horizontal']}`}>
      <ul className={styles['pure-menu-list']} id='searchField'>
        <li className={`${styles['pure-menu-item']} ${styles['pure-menu-has-children']} ${styles['pure-menu-allow-hover']}`}>
            <a href='#' id='menuLink1' className={styles['pure-menu-link']}>{indexName}</a>
            <ul className={styles['pure-menu-children']}>
              {dropDownEntries}
            </ul>
        </li>
      </ul>
  </div>
};

export default IndexDropDown
