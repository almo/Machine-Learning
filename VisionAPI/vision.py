##--
##-- almo (c) 2016
##-- Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0
##--

import argparse
import json
import urllib
import base64

from googleapiclient import discovery

def analyze_img(image_uri):
    api_key = json.load(open('/home/almo/Dev/keys/ex1/api_key.json'))['api_key']

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
    
    response = service_request.execute()
    return response

def get_plus_profile(plus_id):
    api_key = json.load(open('/home/almo/Dev/keys/ex1/api_key.json'))['api_key']
    
    service = discovery.build('plus','v1',developerKey=api_key)
    service_request = service.people().get(userId = plus_id)
    response = service_request.execute()
    return response
    
    return 

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('plus_ids', nargs='+', help='Google+ profiles you\'d like to analyze')
    args = parser.parse_args()

    for plus_id in args.plus_ids:  
        plus_profile = get_plus_profile(plus_id)
        
        image_uri = plus_profile['image']['url'].replace("?sz=50","?sz=250")
    
        image_data = analyze_img(image_uri)

        print(image_uri)
        for label in image_data['responses'][0]['labelAnnotations']:
            print(label['description'],label['score']) 
