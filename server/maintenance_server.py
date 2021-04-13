import os
import json
from urllib.parse import urlparse, parse_qs
from http.server import BaseHTTPRequestHandler, HTTPServer

from maintenence import Maintenence

"""
Samba Maintenence server, endpoints:
 - /statistics generate statistics for whole samba share
 - /statistics?path=/dir/subdir generate statistics for selected path
"""
class MaintenanceServer(BaseHTTPRequestHandler):

    TOKEN_PATH = "{}.md5_auth_token".format(os.environ["SHARE_PATH"])
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
        
        statistics_dir = ""
        query = parse_qs(url.query)
        if "path" in query:
            statistics_dir = query["path"][0]

        statistics = self._maintenence.createStatistics(statistics_dir)
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
