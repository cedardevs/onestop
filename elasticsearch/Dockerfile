FROM elasticsearch:2.4.5

RUN /usr/share/elasticsearch/bin/plugin install --batch license
RUN /usr/share/elasticsearch/bin/plugin install --batch shield
RUN /usr/share/elasticsearch/bin/plugin install --batch delete-by-query
ENV http_proxy tiny_proxy:9200
ENV https_proxy tiny_proxy:9200