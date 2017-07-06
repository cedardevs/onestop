FROM elasticsearch:2.4.1

RUN /usr/share/elasticsearch/bin/plugin install --batch delete-by-query

# Ports 9200 & 9300 are already exposed, and the /usr/share/elasticsearch/data mount point already declared
# Other directories -- config, logs, config/scripts -- need to be declared here if desired