##--
##-- almo (c) 2016
##-- Apache 2.0 http://www.apache.org/licenses/LICENSE-2.0
##--

import argparse
import json

from googleapiclient import discovery

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('image_uri', help='The image URI you\'d like to label.')
    args = parser.parse_args()

    api_key = json.load(open('/usr/local/google/home/almo/Dev/keys/ex1/api_key.json'))['api_key']

    service = discovery.build('vision','v1',developerKey=api_key)
    
    service_request = service.images().annotate(body={
        'requests':[
            {
                'image':{'source':{'gcsImageUri':args.image_uri}
                },
                'features':[
                    {'type':'FACE_DETECTION','maxResults':10},
                    {'type':'LABEL_DETECTION','maxResults':10},
                    {'type':'SAFE_SEARCH_DETECTION','maxResults':10}
                    ]}]
        })
    
    response = service_request.execute()

    print('%s' % response)
