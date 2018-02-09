import sys
import json
import re
from pprint import pprint

pattern = re.compile("(oe|ot|ie|it)_([a-zA-Z0-9]+)_([a-zA-Z0-9]+)_s(\d{14})_e(\d{14})_p(\d{14})_(pub|emb)\.nc\.gz")

data = json.loads(sys.argv[1])
relativePath = data["relativePath"]

match = pattern.match(relativePath)

parsedAttributes = {
  "processingEnvironment": match.group(1),
  "dataType": match.group(2),
  "satellite": match.group(3),
  "startDate": match.group(4),
  "endDate": match.group(5) ,
  "processDate": match.group(6),
  "publish": match.group(7)== 'pub'
}

msg = dict(data)
msg.update(parsedAttributes)
print json.dumps(msg)




