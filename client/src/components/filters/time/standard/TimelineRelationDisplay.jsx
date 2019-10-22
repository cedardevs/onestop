import React, {useState} from 'react'

const QUERY = [ {start: 2, end: 6}, {start: 2}, {end: 6} ]

const RESULTS = [
  {
    label: '1',
    start: 0,
    end: 1,
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
    relation: [
      {contains: true, within: false, intersects: true, disjoint: false},
      {contains: false, within: false, intersects: true, disjoint: false},
      {contains: false, within: false, intersects: true, disjoint: false},
    ],
  },
  {
    label: 'ex 4',
    start: 4,
    relation: [
      {contains: false, within: false, intersects: true, disjoint: false},
      {contains: false, within: true, intersects: true, disjoint: false},
      {contains: false, within: false, intersects: true, disjoint: false},
    ],
  },
  {
    label: 'ex 5',
    start: 1,
    relation: [
      {contains: true, within: false, intersects: true, disjoint: false},
      {contains: true, within: false, intersects: true, disjoint: false},
      {contains: false, within: false, intersects: true, disjoint: false},
    ],
  },
]

const TimeLineQuery = ({query, children}) => {
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
  return (
    <div
      style={{
        position: 'relative',
        paddingTop: '0.309em',
        paddingBottom: '0.309em',
      }}
    >
      <div style={style} />
      {children}
    </div>
  )
}

const colorRelation = hasRelation => {
  return hasRelation ? 'green' : 'red'
}

const TimeLineResult = ({result, relation, queryType}) => {
  let startOffset = result.start
  let endOffset = result.end == null ? 8 : result.end
  let width = endOffset - startOffset
  let includedBasedOnRelationship = result.relation[queryType][relation]
  let description = includedBasedOnRelationship
    ? 'will be returned by search'
    : 'will NOT be returned by search'
  let title = `${result.label} ${description}.`
  let style = {
    marginLeft: `${startOffset}em`,
    width: `${width}em`,
    borderRadius: result.end ? '0' : '0 1em 1em 0',
    backgroundColor: colorRelation(includedBasedOnRelationship),
    marginTop: '0.309em',
    marginBottom: '0.309em',
  }
  return (
    <div style={style} title={title}>
      {result.label}
    </div>
  )
}

const TimelineRelationDisplay = ({relation, hasStart, hasEnd}) => {
  let currentQueryType = 0
  if (hasStart && !hasEnd) currentQueryType = 1
  if (!hasStart && hasEnd) currentQueryType = 2
  return (
    <TimeLineQuery query={QUERY[currentQueryType]}>
      <TimeLineResult
        result={RESULTS[0]}
        relation={relation}
        queryType={currentQueryType}
      />
      <TimeLineResult
        result={RESULTS[1]}
        relation={relation}
        queryType={currentQueryType}
      />
      <TimeLineResult
        result={RESULTS[2]}
        relation={relation}
        queryType={currentQueryType}
      />
      <TimeLineResult
        result={RESULTS[3]}
        relation={relation}
        queryType={currentQueryType}
      />
      <TimeLineResult
        result={RESULTS[4]}
        relation={relation}
        queryType={currentQueryType}
      />
    </TimeLineQuery>
  )
}
export default TimelineRelationDisplay
