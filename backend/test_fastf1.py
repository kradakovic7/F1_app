import fastf1
import os

# 1. SETUP CACHE (Highly recommended in docs)
# We use the same cache folder as your main app
cache_dir = 'f1_cache'
if not os.path.exists(cache_dir):
    os.makedirs(cache_dir)

fastf1.Cache.enable_cache(cache_dir)

print("--- STARTING FASTF1 TEST ---")
print("Loading 2021 French Grand Prix - Qualifying...")

# 2. LOAD SESSION
# As described: fastf1.get_session(Year, Location/Name, SessionIdentifier)
session = fastf1.get_session(2025, 7, 'Q')

# Load the data (this downloads from the internet)
session.load()

# 3. INSPECT EVENT INFO
print(f"\nEvent Name: {session.event['EventName']}")
print(f"Date: {session.event['EventDate']}")

# 4. INSPECT RESULTS
# Showing the top 5 drivers and their Q3 times
print("\n--- TOP 5 DRIVERS (Q3 TIMES) ---")
# accessing .results dataframe
results_view = session.results.iloc[0:5].loc[:, ['Abbreviation', 'TeamName', 'Q3']]
print(results_view)

# 5. INSPECT LAPS
# Finding the absolute fastest lap of the session
print("\n--- POLE POSITION LAP ---")
fastest_lap = session.laps.pick_fastest()

driver = fastest_lap['Driver']
time = fastest_lap['LapTime']
tyre = fastest_lap['Compound']

print(f"Driver: {driver}")
print(f"Time:   {time}")
print(f"Tyre:   {tyre}")

print("\n--- TEST COMPLETE ---")