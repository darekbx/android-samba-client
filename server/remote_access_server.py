import os
import json
import logging
import shutil
from urllib.parse import urlparse, parse_qs
from http.server import BaseHTTPRequestHandler, HTTPServer

from remote_access import RemoteAccess

logging.basicConfig(
    format = "%(levelname) -10s %(asctime)s %(message)s",
    level = logging.DEBUG
)
'''
TODO:
 - Heroku, check how was made in GeoTracker

Endpoints:
 - [POST] /file_upload Upload single file
 - [POST] /dir_create Create directory

'''
class RemoteAccessServer(BaseHTTPRequestHandler):

    TOKEN_PATH = "{}.md5_auth_token".format(os.environ["SERVER_PATH"])
    IP_WHITELIST_PATH = "{}.ip_whitelist".format(os.environ["SERVER_PATH"])

    ENDPOINTS = {
        'authenticate': '/authenticate',
        'list': '/list',
        'file_details': '/file_details',
        'file_delete': '/file_delete',
        'file_download': '/file_download'
    }

    _remote_access = RemoteAccess()
    _was_authorized = True

    def do_GET(self):
        logging.info("GET request,\nPath: %s\nHeaders:\n%s\n", str(self.path), str(self.headers))
    
        md5_authorization = self.headers['Md5Authorization']
        ip_authorization = self.headers['Md5IPAuthorization']
        valid_token = self._read_authentication_token()
        valid_ip = self._read_whitelisted_ip()

        if md5_authorization is None or md5_authorization != valid_token:
            self._end_with_code(401)
            return

        if ip_authorization is None or ip_authorization != valid_ip:
            self._end_with_code(401)
            return
        
        path = urlparse(self.path).path
        if path == self.ENDPOINTS['list']:
            self._handle_list()
        elif path == self.ENDPOINTS['file_details']:
            self._handle_file_details()
        elif path == self.ENDPOINTS['file_delete']:
            self._handle_file_delete()
        elif path == self.ENDPOINTS['file_download']:
            self._handle_file_download()
        else:
            self._end_with_code(404)

    def do_POST(self):
        content_length = int(self.headers['Content-Length'])
        post_data = self.rfile.read(content_length)
        logging.info("POST request,\nPath: %s\nHeaders:\n%s\nBody:\n%s\n",
                str(self.path), str(self.headers), post_data.decode('utf-8'))
        
        md5_authorization = self.headers['Md5Authorization']
        ip_authorization = self.headers['Md5IPAuthorization']
        valid_token = self._read_authentication_token()
        valid_ip = self._read_whitelisted_ip()

        if md5_authorization is None or md5_authorization != valid_token:
            self._end_with_code(401)
            return

        if ip_authorization is None or ip_authorization != valid_ip:
            self._end_with_code(401)
            return
        
        if self.path == self.ENDPOINTS['authenticate']:
            self._handle_authenticate(post_data)
        else:
            self._end_with_code(404)
    
    def _handle_list(self):
        dir_to_list = self._get_path_from_query()
        contents = self._remote_access.list_dir(dir_to_list)
        response = json.dumps(contents)
        self._end_with_json(200, response)

    def _handle_file_details(self):
        file_path = self._get_path_from_query()
        contents = self._remote_access.file_info(file_path)
        response = json.dumps(contents)
        self._end_with_json(200, response)

    def _handle_file_delete(self):
        file_path = self._get_path_from_query()
        contents = self._remote_access.file_delete(file_path)
        response = json.dumps(contents)
        self._end_with_json(200, response)

    def _handle_file_download(self):
        file_path = self._get_path_from_query()
        file_handle = self._remote_access.file_download(file_path)
        file_name = os.path.basename(file_path)
        fs = os.fstat(file_handle.fileno())

        self.send_response(200)
        self.send_header("Content-Type", 'application/octet-stream')
        self.send_header("Content-Disposition", 'attachment; filename="{}"'.format(file_name))
        self.send_header("Content-Length", str(fs.st_size))
        self.end_headers()
        shutil.copyfileobj(file_handle, self.wfile)

        file_handle.close()

    def _handle_authenticate(self, post_data):
        self._was_authorized = True 
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        response = json.dumps({ "authorized": "True" })
        self.wfile.write(response.encode())

    def _end_with_code(self, code):
        self.send_response(code)
        self.end_headers()

    def _end_with_json(self, code, json):
        self.send_response(code)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        self.wfile.write(json.encode())

    def _get_path_from_query(self):
        file_path = ""
        query = parse_qs(urlparse(self.path).query)
        if "path" in query:
            file_path = query["path"][0]
        return file_path
    
    def _read_authentication_token(self):
        with open(self.TOKEN_PATH, "r") as handle:
            return handle.read().strip()

    def _read_whitelisted_ip(self):
        with open(self.IP_WHITELIST_PATH, "r") as handle:
            return handle.read().strip()

def run(server_class=HTTPServer, handler_class=RemoteAccessServer, port = ""):
    server_address = ('', int(port))
    httpd = server_class(server_address, handler_class)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()

if __name__ == '__main__':
    DEFAULT_PORT = '8098'
    run(port = os.environ['PORT'] if 'PORT' in os.environ else DEFAULT_PORT)
