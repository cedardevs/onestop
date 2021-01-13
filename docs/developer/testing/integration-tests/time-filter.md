<div align="center"><a href="/onestop/developer/testing/integration-tests/">Integration Tests Documentation Home</a></div>
<hr>

**Estimated Reading Time: 10 minutes**

# Search API Time Filter Integration Tests

## Table of Contents
* [Test cases](#test-cases)
* [Structure of query and results chosen](#structure-of-query-and-results-chosen)

## Test cases:

The ids of the test cases are indicative of what they intend to test.

- 1-20: test edge cases of set logic for different types of date ranges combined with different query relations
- p1, p2, etc: test paleo dates.

For simplicity, datetime records do not also have beginYear and endYear set, so that those test cases don't pollute year filter tests. In real records, if it has a beginDate, the beginYear will match (e.g. 2000-01-01 will also have a beginYear of 2000)

## Structure of query and results chosen:

Given:
A number line with A,B,C,D,E,F,G,H,I (in order). (Specifically these are: 1980, 1990, 1995, 1998, 2000, 2005, 2012, 2013, 2017)
Nulls will be represented in the below charts as "-".

Let:
Q1 = (C, G) (a bounded query)
Q2 = (C, -) (a query with no end date)
Q3 = (-, G) (a query with no beginning date)
These will be our three queries.

Let Results:

(where x and y are shorthand for the begin and end dates)

id | x | y
 1 | D | F
 2 | B | H
 3 | C | G
 4 | A | B
 5 | A | C
 6 | A | E
 7 | H | I
 8 | G | I
 9 | E | I
10 | - | A
11 | - | C
12 | - | E
13 | - | G
14 | - | I
15 | A | -
16 | C | -
17 | E | -
18 | G | -
19 | I | -
20 | - | -

These points are chosen to represent the various ways the query range and result range might overlap. For the ranges, points are chosen that both cross the query begin/end dates, and also some that match it exactly.

then:

Q1:

contains: 2,3,13,14,15,16
within: 1,3
intersects: 1,2,3,5,6,8,9,11,12,13,14,15,16,17,18
disjoint: 4,7,10,19

Q2:

contains: 15,16
within: 1,3,7,8,9,16,17,18,19
intersects: 1,2,3,5,6,7,8,9,11,12,13,14,15,16,17,18,19
disjoint: 4, 10

Q3:

contains: 13,14
within: 1,3,4,5,6,10,11,12,13
intersects: 1,2,3,4,5,6,8,9,10,11,12,13,14,15,16,17,18
disjoint: 7, 19

Note that no query returns 20, since that is a missing date, not a date range unbounded in both directions

<hr>
<div align="center"><a href="#">Top of Page</a></div>
