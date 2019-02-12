# PSI: The Persistent Streaming Inventory

## Documentation Directory

### Navigating The Docs
The documentation for PSI is divided into four main categories:
* [Deployment](/deployment/README.md)
* [Usage](/usage/README.md)
* [Design](/design/README.md)
* [Development](/development/README.md)

Information in **Deployment** covers [infrastructure requirements](/deployment/infrastructure-components.md), the [project artifacts](/deployment/project-artifacts.md), and how to [build and deploy](/deployment/system-build-and-deploy.md) the system.

Once PSI is running for you, peruse the **Usage** subdirectory for details on how to [load metadata](/usage/loading-metadata.md) and [interact](/usage/registry-api.md) with the system.

Meanwhile, if you're interested in a deep-dive into architectural and infrastructure decisions made in the creation of PSI, check out the **Design** subdirectory.

Finally, if you're a Foam-Cat team member or otherwise interested in [making a contribution](/development/contribution-guidelines.md) to the code, head over to the **Development** subdirectory for tips on setting up your [local dev environment](/development/local-dev-environment.md).

### What Is PSI?
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
