import React, {useState} from 'react'

import _ from 'lodash'

import defaultStyles from '../../../style/defaultStyles'
import FlexColumn from '../../common/ui/FlexColumn'
import FlexRow from '../../common/ui/FlexRow'

const QUERY = [ {start: 2, end: 6}, {start: 2}, {end: 6} ]

const RESULTS = [
  {
    label: '1',
    start: 0,
    end: 1,
    description: [
      'result range ends before query begins, with no overlap',
      'result range ends before query begins, with no overlap',
      'result range ends before query ends',
    ],
    relation: [
      {contains: false, within: false, intersects: false, disjoint: true},
      {contains: false, within: false, intersects: false, disjoint: true},
      {contains: false, within: true, intersects: true, disjoint: false},
    ],
  },
  {
    label: 'ex 2',
    start: 3,
    end: 5,
    description: [
      'result range is smaller than query, with complete overlap (result is a subset)',
      'result range is smaller than query, with complete overlap (result is a subset)',
      'result range ends before query ends',
    ],
    relation: [
      {contains: false, within: true, intersects: true, disjoint: false},
      {contains: false, within: true, intersects: true, disjoint: false},
      {contains: false, within: true, intersects: true, disjoint: false},
    ],
  },
  {
    label: 'ex 3',
    start: 1,
    end: 7,
    description: [
      'result range is larger than query, with complete overlap (result is a superset)',
      'result range starts before query, with significant overlap',
      'result range ends before query, with significant overlap',
    ],
    relation: [
      {contains: true, within: false, intersects: true, disjoint: false},
      {contains: false, within: false, intersects: true, disjoint: false},
      {contains: false, within: false, intersects: true, disjoint: false},
    ],
  },
  {
    label: 'ex 4',
    start: 4,
    description: [
      'result range starts in middle of query range, and continues into present',
      'result range starts in middle of query range, and continues into present',
      'result range starts in middle of query range, and continues into present',
    ],
    relation: [
      {contains: false, within: false, intersects: true, disjoint: false},
      {contains: false, within: true, intersects: true, disjoint: false},
      {contains: false, within: false, intersects: true, disjoint: false},
    ],
  },
  {
    label: 'ex 5',
    start: 1,
    description: [
      'result range starts before query, and continues into present',
      'result range starts before query, and continues into present',
      'result range starts in middle of query range, and continues into present, past query end',
    ],
    relation: [
      {contains: true, within: false, intersects: true, disjoint: false},
      {contains: true, within: false, intersects: true, disjoint: false},
      {contains: false, within: false, intersects: true, disjoint: false},
    ],
  },
]

const TimeLineQuery = ({query, labels, outputs}) => {
  let startOffset = query.start == null ? 0 : query.start
  let endOffset = query.end == null ? 8 : query.end
  let width = endOffset - startOffset
  let style = {
    position: 'absolute',
    marginLeft: `${startOffset}em`,
    width: `${width}em`,
    height: '100%',
    border: 'solid blue 1px',
    borderRadius:
      query.end == null
        ? '0 1em 1em 0'
        : query.start == null ? '1em 0 0 1em' : '0',
  }
  // return (
  //   <div
  //     style={{
  //       position: 'relative',
  //       paddingTop: '0.309em',
  //       paddingBottom: '0.309em',
  //     }}
  //   >
  //     <div style={style} />
  //     {children}
  //   </div>
  // )

  let labelColumn = new Array()
  labelColumn.push(
    <div
      key="inf"
      style={{paddingRight: '0.309em', borderRight: '2px solid blue'}}
      aria-label="negative infinity, start scale of timeline"
    >
      -∞
    </div>
  )
  labelColumn.push(<br key="spacer" />)
  labels.forEach(label => {
    labelColumn.push(label)
  })

  const leftColumn = (
    <FlexColumn
      key="left"
      style={{alignItems: 'flex-end'}}
      items={labelColumn}
    />
  )

  let outputColumn = new Array()
  outputColumn.push(
    <hr
      key="timeline"
      style={{
        width: '100%',
        // marginTop: 'auto',
        // marginBottom: 'auto',
        height: '0px', // fix for IE
        borderStyle: 'solid',
        borderColor: 'blue',
      }}
      aria-hidden={true}
    />
  )

  outputColumn.push(
    <div key="queryrange" style={{width: '100%', marginBottom: '0.309em'}}>
      <output
        style={{
          position: 'absolute',
          left: `${10 * (QUERY[0].start + 1)}%`,
          right: `${10 * (9 - QUERY[0].end)}%`,
          height: '90%',
          bottom: 0,
          borderRight: '1px solid blue',
          borderLeft: '1px solid blue',
          backgroundColor: '#0000ff1f',
        }}
        title="user defined time filter"
      >
        <label style={{position: 'absolute', right: '0.5em', bottom: '0.5em'}}>
          query
        </label>
      </output>&nbsp;
    </div>
  )
  outputs.forEach(output => {
    outputColumn.push(output)
  })
  outputColumn.push(<br key="spacer1" />)
  outputColumn.push(<br key="spacer2" />)

  const middle = (
    <FlexColumn
      key="middle"
      style={{flexGrow: 1, position: 'relative'}}
      items={outputColumn}
    />
  )

  const rightColumn = (
    <FlexColumn
      key="right"
      items={[
        <div
          key="present"
          style={{paddingLeft: '0.309em', borderLeft: '2px solid blue'}}
          aria-label="present, end scale of timeline"
        >
          present
        </div>,
      ]}
    />
  )

  // TODO H1 is a placeholder here for something title-like. Use the fieldset heading thing??
  return (
    <div>
      <h1>Timeline</h1>
      <FlexRow items={[ leftColumn, middle, rightColumn ]} />
    </div>
  )
}

