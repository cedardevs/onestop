<div align="center"><a href="/onestop/metadata-manager">Metadata Manager Navigation Guide Home</a></div>
<div align="center"><a href="/onestop/metadata-manager/metadata-formats">Next: Supported Metadata Formats</a></div>
<hr>

**Estimated Reading Time: 5 minutes**

# Architectural Overview For Metadata Managers
As someone responsible for stewarding a set of metadata in the OneStop ecosystem, you could be interacting with multiple points in the workflow depending on your goals. This guide is designed to give you a brief overview of components you'll be getting familiar with, and point you in the right direction for more in-depth learning. In the event that you're interested in a highly technical overview of the full OneStop project, you can explore the [technical documentation](../architectural-overview.md) on our site.

## OneStop Workflow
The flow of metadata through OneStop can be summarized in 4 conceptual steps: loading, transformation, search indexing, and search access. The following diagrams for each step are color coded. 

Anything grey is external to a OneStop deployment, and represents places where non-required externally developed tools could connect to OneStop. Examples are tools migrating metadata into OneStop, subject matter expert scripts that can be triggered from within the OneStop workflow to enhance received metadata, and custom user interfaces using the Search API.

Non-gray elements are all components of a full OneStop deployment. Green elements -- Kafka and Elasticsearch -- are the 3rd party software components that OneStop is built on top of. Only privileged users (like those that are responsible for managing the servers a deployment runs on) should ever access or configure these components directly. Blue components connect to Kafka and Elasticsearch and facilitate many of the workflow steps, however, these components cannot be interacted with directly. Finally, the purple components are access points into the OneStop system by public users (Search API, UI, and CLI) and trusted users responsible for the metadata contained within OneStop (Registry API).

### Step One: Loading
![Flow diagram for loading metadata](../images/mm/s1-loading.png)

Metadata is loaded into OneStop either manually by Metadata Managers via the Registry API, or automated through external software that also communicates to the Registry API or interfaces directly to Kafka by writing to an input topic.

### Step Two: Transformation
![Flow diagram for internal metadata transformation](../images/mm/s2-transformation.png)

Once metadata lands on the input Kafka topics, automatic processes are triggered to parse it into the Discovery format and analyze individual fields from that format. If a datastream or collection has been setup for it, optional SME functions can be triggered for metadata enhancement prior to parsing and analysis.

### Step Three: Search Indexing
![Flow diagram for indexing records for search](../images/mm/s3-search-indexing.png)

After metadata has been parsed and analyzed, it is put onto a Kafka topic from which the Indexer reads. The Indexer assesses each record for search readiness, and after successful validation pushes the records into their respective search indices on Elasticsearch. Records that do not pass validation will not be indexed until the errors are resolved.

### Step Four: Search Access
![Flow diagram for how indexed records can be accessed](../images/mm/s4-search-access.png)

Metadata that passes validation steps and is indexed is now accessible from the Search API. The Search API services requests from the Search UI, Search CLI, and external users and consumers, translating them into Elasticsearch queries against the appropriate search index.

## 

## Metadata CRUD: The Registry API
Your metadata might arrive into OneStop in a variety of ways, but once it gets added to the system the Registry API is the front door for basic CRUD operations (Create, Read, Update, Delete). However, depending on the external tool, if any, used to connect your data with OneStop (see [External NOAA Tools Overview](v3/setup-datastream-pipeline.md)), you may only ever need to familiarize yourself with read operations. That being said, the Registry is a straightforward RESTful API application, so operations are performed with HTTP requests -- `GET` (read), `POST` (create), `PUT` (update), `PATCH` (also for updating), and `DELETE`. The functionality of the Registry is covered in depth at a later point in the [Registry API guide](v3/onestop-metadata-loading.md).

## Metadata Storage: Kafka
The core of the metadata storage is [Apache Kafka](https://kafka.apache.org/intro). Kafka is an open-source distributed stream-processing software platform that enables data to be transported, stored, and transformed in real-time. Kafka can be scaled horizontally, even across data centers, while simultaneously processing and storing data quickly and compactly. While it is true that OneStop stores metadata in Kafka, the use of it is not limited there. Metadata movement to/from external systems, parsing, analysis, and transformation is all facilitated by Kafka. As a metadata manager, chances are very slim you'll interact directly with Kafka, but knowing it's there gives confidence your metadata is being securely, durably, and efficiently stored.

## Metadata Analysis: Kibana
Once metadata is stored, some parsing and analysis steps automatically run on your input. The generated content is available from some Registry API endpoints but we have also created some [Kibana](https://www.elastic.co/guide/en/kibana/current/introduction.html) dashboards to visualize metadata quality for multiple records at a time. These views are ideal for inspecting an entire collection's granules all at once, and allowing you to narrow down on filtered subsets of interest. From here you can quickly determine if an issue exists or if everything is looking fantastic. Check out the [Kibana Dashboards](kibana-dashboards.md) guide for more information.

## Metadata Searching: Search UI, API, and CLI
Finally, after all your hard work stewarding your data and generating high-quality metadata to describe it all, you and others can search for and discover it in the Search UI, API, and CLI. Each of these is covered in depth over in the [Public User Navigation Guide](../public-user.md). The UI is meant to help a variety of people explore and discover data across an entire organization. The API and CLI, however, are more likely to be used by power users: those building a custom site for a subset of data; those downloading large quantities of data; those building new, higher level products from existing data, etc. While a very small amount of information can result in your metadata making it all the way to these discovery tools, there is definitely a lot of potential to enhance your metadata's discoverability. The last documents in this guide go over the ways you can take a pulse on your input and figure out how to unlock the full potential of each search feature. Check out [Ingest To Discoverability: Best Practices](best-practices.md) to get started on making your metadata stand out.

<hr>
<div align="center"><a href="/onestop/metadata-manager">Previous</a> | <a href="#">Top of Page</a> | <a href="/onestop/metadata-manager/metadata-formats">Next</a></div>