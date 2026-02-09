from flask import Blueprint, request, jsonify
from .models import User, db

auth = Blueprint('auth', __name__)

@auth.route('/register', methods=['POST'])
def register():
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')

    if User.query.filter_by(username=username).first():
        return jsonify({"msg": "Username already exists"}), 400

    new_user = User(username=username)
    new_user.set_password(password) # Metoda iz models.py
    
    db.session.add(new_user)
    db.session.commit()

    return jsonify({"msg": "User created successfully"}), 201

@auth.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')

    user = User.query.filter_by(username=username).first()


    if user and user.check_password(password):
        return jsonify({
            "msg": "Login successful",
            "user": user.to_dict()
        }), 200
    
    return jsonify({"msg": "Invalid credentials"}), 401