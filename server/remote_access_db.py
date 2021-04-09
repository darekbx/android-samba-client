import os
import psycopg2
import logging
from psycopg2 import Error

logging.basicConfig(
    format = "%(levelname) -10s %(asctime)s %(message)s",
    level = logging.DEBUG
)

class RemoteAccessDB:

    connection = None
    cursor = None

    def connect(self):
        if 'DATABASE_URL' in os.environ:
            db_url = os.environ['DATABASE_URL']
        else:
            db_url = "" # Take from heroku variables 
        self.connection = psycopg2.connect(db_url)
        self.cursor = self.connection.cursor()
        self._init_scheme()
        
    def _init_scheme(self):
        self.cursor.execute('''
            CREATE TABLE IF NOT EXISTS push_token (
                value text NOT NULL
            )                    
        ''')
        self.connection.commit()

    def fetch_token(self):
        self.cursor.execute("SELECT value FROM push_token LIMIT 1")
        data = self.cursor.fetchall()
        return data[0][0]

    def save_token(self, token):
        # Only one token can exists
        self.cursor.execute("DELETE FROM push_token")
        self.cursor.execute("INSERT INTO push_token (value) VALUES(%s)", [token])
        self.connection.commit()

    def close(self):
        if (self.connection):
            self.cursor.close()
            self.connection.close()
