# PSI: The Persistent Streaming Inventory

[![CircleCI](https://circleci.com/gh/cedardevs/psi.svg?style=svg)](https://circleci.com/gh/cedardevs/psi)
[![codecov](https://codecov.io/gh/cedardevs/psi/branch/master/graph/badge.svg?token=mpaqa2QKdv)](https://codecov.io/gh/cedardevs/psi)

The Persistent Streaming Inventory is a distributed, scalable, event stream and database for the metadata associated
with environmental data granules. It is designed to receive metadata both from automated systems and manual uploads
of e.g. ISO-19115 XML metadata. It implements generic parsing and analysis of that metadata while also enabling arbitrary
processing flows on it. All metadata entities are retrievable via REST API and are also exposed as streaming events to
support downstream client applications for search, analysis, etc.

## Quickstart w/ Kubernetes + Helm

```bash
helm install ./helm/psi
```

For more details, see the [Kubernetes + Helm](#kubernetes-+-helm).



## Legal

This software was developed by Team Foam-Cat,
under the MSN project: 1555839,
NOAA award number NA17OAR4320101 to CIRES.
This code is licensed under GPL version 2.
© 2018 The Regents of the University of Colorado.

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
