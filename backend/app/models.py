from . import db
from werkzeug.security import generate_password_hash, check_password_hash

# --- ASSOCIATION TABLES ---
user_drivers = db.Table('user_drivers',
    db.Column('user_id', db.Integer, db.ForeignKey('users.id'), primary_key=True),
    db.Column('driver_id', db.Integer, db.ForeignKey('drivers.id'), primary_key=True)
)

user_teams = db.Table('user_teams',
    db.Column('user_id', db.Integer, db.ForeignKey('users.id'), primary_key=True),
    db.Column('team_id', db.Integer, db.ForeignKey('teams.id'), primary_key=True)
)

# --- MODELS ---
class User(db.Model):
    __tablename__ = 'users'
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(50), unique=True, nullable=False)
    password_hash = db.Column(db.String(255), nullable=False)
    is_admin = db.Column(db.Boolean, default=False) 

    budget = db.Column(db.Float, default=120.0) 
    fantasy_points = db.Column(db.Float, default=0.0)
    

    fav_driver_id = db.Column(db.Integer, db.ForeignKey('drivers.id'), nullable=True)

    drivers = db.relationship('Driver', secondary=user_drivers, lazy='subquery',
        backref=db.backref('users', lazy=True))
    teams = db.relationship('Team', secondary=user_teams, lazy='subquery',
        backref=db.backref('users', lazy=True))

    def set_password(self, password):
        self.password_hash = generate_password_hash(password)
        
    def check_password(self, password):
        return check_password_hash(self.password_hash, password)

    def to_dict(self):
        return {
            "id": self.id,
            "username": self.username,
            "is_admin": self.is_admin,
            "budget": self.budget,
            "total_points": self.fantasy_points,
            "fav_driver_id": self.fav_driver_id, 
            "drivers": [d.to_dict() for d in self.drivers],
            "constructors": [t.name for t in self.teams],
            "team_value": sum(d.price for d in self.drivers) + sum(t.price for t in self.teams)
        }

class Team(db.Model):
    __tablename__ = 'teams'
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(100), nullable=False)
    price = db.Column(db.Float, default=15.0)
    drivers = db.relationship('Driver', backref='team', lazy=True)

class Driver(db.Model):
    __tablename__ = 'drivers'
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(50), nullable=False)
    surname = db.Column(db.String(50), nullable=False)
    points = db.Column(db.Float, default=0.0)
    price = db.Column(db.Float, default=10.0)
    team_id = db.Column(db.Integer, db.ForeignKey('teams.id'))
    
    def to_dict(self):
        return {
            'id': self.id,
            'name': f"{self.name} {self.surname}",
            'surname': self.surname,
            'points': self.points,
            'price': self.price,
            'team': self.team.name if self.team else "Free Agent"
        }

class RaceResult(db.Model):
    __tablename__ = 'race_results'
    id = db.Column(db.Integer, primary_key=True)
    round_number = db.Column(db.Integer, nullable=False)
    race_name = db.Column(db.String(100))
    position = db.Column(db.Integer)
    points = db.Column(db.Float)
    driver_id = db.Column(db.Integer, db.ForeignKey('drivers.id'), nullable=False)