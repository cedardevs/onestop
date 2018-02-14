# Persistent Streaming Information: PSI

[![CircleCI](https://circleci.com/gh/cedardevs/psi.svg?style=svg)](https://circleci.com/gh/cedardevs/psi)

The purpose of this project is to build a system which can both store and run processing workflows on the metadata
related to every file ingested and archived by NOAA's National Centers for Environmental Information (NCEI). 

## Development

### Requirements

1. Java
1. Docker
1. Docker Compose

### Quickstart

```bash
git clone https://github.com/cedardevs/psi
cd psi
./gradlew composeStart
```

This will test and compile each component, build docker images for them, then use docker-compose to spin up
docker containers with zookeeper, kafka, and each component.

## Legal

This software was developed by Team Foam-Cat,
under the OneStop project: 1553647,
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
