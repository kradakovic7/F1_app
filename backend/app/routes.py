from flask import Blueprint, jsonify, request
from .models import User, Driver, Team, db
import fastf1
import pandas as pd
import numpy as np
import os
import traceback

api = Blueprint('api', __name__)

# --- CACHE SETUP ---
CACHE_DIR = os.path.join(os.getcwd(), 'f1_cache')
if not os.path.exists(CACHE_DIR):
    os.makedirs(CACHE_DIR)
fastf1.Cache.enable_cache(CACHE_DIR)

# --- DRIVERS & TEAMS ---
@api.route('/drivers', methods=['GET'])
def get_drivers():
    drivers_list = Driver.query.order_by(Driver.points.desc()).all()
    return jsonify([d.to_dict() for d in drivers_list]), 200

@api.route('/constructors', methods=['GET'])
def get_constructors():
    teams = Team.query.all()
    data = []
    for t in teams:
        total_points = sum([d.points for d in t.drivers])
        driver_names = [f"{d.name} {d.surname}" for d in t.drivers]
        data.append({
            "id": t.id,
            "name": t.name,
            "points": total_points,
            "price": t.price,
            "drivers": driver_names
        })
    return jsonify(sorted(data, key=lambda x: x['points'], reverse=True)), 200


# --- FANTASY LOGIC ---

@api.route('/fantasy/create-team', methods=['POST'])
def create_fantasy_team():
    data = request.get_json()
    print(f"DEBUG: Prejeti podatki: {data}") 

    user_id = data.get('user_id') 
    if not user_id: return jsonify({"error": "User not logged in"}), 401
    
    user = User.query.get(user_id)
    if not user: return jsonify({"error": "User not found"}), 404

    driver_ids = data.get('driver_ids', [])
    constructor_ids = data.get('constructor_ids', [])

    print(f"DEBUG: Iskanje voznikov ID: {driver_ids}")
    print(f"DEBUG: Iskanje ekip ID: {constructor_ids}")

    if len(driver_ids) != 5: return jsonify({"error": "Select exactly 5 drivers"}), 400
    if len(constructor_ids) != 2: return jsonify({"error": "Select exactly 2 constructors"}), 400

    selected_drivers = Driver.query.filter(Driver.id.in_(driver_ids)).all()
    selected_constructors = Team.query.filter(Team.id.in_(constructor_ids)).all()

    print(f"DEBUG: Najdenih ekip v bazi: {len(selected_constructors)}")

    cost_drivers = sum([d.price for d in selected_drivers])
    cost_teams = sum([t.price for t in selected_constructors])
    total_cost = cost_drivers + cost_teams
    
    BUDGET_CAP = 120.0 
    
    if total_cost > BUDGET_CAP:
        return jsonify({"error": f"Budget exceeded! Cost: {total_cost}M"}), 400

    # 3. SHRANI V BAZO
    try:
        user.drivers = selected_drivers
        user.teams = selected_constructors
        user.budget = BUDGET_CAP - total_cost
        
        calculate_user_points(user)
        
        db.session.commit()
        print("DEBUG: Uspešno shranjeno v bazo!")
        
        return jsonify({
            "message": "Team saved successfully!", 
            "remaining_budget": user.budget,
            "teams_saved_count": len(user.teams) 
        }), 200
        
    except Exception as e:
        print(f"ERROR pri shranjevanju: {e}")
        db.session.rollback()
        return jsonify({"error": str(e)}), 500
    
    
@api.route('/fantasy/delete-team/<int:user_id>', methods=['DELETE'])
def delete_fantasy_team(user_id):
    user = User.query.get(user_id)
    if not user:
        return jsonify({"error": "User not found"}), 404
    
    user.drivers = []
    user.teams = []
    user.budget = 120.0 
    user.fantasy_points = 0.0
    db.session.commit()
    
    return jsonify({"message": "Team deleted successfully", "budget": 120.0}), 200

@api.route('/delete-user/<int:user_id>', methods=['DELETE'])
def delete_user(user_id):
    user = User.query.get(user_id)
    if not user:
        return jsonify({"error": "User not found"}), 404
    
    try:
        user.drivers = []
        user.teams = []
        
        db.session.delete(user)
        db.session.commit()
        
        return jsonify({"message": "User account deleted successfully"}), 200
    except Exception as e:
        db.session.rollback()
        return jsonify({"error": str(e)}), 500


@api.route('/user/favorite-driver', methods=['POST'])
def set_favorite_driver():
    data = request.get_json()
    user = User.query.get(data.get('user_id'))
    if user:
        user.fav_driver_id = data.get('driver_id')
        db.session.commit()
        return jsonify({"message": "Favorite driver updated"}), 200
    return jsonify({"error": "User not found"}), 404


