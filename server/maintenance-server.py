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
        used_space = self._usedSpace()
        return {
            "usedSpace": used_space,
            "totalSpace": self._totalSpace(used_space),
            "lastBackupTimestamp": self._lastBackupTimestamp(),
            "biggestFiles": self._biggestFiles(),
            "typeStatistics": self._typeStatistics()
        }

    def _usedSpace(self):
        root_directory = Path(self._read_share_path())
        return sum(f.stat().st_size for f in root_directory.glob('**/*') if f.is_file())

    # Total space for data
    def _totalSpace(self, used_space):
        usage = shutil.disk_usage(self._read_share_path())
        return usage.total - (usage.used - used_space)

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
        image_types = ["png", "bmp", "jpeg", "jpg", "gif", "svg"]
        movie_types = ["mpeg", "mpg", "mov", "qt", "mp4", "wmv", "mpv", "avi", "m4p", "m4v", "mp2", "mpe"]
        doc_types = ["doc", "docx", "txt", "rtf", "xlx", "xlxs", "ppt", "ppts", "pdf"]
        archive_types = ["zip", "gz", "tar", "rar", "7z"]
        
        images = { "count": 0, "overallSize": 0 }
        movies = { "count": 0, "overallSize": 0 }
        docs = { "count": 0, "overallSize": 0 }
        archives = { "count": 0, "overallSize": 0 }
        others = { "count": 0, "overallSize": 0 }

        root_directory = Path(self._read_share_path())
        for f in root_directory.glob('**/*'):
            if f.is_file():
                file_size = f.stat().st_size
                extension = os.path.splitext(str(f))[1][1:].strip().lower()
                obj = {}
                if extension in image_types:
                    obj = images
                elif extension in movie_types:
                    obj = movies
                elif extension in doc_types:
                    obj = docs
                elif extension in archive_types:
                    obj = archives
                else:
                    obj = others

                obj["count"] = obj["count"] + 1
                obj["overallSize"] = obj["overallSize"] + file_size

        return [
            { "fileType": "image", **images},
            { "fileType": "movie", **movies},
            { "fileType": "doc", **docs },
            { "fileType": "archive", **archives },
            { "fileType": "other", **others }
        ]

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
