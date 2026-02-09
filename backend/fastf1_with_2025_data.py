from app import create_app, db
from app.models import Driver
import fastf1

app = create_app()

def update_season_points():
    with app.app_context():
        print("--- POSODOBITEV TOČK (SEZONA 2025) ---")
        
        drivers = Driver.query.all()
        for d in drivers:
            d.points = 0.0
        db.session.commit()
        print("Točke ponastavljene na 0.")

        # include_testing=False pomeni samo dirke
        schedule = fastf1.get_event_schedule(2025, include_testing=False)
        
        # dirke, ki so že bile odpeljane
        race_rounds = schedule[schedule['RoundNumber'] > 0]['RoundNumber'].tolist()
        
        print(f"Najdenih {len(race_rounds)} dirk. Prenašam rezultate...")

        for round_num in race_rounds:
            try:
                print(f"Obdelujem dirko {round_num}...", end="\r")
                

                session = fastf1.get_session(2025, round_num, 'R')
                session.load(telemetry=False, weather=False, messages=False)
                results = session.results
                
                for index, row in results.iterrows():
                    points = row['Points']
                    last_name = row['LastName']
                    
                    if points > 0:
                        driver = Driver.query.filter(Driver.surname.ilike(last_name)).first()
                        
                        if driver:
                            driver.points += points
                
                db.session.commit()
                
            except Exception as e:
                print(f"\nNapaka pri dirki {round_num}: {e}")

        print("\n--- KONČANO! Vsi vozniki imajo posodobljene točke. ---")

if __name__ == "__main__":
    fastf1.Cache.enable_cache('f1_cache')
    update_season_points()