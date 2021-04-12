import os
import json
import logging
from pathlib import Path
from urllib.parse import urlparse, parse_qs
from http.server import BaseHTTPRequestHandler, HTTPServer

from remote_access import RemoteAccess
from remote_access_db import RemoteAccessDB

logging.basicConfig(
    format = "%(levelname) -10s %(asctime)s %(message)s",
    level = logging.DEBUG
)
'''
TODO:
 - Heroku, check how was made in GeoTracker
 - Add model definitions equals to SMBJ (SambaFile)
 - Security:
   - https
   - Samba App is generating md5 auth tokens

Remote access to the Samba share.
Share directory is defined in .share_path file and is given as a root for server.

Endpoints:
 - For all endpoints:
   - header: Md5Authorization: {md5(user_password)}
   - header: Md5IPAuthorization: {md5(ip)}
 - [POST] /authenticate Just need to be invoked
 - [GET] /list?path={path to dir, eg dir/subdir} List files in dir, root is /
 - [GET] /file_details?path={path to file} Get details about file
 - [GET] /dir_details?path={path to dir} Get details about file
 - [GET] /delete?path={path to file} Deletes file by given path
 - [GET] /download?path={path to file} Download file by given path
 - [POST] /upload Upload single file
 - [POST] /create Create directory

'''
class RemoteAccessServer(BaseHTTPRequestHandler):

    SHARE_PATH = ".share_path"
    TOKEN_PATH = ".md5_auth_token"
    IP_WHITELIST_PATH = ".ip_whitelist"

    ENDPOINTS = {
        'authenticate': '/authenticate',
        'list': '/list'
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
        contents = []
        dir_to_list = ""
        query = parse_qs(urlparse(self.path).query)
        if "path" in query:
            dir_to_list = query["path"][0]
        
        dir_to_list = Path(os.path.join(self._read_share_path(), dir_to_list))
        for item in os.listdir(dir_to_list):
            item_full_path = os.path.join(dir_to_list, item)
            if os.path.isfile(item_full_path):
                # File
                contents.append({
                    "name": os.path.basename(item),
                    "creationTime": round(os.path.getctime(item_full_path) * 1000),
                    "changeTime": round(os.path.getmtime(item_full_path) * 1000),
                    "size": os.path.getsize(item_full_path),
                    "attributes": 0
                })
            else:
                # Dir
                contents.append({
                    "name": os.path.basename(item),
                    "creationTime": round(os.path.getctime(item_full_path) * 1000),
                    "changeTime": round(os.path.getmtime(item_full_path) * 1000),
                    "size": None,
                    "attributes": 16
                })

        response = json.dumps(contents)
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        self.wfile.write(response.encode())

    def _handle_authenticate(self, post_data):
        self._was_authorized = True 
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()

    def _save_push_token(self, token):
        db = RemoteAccessDB()
        db.connect()
        db.save_token(token)
        db.close()
        
    def _end_with_code(self, code):
        self.send_response(code)
        self.end_headers()
    
    def _read_authentication_token(self):
        with open(self.TOKEN_PATH, "r") as handle:
            return handle.read().strip()

    def _read_whitelisted_ip(self):
        with open(self.IP_WHITELIST_PATH, "r") as handle:
            return handle.read().strip()

    def _read_share_path(self):
        with open(self.SHARE_PATH, "r") as handle:
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
