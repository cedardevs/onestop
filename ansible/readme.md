Ansible playbook for deploying onestop
======================================

This subproject defines a playbook which deploys all of onestop.

### Prerequisites

1. Ansible installed locally
1. An [ansible inventory file](http://docs.ansible.com/ansible/intro_inventory.html) named `hosts` in this subproject directory
    - The inventory should define a group of machines called `web` and one called `backend`
    - You can put the same host(s) in both groups
    - See `sameple_hosts` for an example
1. Root access via ssh to the machines in your inventory w/ ssh keys copied to them

### Usage

`../gradlew deploy`

This will:

1. Build the api jar
1. Build the client
1. Set up the `backend` machines:
    1. Update java
    1. Install elasticsearch via rpm
    1. Create elasticsearch user and group
    1. Start the elasticsearch service
1. Set up the `web` machines:
    1. Update java
    1. Create onestop user and group
    1. Install the api app as an init.d service
    1. Install nginx
    1. Configure nginx to proxy requests to the api app
    1. Copy the client static content into the nginx document root
    
##### WARNING:

1. The `backend` machines should be able to see each other's port 9300
in order for elasticsearch nodes to form a cluster
1. The `web` machines should be able to see port 9300 on at least one of
the backend machines so the api can talk to elasticsearch 