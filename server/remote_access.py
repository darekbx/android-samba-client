import os
from pathlib import Path

class RemoteAccess:
    
    SHARE_PATH = "{}.share_path".format(os.environ["SERVER_PATH"])

    def list_dir(self, path):
        contents = []
        dir_to_list = Path(os.path.join(self._read_share_path(), path))
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
        return contents
    
    def file_info(self, path):
        full_path = Path(os.path.join(self._read_share_path(), path))
        return {
            "name": os.path.basename(path),
            "creationTime": round(os.path.getctime(full_path) * 1000),
            "changeTime": round(os.path.getmtime(full_path) * 1000),
            "size": os.path.getsize(full_path),
            "attributes": 0
        }

    def file_download(self, path):
        full_path = Path(os.path.join(self._read_share_path(), path))
        return open(full_path, 'rb')

    def _read_share_path(self):
        with open(self.SHARE_PATH, "r") as handle:
            return handle.read().strip()