@api.route('/fantasy/my-team/<int:user_id>', methods=['GET'])
def get_my_team(user_id):
    user = User.query.get(user_id)
    if not user: return jsonify({"error": "User not found"}), 404
    
    calculate_user_points(user)

    constructors_data = []
    for team in user.teams:
        team_points = sum([d.points for d in team.drivers])
        constructors_data.append({
            "id": team.id,
            "name": team.name,
            "points": team_points,
            "price": team.price
        })

    return jsonify({
        "id": user.id,
        "username": user.username,
        "budget": user.budget,
        "total_points": user.fantasy_points,  
        "drivers": [d.to_dict() for d in user.drivers],
        "constructors": constructors_data, # Android
        "teams": constructors_data         # Web app 
    }), 200


@api.route('/calendar', methods=['GET'])
def get_calendar():
    try:
        schedule = fastf1.get_event_schedule(2025, include_testing=False)
        races_data = []
        for _, row in schedule.iterrows():
            races_data.append({
                "round": int(row['RoundNumber']), 
                "name": row['EventName'], 
                "location": row['Location'], 
                "date": row['EventDate'].strftime('%Y-%m-%d')
            })
        return jsonify(races_data), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@api.route('/results/<int:round_num>', methods=['GET'])
def get_race_results(round_num):
    try:
        session = fastf1.get_session(2025, round_num, 'R')
        session.load(telemetry=False, weather=False, messages=False)
        results = session.results.iloc[:10]
        data = []
        for i, row in results.iterrows():
            data.append({
                "position": int(row['Position']), 
                "driver": row['Abbreviation'], 
                "team": row['TeamName'], 
                "time": str(row['Time']).split('days')[-1].strip(), 
                "points": int(row['Points'])
            })
        return jsonify(data), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@api.route('/telemetry/compare', methods=['GET'])
def compare_drivers():
    d1_raw = request.args.get('d1', 'VER')
    d2_raw = request.args.get('d2', 'HAM')
    race_name = request.args.get('race', 'Bahrain Grand Prix')
    YEAR = 2025 

    def clean_name(n):
        if not n: return "VER"
        return n.strip().split(' ')[-1][:3].upper()

    d1 = clean_name(d1_raw)
    d2 = clean_name(d2_raw)

    try:
        session = fastf1.get_session(YEAR, race_name, 'R')
        session.load(telemetry=True, weather=False, messages=False)
        
        try: d1_laps = session.laps.pick_driver(d1)
        except: return jsonify({"error": f"Driver {d1} not found"}), 404
            
        try: d2_laps = session.laps.pick_driver(d2)
        except: return jsonify({"error": f"Driver {d2} not found"}), 404

        if len(d1_laps) == 0 or len(d2_laps) == 0:
            return jsonify({"error": "No laps found"}), 404

        fastest_d1 = d1_laps.pick_fastest()
        fastest_d2 = d2_laps.pick_fastest()
        
        if fastest_d1 is None or fastest_d2 is None:
             return jsonify({"error": "No telemetry data"}), 404

        d1_car = fastest_d1.get_car_data().add_distance()
        d2_car = fastest_d2.get_car_data().add_distance()
        
        max_dist = min(d1_car['Distance'].max(), d2_car['Distance'].max())
        common_dist = np.arange(0, max_dist, 10) 
        
        d1_speed = np.interp(common_dist, d1_car['Distance'], d1_car['Speed'])
        d2_speed = np.interp(common_dist, d2_car['Distance'], d2_car['Speed'])
        
        telemetry_data = []
        for i in range(len(common_dist)):
            telemetry_data.append({
                "dist": int(common_dist[i]), 
                "d1_speed": int(d1_speed[i]), 
                "d2_speed": int(d2_speed[i])
            })
            
        return jsonify({
            "drivers": [d1, d2], 
            "track": race_name, 
            "telemetry": telemetry_data
        }), 200

    except Exception as e:
        print("Backend Error:", e)
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500

# --- LEADERBOARD & ADMIN ---

def calculate_user_points(user):
    driver_points = sum(d.points for d in user.drivers)
    
    constructor_points = 0
    for team in user.teams:
        constructor_points += sum(d.points for d in team.drivers)
        
    user.fantasy_points = driver_points + constructor_points

@api.route('/leaderboard', methods=['GET'])
def get_leaderboard():
    users = User.query.filter_by(is_admin=False).all()
    
    leaderboard_data = []
    
    for u in users:
        calculate_user_points(u)
        
        team_value = 120.0 - u.budget
        leaderboard_data.append({
            "id": u.id,     
            "username": u.username,
            "points": int(u.fantasy_points), 
            "team_value": round(team_value, 1)
        })
    
    db.session.commit()
    
    leaderboard_data.sort(key=lambda x: x['points'], reverse=True)
    
    for i, row in enumerate(leaderboard_data):
        row['rank'] = i + 1
        
    return jsonify(leaderboard_data), 200

