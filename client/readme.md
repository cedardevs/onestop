Experiments in Creating a OneStop Web App
===

## Getting started
Getting your development environment set up should be straightforward:

1. Clone this repo
1. Run `npm install`
    - Installs dependencies into ./node_modules
1. Run `npm run dev`
    - Starts up a webpack dev server hosting the app
    - Starts mocha in watch mode to automatically run the tests as you work
1. Go to http://localhost:8080/onestop/

## Stack
#### Development/Build Environment
  - npm
  - babel
  - webpack
  - es6 
  
#### Testing/Code Quality
  - mocha
  - chai <sup>[1](#chaistyle)</sup>
  - eslint
  
#### Client-Side Libraries/Frameworks
  - redux
  - immutable <sup>[2](#immutable)</sup>
  - react

#### Outstanding Questions
###### <a name="chaistyle">Chai Style</a>
Though we've been using the 'should' style so far out of habit, we still need to decide which of chai's assertion styles we prefer

###### <a name="immutable">Immutable</a>
Some of us are excited about using immutable.js but we haven't decided to dive into it as a team yet.

## Project Structure
The project root contains directories for different resource types, e.g. img/, style/, src/, etc

Within the src/ folder, javascript is organized by feature. Different module types within each feature are indicated by file name.

## Deployment
There are a number of possible future deployment options which we have not fully evaluated or decided on... but for now we're deploying to AWS via a Jenkins job:

#### Jenkins + AWS:
There is a Jenkins jobs called [onestop-client](https://jenkins.ngdc.noaa.gov/job/onestop-client/) which builds the app using `npm build`. The job currently runs on the session slave, here:
    
    ssh session
    sudo /usr/local/bin/become_ingest
    cd /home/ingest/jenkins-slave-ingest/workspace/onestop-client
    
The job then uses the awscli (instaled via pip) to push the resulting static content to an S3 bucket.

The job publishes the client as the `aws-cojenkins-ec` user with the `AWS_ACCESS_KEY_ID` and and `AWS_SECRET_ACCESS_KEY` environment variables configured for the slave.
    
The static content is pushed to an S3 bucket called 'onestop-client' which has public web hosting enabled.

The client is available at: http://onestop-client.s3-website-us-west-2.amazonaws.com/

#### Json schema validation
The requests to api are validated against a json schema.
Here are a few links related to validation and schema.
http://json-schema.org/
https://github.com/fge/sample-json-schemas/blob/master/geojson/geometry.json
http://json.schemastore.org

