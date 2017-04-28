##--
##-- almo (c) 2016, 2017
##-- Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0
##--

import json
import urllib
import base64
import httplib2
import sys, codecs, locale
from pprint import pprint

from googleapiclient import discovery
from apiclient.discovery import build

from oauth2client.file import Storage
from oauth2client.client import OAuth2WebServerFlow
from oauth2client.tools import run_flow
from oauth2client.client import flow_from_clientsecrets

storage = Storage('/home/almo/dev/keys/ex1/oAuth_credentials.dat')    
credentials = storage.get()
    
if credentials is None or credentials.invalid:
    PREDICTION_API='https://www.googleapis.com/auth/prediction'
    flow = flow_from_clientsecrets('/home/almo/dev/keys/ex1/oAuth_key.json',scope=[PREDICTION_API], redirect_uri='urn:ietf:wg:oauth:2.0:oob')
    credentials = run_flow(flow, storage)

http = credentials.authorize(httplib2.Http())
service = build('prediction','v1.6', http=http)

fd=open("prediction.out","w")

result = service.trainedmodels().list(project='321866784198').execute()
pprint(unicode(result).decode('ascii'),fd)

body = {'input': {'csvInstance': [ "black and white", "0.9267871", "person", "0.8998944","photography", "0.8296365"]}}
result = service.trainedmodels().predict(body=body, id="language-identifier",project='321866784198').execute()
pprint(unicode(result).decode('ascii'),fd)