const colorRelation = hasRelation => {
  return hasRelation ? 'green' : 'red'
}

const TimeLineResult = ({id, result, relation, queryType}) => {
  let startOffset = result.start
  let endOffset = result.end == null ? 8 : result.end
  let width = endOffset - startOffset
  let includedBasedOnRelationship = result.relation[queryType][relation]
  let description = includedBasedOnRelationship
    ? 'Included in search results:'
    : 'NOT included in search results:'
  let title = `${result.label} ${description}.`
  let style = {
    marginLeft: `${startOffset}em`,
    width: `${width}em`,
    borderRadius: result.end ? '0' : '0 1em 1em 0',
    backgroundColor: colorRelation(includedBasedOnRelationship),
    marginTop: '0.309em',
    marginBottom: '0.309em',
  }

  const continuation =
    result.end == null ? (
      <div
        style={{
          position: 'absolute',
          height: '100%',
          borderRadius: '0px 1em 1em 0px',
          backgroundColor: colorRelation(includedBasedOnRelationship),
          top: 0,
          left: '100%',
        }}
      >
        ...&nbsp;&nbsp;
      </div>
    ) : null // TODO background-image: linear-gradient(to right, green , yellow) instead of bg color?

  let desc = `${description} ${result.description[queryType]}` //'no overlap between result and query means result will not be included by this search' // TODO
  let marginLeft = `${10 * (startOffset + 1)}%`
  let marginRight = `${10 * (9 - endOffset)}%`
  return (
    <output
      id={id}
      title={desc}
      style={{
        cursor: 'pointer',
        display: 'block',
        marginLeft: marginLeft,
        marginRight: marginRight,
        backgroundColor: colorRelation(includedBasedOnRelationship),
        marginBottom: '0.309em',
        position: 'relative',
        overflow: 'visible',
      }}
    >
      &nbsp;{continuation}
      <div style={defaultStyles.hideOffscreen}>{desc}</div>
    </output>
  )
}

const TimelineRelationDisplay = ({relation, hasStart, hasEnd}) => {
  let currentQueryType = 0
  if (hasStart && !hasEnd) currentQueryType = 1
  if (!hasStart && hasEnd) currentQueryType = 2

  const labels = _.map(RESULTS, (result, index) => {
    return (
      <label
        key={`result${index + 1}`}
        htmlFor={`result${index + 1}`}
        style={{marginBottom: '0.309em'}}
      >
        ex {index + 1}
      </label>
    )
  })
  const outputs = _.map(RESULTS, (result, index) => {
    return (
      <TimeLineResult
        key={`result${index + 1}`}
        id={`result${index + 1}`}
        result={result}
        relation={relation}
        queryType={currentQueryType}
      />
    )
  })
  return (
    <TimeLineQuery
      query={QUERY[currentQueryType]}
      labels={labels}
      outputs={outputs}
    />
  )
}
export default TimelineRelationDisplay
