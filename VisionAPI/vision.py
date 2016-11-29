##--
##-- almo (c) 2016
##-- Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0
##--

import json
import urllib
import base64
import httplib2

from time import sleep

from googleapiclient import discovery
from apiclient.discovery import build

from oauth2client.file import Storage
from oauth2client.client import OAuth2WebServerFlow
from oauth2client.tools import run_flow
from oauth2client.client import flow_from_clientsecrets

def analyze_img(image_uri):
    api_key = json.load(open('/home/almo/dev/keys/ex1/api_key.json'))['api_key']

    service = discovery.build('vision','v1',developerKey=api_key)
    
    image= urllib.urlopen(image_uri)
    image_content = base64.b64encode(image.read())
    
    service_request = service.images().annotate(body={
        'requests': [{
            'image': {
                'content': image_content.decode('UTF-8')
                },
            'features': [{
                'type': 'LABEL_DETECTION',
                'maxResults': 3
                }]
            }]
        })
    
    if service_request is not None:
        response = service_request.execute()
    else:
        response = None
    
    return response

def get_plus_profile(id):
    api_key = json.load(open('/home/almo/dev/keys/ex1/api_key.json'))['api_key']
  
    service = discovery.build('plus','v1',developerKey=api_key)
    service_request = service.people().get(userId = id)
    
    if service_request is not None:
        response = service_request.execute()
    else:
        response = None
        
    return response
     
def get_plus_contacts():
    PEOPLE_API='https://www.googleapis.com/auth/contacts.readonly'
    flow = flow_from_clientsecrets('/home/almo/dev/keys/ex1/oAuth_key.json',scope=[PEOPLE_API])
    
    storage = Storage('/home/almo/dev/keys/ex1/oAuth_credentials.dat')    
    credentials = storage.get()
    
    if credentials is None or credentials.invalid:
        credentials = run_flow(flow, storage)
        
    http = httplib2.Http()
    http = credentials.authorize(http)

    service = build(serviceName='people', version='v1', http=http)
    
    request = service.people().connections().list(resourceName='people/me', pageSize=500)
    
    plus_contacts=[]
      
    while request is not None:
        response = request.execute()
        for contact in response['connections']:
            if 'urls' in contact:
                plus_contacts.append(contact['urls'][0]['metadata']['source']['id'])
 
        request = service.people().connections().list_next(request,response)
    
    return plus_contacts

if __name__ == '__main__':
    plus_contacts = get_plus_contacts()
    
    print "Processing %d contacts" % len(plus_contacts)
    
    for plus_id in plus_contacts:
                
        plus_profile = get_plus_profile(plus_id)
        image_uri = plus_profile['image']['url'].replace("?sz=50","?sz=250")
        
        image_data = analyze_img(image_uri)
        
        if image_data is not None:
            print(image_uri)
            if 'labelAnnotations' in image_data['responses'][0]:
                for label in image_data['responses'][0]['labelAnnotations']:
                    print(label['description'],label['score']) 
            else:
                print image_data['responses']
