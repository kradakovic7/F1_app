import os

class Config:
    SQLALCHEMY_DATABASE_URI = 'mysql+mysqlconnector://root:@192.168.1.103/f1_fantasy_db'
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    JWT_SECRET_KEY = 'super-secret-key-change-this'