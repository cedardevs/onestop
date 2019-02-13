## The PSI Documentation Directory

### What Is PSI?
The Persistent Streaming Inventory is a distributed, scalable, event stream and database for the metadata associated
with environmental data granules. It is designed to receive metadata both from automated systems and manual uploads
of ISO-19115 XML metadata. It implements generic parsing and analysis of that metadata while also enabling arbitrary
processing flows on it. All metadata entities are retrievable via REST API and are also exposed as streaming events to
support downstream client applications for search, analysis, etc.

### Navigating The Docs
The documentation for PSI is divided into four main categories:
* [Deployment](/docs/deployment)
* [Usage](/docs/usage)
* [Design](/docs/design)
* [Development](/docs/development)

Information in **Deployment** covers [infrastructure requirements](/docs/deployment/infrastructure-components.md), the [project artifacts](/docs/deployment/project-artifacts.md), and how to [build and deploy](/docs/deployment/system-build-and-deploy.md) the system.

Once PSI is running for you, peruse the **Usage** subdirectory for details on how to [load metadata](/docs/usage/loading-metadata.md) and [interact](/docs/usage/registry-usage.md) with the system.

Meanwhile, if you're interested in a deep-dive into architectural and infrastructural decisions made in the creation of PSI, check out the **Design** subdirectory.

Finally, if you're a Foam-Cat team member or otherwise interested in [making a contribution](/docs/development/contribution-guidelines.md) to the code, head over to the **Development** subdirectory for tips on setting up your [local dev environment](/docs/development/local-dev-environment.md).
