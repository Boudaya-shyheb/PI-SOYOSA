import urllib.request
import urllib.error

url = 'http://localhost:8082/api/training'
req = urllib.request.Request(url, headers={'Authorization': 'Bearer mock'})

try:
    with urllib.request.urlopen(req) as response:
        print(f"Status: {response.status}")
        print(response.read().decode())
except urllib.error.HTTPError as e:
    print(f"HTTP Error: {e.code}")
    print(e.read().decode())
except urllib.error.URLError as e:
    print(f"URL Error: {e.reason}")
