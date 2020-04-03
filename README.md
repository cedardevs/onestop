# OneStop Stack

[![Build Status](https://circleci.com/gh/cedardevs/onestop.svg?style=svg)](https://circleci.com/gh/cedardevs/onestop)
[![Code Coverage](https://codecov.io/gh/cedardevs/onestop/branch/master/graph/badge.svg)](https://codecov.io/gh/cedardevs/onestop)

OneStop is a distributed, scalable, event-driven database and search engine for environmental data.
It is designed to receive metadata both from automated systems and manual uploads of ISO-19115 XML metadata.
It implements generic parsing and analysis of that metadata while also enabling arbitrary processing flows
on it. All metadata entities are retrievable via REST API, exposed as streaming events via Kafka, and
indexed to support a wide range of search and discovery capabilities via Elasticsearch.

It is being developed on a grant by a team of researchers from the University of Colorado (more legal info below).

## Documentation

For Overview, Usage, Deployment, and Development information about this project, check out the [docs](/docs).

## Legal

This software was developed under the OneStop project: 1553647 and MSN project: 1555839,
NOAA award numbers NA12OAR4320137 and NA17OAR4320101 respectively to the Cooperative Institute
for Research in Environmental Sciences at the University of Colorado.
This code is licensed under GPL version 2.
© 2020 The Regents of the University of Colorado.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation version 2
of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