@api.route('/admin/update-prices', methods=['POST'])
def update_prices():
    data = request.get_json()
    driver = Driver.query.get(data.get('driver_id'))
    if driver:
        driver.price = float(data.get('new_price'))
        db.session.commit()
        return jsonify({"msg": "Price updated"}), 200
    return jsonify({"error": "Driver not found"}), 404

@api.route('/admin/trigger-update', methods=['POST'])
def trigger_update():
    print("--- STARTING POINTS UPDATE (2025) ---")
    try:
        all_drivers = Driver.query.all()
        for d in all_drivers: d.points = 0.0
        db.session.commit()
        
        schedule = fastf1.get_event_schedule(2025, include_testing=False)
        race_rounds = schedule[schedule['RoundNumber'] > 0]['RoundNumber'].tolist()
        
        for round_num in race_rounds:
            print(f"Processing Round {round_num}...")
            session = fastf1.get_session(2025, round_num, 'R')
            session.load(telemetry=False, weather=False, messages=False)
            results = session.results
            for index, row in results.iterrows():
                if row['Points'] > 0:
                    d = Driver.query.filter(Driver.surname.ilike(row['LastName'])).first()
                    if d: d.points += row['Points']
            db.session.commit()
        
        all_users = User.query.all()
        for u in all_users:
            calculate_user_points(u)
        db.session.commit()
            
        return jsonify({"msg": "Points successfully updated!"}), 200
    except Exception as e:
        print("Update Error:", e)
        return jsonify({"error": "Update failed"}), 500
    


# --- RACE LAB ---
@api.route('/race/weather/<int:round_num>', methods=['GET'])
def get_race_weather(round_num):
    try:
        session = fastf1.get_session(2025, round_num, 'R')
        session.load(telemetry=False, weather=True, messages=False)

        weather_data = session.weather_data
        
        data = []
        for i, row in weather_data.iterrows():
            time_min = row['Time'].total_seconds() / 60
            
            data.append({
                "time_min": round(time_min, 1),
                "air_temp": row['AirTemp'],
                "track_temp": row['TrackTemp'],
                "humidity": row['Humidity'],
                "rainfall": row['Rainfall']
            })
            
        return jsonify({
            "race": session.event['EventName'],
            "weather": data
        }), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500
    

@api.route('/race/tyres/<int:round_num>', methods=['GET'])
def get_tyre_strategy(round_num):
    try:
        session = fastf1.get_session(2025, round_num, 'R')
        session.load(telemetry=False, weather=False, messages=False)
       
        top_10_drivers = session.results.iloc[:10]['Abbreviation'].tolist()
        
        strategy_data = []

        for driver in top_10_drivers:
            laps = session.laps.pick_driver(driver)
            stints = []
            
            for stint_id, stint_laps in laps.groupby('Stint'):
                if len(stint_laps) == 0: continue
                
                compound = stint_laps['Compound'].iloc[0] 
                start_lap = int(stint_laps['LapNumber'].min())
                end_lap = int(stint_laps['LapNumber'].max())
                tyre_life_start = int(stint_laps['TyreLife'].min())
                
                stints.append({
                    "stint_number": int(stint_id),
                    "compound": compound,
                    "start_lap": start_lap,
                    "end_lap": end_lap,
                    "laps_driven": end_lap - start_lap + 1
                })
            
            strategy_data.append({
                "driver": driver,
                "stints": stints
            })

        return jsonify(strategy_data), 200

    except Exception as e:
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500
    

# --- AUTHENTICATION -- ANDROID  ---

@api.route('/register', methods=['POST'])
def register():
    data = request.get_json()
    
    if not data or not data.get('username') or not data.get('password'):
        return jsonify({'message': 'Missing username or password'}), 400
    
    if User.query.filter_by(username=data['username']).first():
        return jsonify({'message': 'Username already exists'}), 400
    
    try:
        new_user = User(username=data['username'])
        new_user.set_password(data['password'])
        
        db.session.add(new_user)
        db.session.commit()
        
        return jsonify({
            'message': 'User created successfully',
            'user_id': new_user.id,
            'username': new_user.username
        }), 201
    except Exception as e:
        return jsonify({'message': str(e)}), 500

@api.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    
    user = User.query.filter_by(username=data.get('username')).first()
    
    if user and user.check_password(data.get('password')):
        return jsonify({
            'message': 'Login successful',
            'user_id': user.id,
            'username': user.username,
            'is_admin': user.is_admin
        }), 200
    
    return jsonify({'message': 'Invalid credentials'}), 401