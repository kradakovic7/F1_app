from app import create_app, db
from app.models import Team, Driver, User, RaceResult

app = create_app()

def seed_database():
    with app.app_context():
        
        db.drop_all()
        db.create_all()

        # --- TEAMS ---
        teams_data = [
            {"name": "Ferrari HP", "price": 26.0},
            {"name": "Oracle Red Bull Racing", "price": 27.5},
            {"name": "Mercedes-AMG PETRONAS", "price": 24.0},
            {"name": "McLaren Formula 1 Team", "price": 26.5},
            {"name": "Aston Martin Aramco", "price": 14.0},
            {"name": "BWT Alpine F1 Team", "price": 10.5},
            {"name": "Williams Racing", "price": 11.5},
            {"name": "Kick Sauber", "price": 8.0}, 
            {"name": "Visa Cash App RB", "price": 9.5},
            {"name": "Haas F1 Team", "price": 8.5},
        ]

        teams_objects = {}
        for t_data in teams_data:
            team = Team(name=t_data["name"], price=t_data["price"])
            db.session.add(team)
            teams_objects[t_data["name"]] = team
        
        db.session.commit()

        # --- DRIVERS ---
        drivers_data = [
            {"name": "Charles", "surname": "Leclerc", "team": "Ferrari HP", "price": 28.0},
            {"name": "Lewis", "surname": "Hamilton", "team": "Ferrari HP", "price": 27.5},
            {"name": "Max", "surname": "Verstappen", "team": "Oracle Red Bull Racing", "price": 31.0},
            {"name": "Yuki", "surname": "Tsunoda", "team": "Oracle Red Bull Racing", "price": 16.0},
            {"name": "George", "surname": "Russell", "team": "Mercedes-AMG PETRONAS", "price": 23.0},
            {"name": "Andrea Kimi", "surname": "Antonelli", "team": "Mercedes-AMG PETRONAS", "price": 12.0},
            {"name": "Lando", "surname": "Norris", "team": "McLaren Formula 1 Team", "price": 29.0},
            {"name": "Oscar", "surname": "Piastri", "team": "McLaren Formula 1 Team", "price": 26.0},
            {"name": "Fernando", "surname": "Alonso", "team": "Aston Martin Aramco", "price": 14.5},
            {"name": "Lance", "surname": "Stroll", "team": "Aston Martin Aramco", "price": 9.0},
            {"name": "Pierre", "surname": "Gasly", "team": "BWT Alpine F1 Team", "price": 11.0},
            {"name": "Franco", "surname": "Colapinto", "team": "BWT Alpine F1 Team", "price": 9.0},
            {"name": "Alexander", "surname": "Albon", "team": "Williams Racing", "price": 12.5},
            {"name": "Carlos", "surname": "Sainz", "team": "Williams Racing", "price": 19.0},
            {"name": "Nico", "surname": "Hulkenberg", "team": "Kick Sauber", "price": 9.5},
            {"name": "Gabriel", "surname": "Bortoleto", "team": "Kick Sauber", "price": 7.5},
            {"name": "Liam", "surname": "Lawson", "team": "Visa Cash App RB", "price": 10.0},
            {"name": "Isack", "surname": "Hadjar", "team": "Visa Cash App RB", "price": 8.0},
            {"name": "Esteban", "surname": "Ocon", "team": "Haas F1 Team", "price": 10.5},
            {"name": "Oliver", "surname": "Bearman", "team": "Haas F1 Team", "price": 9.0},
        ]

        for d_data in drivers_data:
            team_obj = teams_objects.get(d_data["team"])
            if team_obj:
                driver = Driver(
                    name=d_data["name"],
                    surname=d_data["surname"],
                    price=d_data["price"],
                    team=team_obj
                )
                db.session.add(driver)

        # --- ADMIN ---
        admin = User(username="Admin", is_admin=True, budget=999.0)
        admin.set_password("admin123")
        db.session.add(admin)

        db.session.commit()

if __name__ == "__main__":
    seed_database()