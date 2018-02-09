import sys
import json
from pprint import pprint

data = json.load(sys.argv[1])

pprint(data)

print "This is the name of the script: ", sys.argv[0]
print "Number of arguments: ", len(sys.argv)
print "The arguments are: " , str(sys.argv)


