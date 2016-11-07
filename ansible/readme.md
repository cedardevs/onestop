Ansible playbook for deploying onestop
======================================

This subproject defines a playbook which deploys all of onestop.

### Prerequisites
1. Install pip
    - Download https://bootstrap.pypa.io/get-pip.py
    - python get-pip.py --user
    - Add .local/bin to path
2. Install ansible
    - pip install ansible --user
3. Set-up an ansible vault
    - obtain the vault passphrase from a sysadmin.
    - edit ~/.vault_pass.txt and add the passphrase on a single line to this file.
    - chmod 600 .vault_pass.txt
    - add "ANSIBLE_VAULT_PASSWORD_FILE=~/.vault_pass.txt" to your environment.      
4. An [ansible inventory file](http://docs.ansible.com/ansible/intro_inventory.html) named `hosts` in this subproject directory
    - The inventory should define a group of machines called `web` and one called `backend`
    - You can put the same host(s) in both groups
    - See `sample_hosts` for an example
5. Root access via ssh to the machines in your inventory w/ ssh keys copied to them

### Usage

`../gradlew deploy`

This will:

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
    
##### NOTE:

The api and client are the most recent versions from the [OJO Snapshot Repo](https://oss.jfrog.org/webapp/#/artifacts/browse/tree/General/oss-snapshot-local/cires/ncei/onestop/)
    
##### WARNING:

1. The `backend` machines should be able to see each other's port 9300
in order for elasticsearch nodes to form a cluster
1. The `web` machines should be able to see port 9300 on at least one of
the backend machines so the api can talk to elasticsearch 
