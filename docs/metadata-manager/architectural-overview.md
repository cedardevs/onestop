<div align="center"><a href="/onestop/metadata-manager">Metadata Manager Navigation Guide Home</a></div>
<div align="center"><a href="/onestop/metadata-manager/metadata-formats">Next: Supported Metadata Formats</a></div>
<hr>

**Estimated Reading Time:**

# Architectural Overview For Metadata Managers
As someone responsible for stewarding a set of metadata in the OneStop ecosystem, you will be interacting with multiple points in the workflow depending on your goals. This guide is designed to give you a brief overview of components you'll be getting familiar with, and point you in the right direction for more in-depth learning. In the event that you're interested in a highly technical overview of the full OneStop project, you can explore the [technical documentation](../architectural-overview.md) on our site.

## Metadata CRUD: The Registry API
Your metadata might arrive into OneStop in a variety of ways, but once it gets added to the system the Registry API is the front door for basic CRUD operations (Create, Read, Update, Delete). However, depending on the external tool, if any, used to connect your data with OneStop (see [External NOAA Tools Overview]()), you may only ever need to familiarize yourself with read operations. That being said, the Registry is a straightforward RESTful API application, so operations are performed with HTTP requests -- `GET` (read), `POST` (create), `PUT` (update), `PATCH` (also for updating), and `DELETE`. The functionality of the Registry is covered in depth at a later point in the [Registry API guide](v3/onestop-metadata-loading.md).

## Metadata Storage: Kafka
The core of the metadata storage is [Apache Kafka](https://kafka.apache.org/intro). Kafka is 

## Metadata Analysis: Kibana
Once metadata is stored, some parsing and analysis steps automatically run on your input. The generated content is available from some Registry API endpoints but we have also created some [Kibana]() dashboards to visualize metadata quality for multiple records at a time. These views are ideal for inspecting an entire collection's granules all at once, and allowing you to narrow down on filtered subsets of interest. From here you can quickly determine if an issue exists or if everything is looking fantastic. Check out the [Kibana Dashboards]() guide for more information.

## Metadata Searching: Search UI, API, and CLI
Finally, after all your hard work stewarding your data and generating high-quality metadata to describe it all, you and others can search for and discover it in the Search UI, API, and CLI. Each of these is covered in depth over in the [Public User Navigation Guide](../public-user.md). The UI is meant to help a variety of people explore and discover data across an entire organization. The API and CLI, however, are more likely to be used by power users: those building a custom site for a subset of data; those downloading large quantities of data; those building new, higher level products from existing data, etc. While a very small amount of information can result in your metadata making it all the way to these discovery tools, there is definitely a lot of potential to enhance your metadata's discoverability. The last documents in this guide go over the ways you can take a pulse on your input and figure out how to unlock the full potential of each search feature. Check out [Ingest To Discoverability: Best Practices] to get started on making your metadata stand out.

<hr>
<div align="center"><a href="/onestop/metadata-manager">Previous</a> | <a href="#">Top of Page</a> | <a href="/onestop/metadata-manager/metadata-formats">Next</a></div>