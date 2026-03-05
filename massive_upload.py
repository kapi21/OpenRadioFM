import re
import json
import urllib.request
import urllib.error
import os

M3U_FILE = "C:\\@MIS PROYECTOS\\K706_RE\\OpenRadioFM\\radio.m3u8"
EXISTING_FILE = "C:\\@MIS PROYECTOS\\K706_RE\\OpenRadioFM\\existing_stations.json"
API_URL = "https://hciqxvfvohcaiaqqrvdq.supabase.co/rest/v1/stations"
API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhjaXF4dmZ2b2hjYWlhcXFydmRxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI2MjExNDcsImV4cCI6MjA4ODE5NzE0N30.kE5W3_qHMWMc1nKQQQn_lMb9NXOu6kFjEL5glpIhswM"

def parse_m3u(file_path):
    stations = []
    current_station = {}
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if line.startswith("#EXTINF"):
                    name_match = re.search(r'tvg-name="([^"]+)"', line)
                    logo_match = re.search(r'tvg-logo="([^"]+)"', line)
                    
                    name = name_match.group(1) if name_match else None
                    if not name:
                        last_comma = line.rfind(",")
                        name = line[last_comma + 1:].strip() if last_comma != -1 else "Unknown"
                    
                    ps_name = name.replace(" (Radio)", "").strip()
                    current_station = {
                        "ps_name": ps_name,
                        "logo_url": logo_match.group(1) if logo_match else None,
                        "frequency": 0,
                        "pi_code": None
                    }
                elif line.startswith("http") and current_station:
                    current_station["stream_url"] = line
                    stations.append(current_station)
                    current_station = {}
    except Exception as e:
        print(f"Error parsing M3U: {e}")
    return stations

def load_existing():
    if not os.path.exists(EXISTING_FILE):
        return set()
    try:
        with open(EXISTING_FILE, 'r', encoding='utf-16') as f: # PowerShell uses UTF-16 by default for Out-File
            data = json.load(f)
            return {item['ps_name'].upper() for item in data if 'ps_name' in item}
    except Exception as e:
        # Try UTF-8 if UTF-16 fails
        try:
           with open(EXISTING_FILE, 'r', encoding='utf-8') as f:
               data = json.load(f)
               return {item['ps_name'].upper() for item in data if 'ps_name' in item}
        except:
           print(f"Error loading existing stations: {e}")
           return set()

def upload_batch(batch):
    data = json.dumps(batch).encode('utf-8')
    req = urllib.request.Request(API_URL, data=data, method='POST')
    req.add_header('apikey', API_KEY)
    req.add_header('Authorization', f'Bearer {API_KEY}')
    req.add_header('Content-Type', 'application/json')
    
    try:
        with urllib.request.urlopen(req) as response:
            print(f"Batch uploaded: {response.status}")
    except urllib.error.HTTPError as e:
        print(f"HTTP Error {e.code}: {e.read().decode()}")
    except Exception as e:
        print(f"Error: {e}")

def main():
    print("Loading existing stations...")
    existing_names = load_existing()
    print(f"Found {len(existing_names)} existing stations in DB.")

    print("Parsing M3U8...")
    all_stations = parse_m3u(M3U_FILE)
    
    # Filter out duplicates
    new_stations = []
    seen_in_m3u = set()
    for s in all_stations:
        name_upper = s['ps_name'].upper()
        if name_upper not in existing_names and name_upper not in seen_in_m3u:
            new_stations.append(s)
            seen_in_m3u.add(name_upper)

    print(f"Filtered {len(all_stations)} down to {len(new_stations)} new stations.")
    
    if not new_stations:
        print("Nothing new to upload.")
        return

    chunk_size = 50
    for i in range(0, len(new_stations), chunk_size):
        batch = new_stations[i:i + chunk_size]
        print(f"Uploading {i} to {min(i + chunk_size, len(new_stations))}...")
        upload_batch(batch)

if __name__ == "__main__":
    main()
