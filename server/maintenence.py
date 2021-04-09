import shutil
import os
from operator import itemgetter
from pathlib import Path

"""
Samba share maintenence:
 - `createStatistics` method used to generate statistics for defined share_path or for share subdirectory 

"""
class Maintenence:

    SHARE_PATH = ".share_path"

    def createStatistics(self, sub_dir = ""):
        used_space = self._usedSpace(sub_dir)
        if len(sub_dir) > 0:
            dir_count, file_count = self._dir_counts(sub_dir)
            created_time, modified_time = self._dir_times(sub_dir)
            return {
                "usedSpace": used_space,
                "directoryCount": dir_count,
                "filesCount": file_count,
                "createdTime": created_time,
                "modifiedTime": modified_time
            }
        
        return {
            "usedSpace": used_space,
            "totalSpace": self._totalSpace(used_space),
            "lastBackupTimestamp": self._lastBackupTimestamp(),
            "biggestFiles": self._biggestFiles(),
            "typeStatistics": self._typeStatistics()
        }

    def _usedSpace(self, sub_dir = ""):
        root_directory = Path(os.path.join(self._read_share_path(), sub_dir))
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

    def _dir_counts(self, sub_dir):
        root_directory = Path(os.path.join(self._read_share_path(), sub_dir))
        dir_count = 0
        file_count = 0
        for f in root_directory.glob('**/*'):
            if f.is_dir():
                dir_count = dir_count + 1
            elif f.is_file():
                file_count = file_count + 1
        return dir_count, file_count

    def _dir_times(self, sub_dir):
        root_directory = Path(os.path.join(self._read_share_path(), sub_dir))
        return round(os.stat(root_directory).st_ctime * 1000), round(os.stat(root_directory).st_mtime * 1000)

    def _read_share_path(self):
        with open(self.SHARE_PATH, "r") as handle:
            return handle.read().strip()
