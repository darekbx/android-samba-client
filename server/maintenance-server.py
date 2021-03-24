import os
import json
import shutil
from operator import itemgetter
from pathlib import Path
from urllib.parse import urlparse, parse_qs
from http.server import BaseHTTPRequestHandler, HTTPServer

class Maintenence:

    SHARE_PATH = ".share_path"

    def createStatistics(self):
        return {
            "usedSpace": self._usedSpace(),
            "freeSpace": self._freeSpace(),
            "lastBackupTimestamp": self._lastBackupTimestamp(),
            "biggestFiles": self._biggestFiles(),
            "typeStatistics": self._typeStatistics()
        }

    def _usedSpace(self):
        root_directory = Path(self._read_share_path())
        return sum(f.stat().st_size for f in root_directory.glob('**/*') if f.is_file())

    def _freeSpace(self):
        usage = shutil.disk_usage(self._read_share_path())
        return usage.free

    def _lastBackupTimestamp(self):
        return None

    def _biggestFiles(self):
        root_directory = Path(self._read_share_path())
        all_files = {}
        for f in root_directory.glob('**/*'):
            if f.is_file():
                all_files[str(f)] = f.stat().st_size
        sorted_by_size = sorted(all_files.items(), key=itemgetter(1), reverse=True)
        biggset_files = []

        for item in sorted_by_size[:10]:
            biggset_files.append({ 
                "name": os.path.basename(item[0]),
                "path": os.path.dirname(item[0]),
                "size": item[1]
            })

        return biggset_files
    
    def _typeStatistics(self):
        return []

    def _read_share_path(self):
        with open(self.SHARE_PATH, "r") as handle:
            return handle.read().strip()

class MaintenanceServer(BaseHTTPRequestHandler):

    TOKEN_PATH = ".md5_auth_token"
    STATISTICS_PATH = "/statistics"

    _maintenence = Maintenence()

    def do_GET(self):
        url = urlparse(self.path)

        if url.path != self.STATISTICS_PATH:
            self._end_with_404()
            return

        md5Authorization = self.headers['Md5Authorization']
        validToken = self._read_authentication_token()

        if md5Authorization != validToken:
            self._end_with_401()
            return

        statistics = self._maintenence.createStatistics()
        response = json.dumps(statistics)

        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        self.wfile.write(response.encode())

    def _end_with_404(self):
        self.send_response(404)
        self.end_headers()

    def _end_with_401(self):
        self.send_response(401)
        self.end_headers()

    def _read_authentication_token(self):
        with open(self.TOKEN_PATH, "r") as handle:
            return handle.read().strip()

def run(server_class=HTTPServer, handler_class=MaintenanceServer, port = ""):
    server_address = ('', int(port))
    httpd = server_class(server_address, handler_class)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()

if __name__ == '__main__':
    DEFAULT_PORT = '8099'
    run(port = os.environ['PORT'] if 'PORT' in os.environ else DEFAULT_PORT)
