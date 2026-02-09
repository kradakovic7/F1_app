from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from flask_cors import CORS
from .config import Config

db = SQLAlchemy()

def create_app():
    app = Flask(__name__)
    app.config.from_object(Config)

    # Nastavitve za bazo
    app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://root:@127.0.0.1/f1_fantasy'
    app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False 

    db.init_app(app)
    
    CORS(app) 

    with app.app_context():
        from .auth import auth
        from .routes import api
        
        app.register_blueprint(auth, url_prefix='/auth')
        app.register_blueprint(api, url_prefix='/api')

    return app