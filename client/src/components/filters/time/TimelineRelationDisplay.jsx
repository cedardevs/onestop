import React, {useState} from 'react'

import _ from 'lodash'

import defaultStyles from '../../../style/defaultStyles'
import FlexColumn from '../../common/ui/FlexColumn'
import FlexRow from '../../common/ui/FlexRow'
import {consolidateStyles} from '../../../utils/styleUtils'

const QUERY = [ {start: 2, end: 6}, {start: 2}, {end: 6} ]

const RESULTS = [
  {
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

const Spacer = ({style, children}) => {
  const SPACER_HEIGHT = '0.309em'
  const styleSpacer = {height: SPACER_HEIGHT}

  return <div style={consolidateStyles(styleSpacer, style)}>{children}</div>
}

const TimeLineQuery = ({query, labels, outputs}) => {
  const timelineStartLabel = (
    <div
      key="inf"
      style={{paddingRight: '0.309em', borderRight: '2px solid blue'}}
      aria-label="negative infinity, start scale of timeline"
    >
      -∞
    </div>
  )
  const timelineEndLabel = (
    <div
      key="present"
      style={{
        paddingLeft: '0.309em',
        borderLeft: '2px solid blue',
        position: 'relative',
      }}
      aria-label="present, end scale of timeline"
    >
      &nbsp;<div
        style={{
          position: 'absolute',
          transform: 'rotate(90deg)',
          transformOrigin: 'left bottom',
          top: 0,
          left: 0,
        }}
      >
        present
      </div>
    </div>
  )
  const timeline = (
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

  let labelColumn = [ timelineStartLabel, <Spacer key="spacer" /> ]
  labels.forEach(label => {
    labelColumn.push(label)
  })

  let endLabelColumn = [ timelineEndLabel ]

  const leftColumn = (
    <FlexColumn
      key="left"
      style={{alignItems: 'flex-end'}}
      items={labelColumn}
    />
  )

  const queryBox = (
    <Spacer key="queryrange" style={{width: '100%'}}>
      <output
        style={{
          position: 'absolute',
          left: leftEdgeOfRange(query.start == null ? -1 : query.start), // TODO edge of range position functions build in padding for results, manually pushing that off for query...
          right: rightEdgeOfRange(query.end == null ? 9 : query.end),
          height: '90%',
          bottom: 0,
          borderLeft: queryRangeBorder(query.start),
          borderRight: queryRangeBorder(query.end),
          backgroundColor: '#0000ff1f',
        }}
        title="user defined time filter"
      >
        <label style={{position: 'absolute', right: '0.5em'}}>filter</label>
      </output>&nbsp;
    </Spacer>
  )

  let exampleColumn = [ timeline, queryBox ]
  outputs.forEach(output => {
    exampleColumn.push(output)
  })
  // exampleColumn.push(<Spacer key="spacer1" />)
  // exampleColumn.push(<Spacer key="spacer2" />)

  const middle = (
    <FlexColumn
      key="middle"
      style={{
        flexGrow: 1,
        position: 'relative',
        justifyContent: 'space-evenly',
      }}
      items={exampleColumn}
    />
  )

  const rightColumn = <FlexColumn key="right" items={endLabelColumn} />

  return (
    <div>
      <div>Timeline</div>
      <FlexRow items={[ leftColumn, middle, rightColumn ]} />
    </div>
  )
}

const colorRelation = (isMatched, isBorder) => {
  if (isBorder) return isMatched ? '#7ec390' : '#888888'
  return isMatched ? '#9ffcb7' : '#b9b9b9'
}

const queryRangeBorder = offset => {
  return offset == null ? '1px dashed blue' : '1px solid blue'
}
const leftEdgeOfRange = offset => {
  return `${10 * ((offset == null ? 1 : offset) + 1)}%`
}
const rightEdgeOfRange = offset => {
  return `${10 * (9 - (offset == null ? 8 : offset))}%`
}

const TimeLineResult = ({id, result, relation, queryType}) => {
  let isOngoing = result.end == null
  let includedBasedOnRelationship = result.relation[queryType][relation]
  let description = `${includedBasedOnRelationship
    ? 'Included in search results:'
    : 'NOT included in search results:'} ${result.description[queryType]}`
  let title = `${result.label} ${description}.`

  const continuation = isOngoing ? (
    <div
      style={{
        position: 'absolute',
        height: '100%',
        borderRadius: '0px 1em 1em 0px',
        borderStyle: 'solid',
        borderWidth: '1px',
        borderColor: colorRelation(includedBasedOnRelationship, true),
        backgroundColor: colorRelation(includedBasedOnRelationship),
        borderLeft: '0',
        top: '-1px', // due to border width
        left: '100%',
        paddingRight: '.4em',
      }}
    >
      ...
    </div>
  ) : null // TODO background-image: linear-gradient(to right, green , yellow) instead of bg color?

  return (
    <output
      id={id}
      title={description}
      style={{
        cursor: 'pointer',
        display: 'block',
        marginLeft: leftEdgeOfRange(result.start),
        marginRight: rightEdgeOfRange(result.end),
        borderRadius: isOngoing ? '.2em 0 0 .2em' : '.2em',
        borderStyle: 'solid',
        borderWidth: '1px',
        borderColor: colorRelation(includedBasedOnRelationship, true),
        backgroundColor: colorRelation(includedBasedOnRelationship),
        marginBottom: '0.309em',
        position: 'relative',
        overflow: 'visible',
      }}
    >
      &nbsp;{continuation}
      <div style={defaultStyles.hideOffscreen}>{description}</div>
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
        style={{marginBottom: '0.309em', marginRight: '0.309em'}}
      >
        ex {index + 1} :
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
