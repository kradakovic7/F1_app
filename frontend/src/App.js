import React, { useEffect, useState } from 'react';
import axios from 'axios';
import 'bootstrap/dist/css/bootstrap.min.css';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend, ReferenceLine } from 'recharts';

function App() {
  // CONFIG
  const API_URL = 'http://localhost:5000'; 
  
  const NEXT_RACE_DATE = "2026-03-02T15:00:00"; 

  // STATE
  const [user, setUser] = useState(null); 
  const [authMode, setAuthMode] = useState('login'); 
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [adminMsg, setAdminMsg] = useState("");

  const [activeTab, setActiveTab] = useState('drivers'); 
  const [drivers, setDrivers] = useState([]);
  const [teams, setTeams] = useState([]);
  const [races, setRaces] = useState([]);
  const [leaderboard, setLeaderboard] = useState([]);

  // Fantasy Team
  const [myDrivers, setMyDrivers] = useState([]);
  const [myConstructors, setMyConstructors] = useState([]);
  const [budget, setBudget] = useState(120.0);
  const [teamSaved, setTeamSaved] = useState(false);
  
  // Telemetry & Results
  const [compDriver1, setCompDriver1] = useState("VER");
  const [compDriver2, setCompDriver2] = useState("HAM");
  const [selectedRaceTelemetry, setSelectedRaceTelemetry] = useState("Italian Grand Prix");
  const [telemetryData, setTelemetryData] = useState(null);
  const [loadingTelemetry, setLoadingTelemetry] = useState(false);
  const [selectedRaceResults, setSelectedRaceResults] = useState(null);

  // --- WEATHER & TYRES STATE ---
  const [selectedAnalysisRound, setSelectedAnalysisRound] = useState(1);
  const [weatherData, setWeatherData] = useState(null);
  const [tyreData, setTyreData] = useState(null);
  const [loadingAnalysis, setLoadingAnalysis] = useState(false);

  // Features State
  const [isDarkMode, setIsDarkMode] = useState(true); 
  const [countdown, setCountdown] = useState("");
  const [favDriverId, setFavDriverId] = useState(null);
  const [themeColor, setThemeColor] = useState("#dc3545");

  // --- THEME ENGINE ---
  useEffect(() => {
    const interval = setInterval(() => {
        const now = new Date().getTime();
        const distance = new Date(NEXT_RACE_DATE).getTime() - now;
        if (distance < 0) {
            setCountdown("RACE STARTED!");
        } else {
            const days = Math.floor(distance / (1000 * 60 * 60 * 24));
            const hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
            const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
            setCountdown(`${days}d ${hours}h ${minutes}m`);
        }
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    if (favDriverId && drivers.length > 0) {
        const driver = drivers.find(d => d.id === favDriverId);
        if (driver) {
            if (driver.team.includes("Red Bull")) setThemeColor("#3671C6"); 
            else if (driver.team.includes("Ferrari")) setThemeColor("#E80020"); 
            else if (driver.team.includes("Mercedes")) setThemeColor("#00D2BE"); 
            else if (driver.team.includes("McLaren")) setThemeColor("#FF8000"); 
            else if (driver.team.includes("Aston")) setThemeColor("#229971"); 
            else if (driver.team.includes("Alpine")) setThemeColor("#0093CC"); 
            else if (driver.team.includes("Williams")) setThemeColor("#0e1490"); 
            else if (driver.team.includes("Visa")) setThemeColor("#1e21c0"); 
            else if (driver.team.includes("Sauber")) setThemeColor("#52E252"); 
            else if (driver.team.includes("Haas")) setThemeColor("#B6BABD");
            else setThemeColor("#dc3545"); // Default 
        }
    }
  }, [favDriverId, drivers]);

  useEffect(() => {
    if (user) {
        fetchDrivers();
        fetchTeams();
        fetchCalendar();
        fetchMyTeam();
        fetchLeaderboard();
        if(user.fav_driver_id) setFavDriverId(user.fav_driver_id);
    }
  }, [user]);

  // --- AUTH ---
  const handleLogin = async (e) => {
      e.preventDefault();
      try {
          const res = await axios.post(`${API_URL}/auth/login`, { username, password });
          setUser(res.data.user);
      } catch (err) { alert("Login failed"); }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    try {
        await axios.post(`${API_URL}/auth/register`, { username, password });
        alert("Success! Please log in.");
        setAuthMode('login');
    } catch (err) { alert("Registration failed"); }
  };

  const handleLogout = () => {
      setUser(null);
      setMyDrivers([]);
      setMyConstructors([]);
      setTeamSaved(false);
      setBudget(120.0);
      setUsername('');
      setPassword('');
      setActiveTab('drivers');
  };

  const handleDeleteAccount = async () => {
      if (!window.confirm("Are you sure you want to delete your account?\nThis will permanently remove your team and ranking.\nThis action cannot be undone.")) return;

      try {
          await axios.delete(`${API_URL}/api/delete-user/${user.id}`);
          alert("Account deleted successfully. Goodbye!");
          handleLogout(); 
      } catch (e) {
          alert("Error deleting account: " + (e.response?.data?.error || e.message));
      }
  };

  // --- DATA FETCHING ---
  const fetchDrivers = async () => { try { const res = await axios.get(`${API_URL}/api/drivers`); setDrivers(res.data); } catch (e) {} };
  const fetchTeams = async () => { try { const res = await axios.get(`${API_URL}/api/constructors`); setTeams(res.data); } catch (e) {} };
  const fetchCalendar = async () => { try { const res = await axios.get(`${API_URL}/api/calendar`); setRaces(res.data); } catch (e) {} };
  const fetchLeaderboard = async () => { try { const res = await axios.get(`${API_URL}/api/leaderboard`); setLeaderboard(res.data); } catch (e) {} };

  const fetchMyTeam = async () => {
      if (!user) return;
      try {
          const res = await axios.get(`${API_URL}/api/fantasy/my-team/${user.id}`);
          if (res.data.drivers && res.data.drivers.length > 0) {
              setTeamSaved(true);
              setBudget(res.data.budget);
              setMyDrivers(res.data.drivers.map(d => ({...d, team: d.team}))); 
              
              setMyConstructors(res.data.teams || res.data.constructors || []); 
              
          } else {
              setTeamSaved(false);
              setBudget(120.0);
              setMyConstructors([]); 
              setMyDrivers([]);
          }
      } catch (e) {}
  };

  // --- ACTIONS ---
  const handlePriceUpdate = async (driverId, newPrice) => {
      try {
          await axios.post(`${API_URL}/api/admin/update-prices`, { driver_id: driverId, new_price: newPrice });
          fetchDrivers();
          setAdminMsg("Price updated!");
          setTimeout(() => setAdminMsg(""), 3000);
      } catch (e) { alert("Error updating price"); }
  };

  const triggerPointsUpdate = async () => {
      if(!window.confirm("Download data from FastF1? This takes time.")) return;
      setAdminMsg("Updating points... please wait...");
      try {
          await axios.post(`${API_URL}/api/admin/trigger-update`);
          fetchDrivers();
          setAdminMsg("Points updated successfully!");
      } catch (e) { setAdminMsg("Error updating points."); }
  };

  const saveTeam = async () => {
      if (myDrivers.length !== 5 || myConstructors.length !== 2) {
          alert("Select 5 drivers and 2 constructors.");
          return;
      }
      try {
          await axios.post(`${API_URL}/api/fantasy/create-team`, {
              user_id: user.id,
              driver_ids: myDrivers.map(d => d.id),
              constructor_ids: myConstructors.map(c => c.id)
          });
          setTeamSaved(true);
          fetchLeaderboard();
          alert("Team saved!");
      } catch (e) { alert("Error: " + (e.response?.data?.error)); }
  };

  const deleteTeam = async () => {
      if(!window.confirm("Are you sure? This will delete your team and reset your budget.")) return;
      try {
          await axios.delete(`${API_URL}/api/fantasy/delete-team/${user.id}`);
          setTeamSaved(false);
          setMyDrivers([]);
          setMyConstructors([]);
          setBudget(120.0);
          fetchLeaderboard();
          alert("Team deleted!");
      } catch (e) { alert("Error deleting team"); }
  }

  const handleFavDriverChange = async (e) => {
      const id = parseInt(e.target.value);
      setFavDriverId(id);
      try {
          await axios.post(`${API_URL}/api/user/favorite-driver`, { user_id: user.id, driver_id: id });
      } catch(e) {}
  }

  const handleDriverSelect = (driver) => {
    if (teamSaved) return; 
    if (myDrivers.find(d => d.id === driver.id)) {
        setMyDrivers(myDrivers.filter(d => d.id !== driver.id));
        setBudget(prev => prev + driver.price);
    } else {
        if (myDrivers.length >= 5) { alert("Limit 5 drivers!"); return; }
        if (budget - driver.price < 0) { alert("Not enough budget!"); return; }
        setMyDrivers([...myDrivers, driver]);
        setBudget(prev => prev - driver.price);
    }
  };

  const handleConstructorSelect = (team) => {
    if (teamSaved) return;
    if (myConstructors.find(c => c.id === team.id)) {
        setMyConstructors(myConstructors.filter(c => c.id !== team.id));
        setBudget(prev => prev + team.price);
    } else {
        if (myConstructors.length >= 2) { alert("Limit 2 teams!"); return; }
        if (budget - team.price < 0) { alert("Not enough budget!"); return; }
        setMyConstructors([...myConstructors, team]);
        setBudget(prev => prev - team.price);
    }
  };

  const compareDrivers = async () => { 
      setLoadingTelemetry(true); setTelemetryData(null); 
      try { 
          const res = await axios.get(`${API_URL}/api/telemetry/compare?d1=${compDriver1}&d2=${compDriver2}&race=${selectedRaceTelemetry}`); 
          setTelemetryData(res.data); 
      } catch (e) { alert("No data available."); } 
      finally { setLoadingTelemetry(false); } 
  };

  // --- NEW: ANALYSIS FUNCTIONS ---
  const fetchAnalysisData = async () => {
    setLoadingAnalysis(true);
    setWeatherData(null);
    setTyreData(null);
    try {
        const weatherRes = await axios.get(`${API_URL}/api/race/weather/${selectedAnalysisRound}`);
        setWeatherData(weatherRes.data);

        const tyreRes = await axios.get(`${API_URL}/api/race/tyres/${selectedAnalysisRound}`);
        setTyreData(tyreRes.data);
    } catch (e) {
        alert("Data not available for this race. Make sure it has happened!");
    } finally {
        setLoadingAnalysis(false);
    }
  };

  const getTyreColor = (compound) => {
      const c = compound.toUpperCase();
      if (c.includes("SOFT")) return "#FF3333"; 
      if (c.includes("MEDIUM")) return "#FFD700"; 
      if (c.includes("HARD")) return "#F0F0F0"; 
      if (c.includes("INTER")) return "#39B54A"; 
      if (c.includes("WET")) return "#0072BB";
      return "#888";
  };
  
  const showRaceResults = async (round, raceName) => { 
      try { 
          const res = await axios.get(`${API_URL}/api/results/${round}`); 
          setSelectedRaceResults({ name: raceName, data: res.data }); 
      } catch (e) {} 
  };
  
  const getTeamStyle = (teamName) => { 
      let color="#6c757d"; let text="white"; 
      if(!teamName) return {}; 
      if(teamName.includes("Ferrari")) color="#E80020"; 
      else if(teamName.includes("Red Bull")) color="#3671C6"; 
      else if(teamName.includes("Mercedes")) color="#00D2BE"; 
      else if(teamName.includes("McLaren")) {color="#FF8000"; text="black";} 
      else if(teamName.includes("Aston")) color="#229971"; 
      else if(teamName.includes("Alpine")) color="#0093CC"; 
      else if(teamName.includes("Visa")) color="#1e21c0"; 
      else if(teamName.includes("Williams")) color="#0e1490"; 
      else if(teamName.includes("Sauber")) {color="#52E252"; text="black";} 
      else if(teamName.includes("Haas")) {color="#B6BABD"; text="black";} 
      return {backgroundColor:color, color:text, border:'1px solid rgba(0,0,0,0.1)'};  
  };

  // --- RENDER ---
  const renderContent = () => {
      switch(activeTab) {
          case 'admin':
            return (
                <div className={`card shadow border-warning ${isDarkMode ? 'bg-secondary text-white' : ''}`}>
                    <div className="card-header bg-warning text-dark"><h4 className="mb-0">🔒 Admin Dashboard</h4></div>
                    <div className="card-body">
                        {adminMsg && <div className="alert alert-info">{adminMsg}</div>}
                        <div className="mb-4">
                            <h5>⚙️ System Operations</h5>
                            <button className="btn btn-dark me-2 border-white" onClick={triggerPointsUpdate}>🔄 Force Update Points (2025 Real Data)</button>
                        </div>
                        <hr className="bg-white"/>
                        <h5>💰 Manage Driver Prices</h5>
                        <div className="table-responsive" style={{maxHeight: '400px'}}>
                            <table className={`table table-sm ${isDarkMode ? 'table-dark' : 'table-hover'}`}>
                                <thead className={isDarkMode ? "text-white" : "table-light"}><tr><th>Driver</th><th>Current Price</th><th>New Price</th><th>Action</th></tr></thead>
                                <tbody>
                                    {drivers.map(d => (
                                        <tr key={d.id}>
                                            <td>{d.name}</td>
                                            <td>{d.price} M€</td>
                                            <td><input type="number" step="0.5" className="form-control form-control-sm" style={{width: '80px'}} id={`price-${d.id}`} defaultValue={d.price} /></td>
                                            <td><button className="btn btn-sm btn-success" onClick={() => { const val = document.getElementById(`price-${d.id}`).value; handlePriceUpdate(d.id, val); }}>Save</button></td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            );

          case 'leaderboard':
            return (
                <div className={`card shadow-sm border-0 ${isDarkMode ? 'bg-secondary text-white' : ''}`} style={{maxWidth: '800px', margin: '0 auto'}}>
                    <div className="card-header text-center text-white" style={{backgroundColor: themeColor}}><h3 className="mb-0 fw-bold">🏆 Global Championship</h3></div>
                    <div className="card-body p-0">
                        <table className={`table mb-0 text-center ${isDarkMode ? 'table-dark' : 'table-striped'}`}>
                            <thead><tr><th>Rank</th><th>Manager</th><th>Team Value</th><th>Total Points</th></tr></thead>
                            <tbody>
                                {leaderboard.map((u) => (
                                    <tr key={u.rank} className={u.username === user.username ? "fw-bold" : ""}>
                                            <td>{u.rank}</td>
                                            <td>{u.username} {u.username === user.username && "(You)"}</td>
                                            <td>{u.team_value.toFixed(1)} M€</td>
                                            <td className="fs-5">{u.points}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            );
          
          case 'myteam': return (
            <div className="row">
                <div className="col-md-6">
                    <div className={`card shadow-sm border-0 mb-3 ${isDarkMode ? 'bg-secondary text-white' : ''}`}>
                        <div className="card-header text-white d-flex justify-content-between" style={{backgroundColor: themeColor}}><span>🛒 Driver Market</span><span>Budget: {budget.toFixed(1)} M€</span></div>
                        <div className="card-body p-0" style={{maxHeight: '350px', overflowY: 'auto'}}>
                            <table className={`table table-sm mb-0 ${isDarkMode ? 'table-dark' : 'table-hover'}`}>
                                <thead><tr><th>Name</th><th>Price</th><th>Action</th></tr></thead>
                                <tbody>
                                    {drivers.map(d => { 
                                        const isSelected = myDrivers.find(md => md.id === d.id); 
                                        return (
                                            <tr key={d.id} className={isSelected ? (isDarkMode ? "table-active" : "table-success") : ""}>
                                                <td className="fw-bold">{d.name} <span className="badge ms-1" style={getTeamStyle(d.team)}>{d.team}</span></td>
                                                <td>{d.price} M€</td>
                                                <td><button disabled={teamSaved} className={`btn btn-sm ${isSelected ? 'btn-danger' : 'btn-outline-success'}`} onClick={() => handleDriverSelect(d)}>{isSelected ? '-' : '+'}</button></td>
                                            </tr>
                                        )
                                    })}
                                </tbody>
                            </table>
                        </div>
                    </div>
                    
                    <div className={`card shadow-sm border-0 ${isDarkMode ? 'bg-secondary text-white' : ''}`}>
                        <div className="card-header text-white" style={{backgroundColor: themeColor}}>🏭 Constructor Market</div>
                        <div className="card-body p-0" style={{maxHeight: '250px', overflowY: 'auto'}}>
                            <table className={`table table-sm mb-0 ${isDarkMode ? 'table-dark' : 'table-hover'}`}>
                                <thead><tr><th>Team</th><th>Price</th><th>Action</th></tr></thead>
                                <tbody>
                                    {teams.map(t => { 
                                        const isSelected = myConstructors.find(c => c.id === t.id); 
                                        return (
                                            <tr key={t.id} className={isSelected ? (isDarkMode ? "table-active" : "table-success") : ""}>
                                                <td className="fw-bold">{t.name}</td>
                                                <td>{t.price} M€</td>
                                                <td><button disabled={teamSaved} className={`btn btn-sm ${isSelected ? 'btn-danger' : 'btn-outline-success'}`} onClick={() => handleConstructorSelect(t)}>{isSelected ? '-' : '+'}</button></td>
                                            </tr>
                                        )
                                    })}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <div className="col-md-6">
                    <div className={`card shadow ${isDarkMode ? 'bg-dark text-white border-secondary' : 'border-danger'}`}>
                        <div className="card-header text-white text-center" style={{backgroundColor: themeColor}}>
                            <h4 className="mb-0">🏎️ My Squad</h4>
                        </div>
                        <div className={`card-body text-center ${isDarkMode ? 'bg-dark' : 'bg-light'}`}>
                            {teamSaved && <div className="alert alert-success">Team Locked!</div>}
                            <h6 className="text-muted mb-2 text-start">Drivers (Select 5)</h6>
                            <div className="d-flex flex-wrap justify-content-center gap-2 mb-3">
                                {[0, 1, 2, 3, 4].map(i => (
                                    <div key={i} className={`border rounded p-2 shadow-sm ${isDarkMode ? 'bg-secondary border-dark' : 'bg-white'}`} style={{width: '30%', minWidth: '100px', fontSize:'0.9rem'}}>
                                            {myDrivers[i] ? (<><div className="fw-bold text-truncate">{myDrivers[i].name.split(" ")[1]}</div><div className="small opacity-75">{myDrivers[i].price} M€</div></>) : <span className="opacity-50 small">Empty</span>}
                                    </div>
                                ))}
                            </div>
                            <h6 className="text-muted mb-2 text-start">Constructors (Select 2)</h6>
                            <div className="d-flex justify-content-center gap-2 mb-4">
                                {[0, 1].map(i => (
                                    <div key={i} className={`border rounded p-3 shadow-sm ${isDarkMode ? 'bg-secondary border-dark' : 'bg-white'}`} style={{width: '45%'}}>
                                            {myConstructors[i] ? (<><div className="fw-bold text-truncate">{myConstructors[i].name}</div><div className="small opacity-75">{myConstructors[i].price} M€</div></>) : <span className="opacity-50">Empty Slot</span>}
                                    </div>
                                ))}
                            </div>
                            
                            <h4 className="mb-3">Remaining: <span className={budget < 0 ? "text-danger" : "text-success"}>{budget.toFixed(1)} M€</span></h4>
                            
                            {teamSaved ? (
                                <button className="btn btn-lg btn-danger w-100" onClick={deleteTeam}>🗑️ DELETE TEAM & RESET</button>
                            ) : (
                                <button className="btn btn-lg w-100 text-white" style={{backgroundColor: themeColor}} onClick={saveTeam} disabled={myDrivers.length !== 5 || myConstructors.length !== 2 || budget < 0}>💾 SAVE TEAM</button>
                            )}
                        </div>
                    </div>
                </div>
            </div>
          );

          case 'drivers': return (
            <div className={`card shadow-sm border-0 ${isDarkMode ? 'bg-secondary text-white' : ''}`}>
                <div className="card-header text-white" style={{backgroundColor: themeColor}}> Driver Standings 2025</div>
                <div className="card-body p-0">
                    <table className={`table mb-0 ${isDarkMode ? 'table-dark' : 'table-striped'}`}>
                        <thead><tr><th>#</th><th>Driver</th><th>Team</th><th className="text-end">Points</th></tr></thead>
                        <tbody>{drivers.map((d, i) => (<tr key={d.id}><th>{i+1}</th><td className="fw-bold">{d.name}</td><td><span className="badge" style={getTeamStyle(d.team)}>{d.team}</span></td><td className="text-end fw-bold">{d.points}</td></tr>))}</tbody>
                    </table>
                </div>
            </div>
          );

          case 'teams': return (
            <div className={`card shadow-sm border-0 ${isDarkMode ? 'bg-secondary text-white' : ''}`}>
                <div className="card-header text-white" style={{backgroundColor: themeColor}}> Constructor Standings 2025</div>
                <div className="card-body p-0">
                    <table className={`table mb-0 ${isDarkMode ? 'table-dark' : 'table-striped'}`}>
                        <thead><tr><th>#</th><th>Team</th><th>Drivers</th><th className="text-end">Points</th></tr></thead>
                        <tbody>{teams.map((t, i) => (
                            <tr key={t.id}>
                                <th>{i+1}</th>
                                <td><span className="badge" style={getTeamStyle(t.name)}>{t.name}</span></td>
                                {/* FIX: Visible text in Dark Mode */}
                                <td className={`small ${isDarkMode ? 'text-white-50' : 'text-muted'}`}>{t.drivers.join(", ")}</td>
                                <td className="text-end fw-bold fs-5">{t.points}</td>
                            </tr>
                        ))}</tbody>
                    </table>
                </div>
            </div>
          );
          
          case 'races': return (
            <div>
                <h4 className={`mb-3 ${isDarkMode ? 'text-white' : 'text-dark'}`}>2025 Race Calendar</h4>
                <div className="row">
                    {races.map((race) => (
                        <div className="col-md-3 mb-3" key={race.round}>
                            <div className={`card h-100 shadow-sm ${isDarkMode ? 'bg-secondary text-white border-0' : ''}`} onClick={() => showRaceResults(race.round, race.name)} style={{cursor: 'pointer'}}>
                                <div className="card-body">
                                    <h6 className="fw-bold" style={{color: themeColor}}>R{race.round}</h6>
                                    <h5 className="card-title">{race.name}</h5>
                                    <p className={`card-text ${isDarkMode ? 'text-light opacity-75' : 'text-muted'}`}><small>📍 {race.location}<br/>{race.date}</small></p>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
                {selectedRaceResults && (
                    <div className="modal d-block" style={{backgroundColor: 'rgba(0,0,0,0.5)'}}>
                        <div className="modal-dialog modal-lg">
                            <div className={`modal-content ${isDarkMode ? 'bg-dark text-white' : ''}`}>
                                <div className="modal-header text-white" style={{backgroundColor: themeColor}}>
                                    <h5 className="modal-title">{selectedRaceResults.name} Results</h5>
                                    <button className="btn-close btn-close-white" onClick={() => setSelectedRaceResults(null)}></button>
                                </div>
                                <div className="modal-body p-0">
                                    <table className={`table mb-0 ${isDarkMode ? 'table-dark' : 'table-striped'}`}>
                                            <thead><tr><th>Pos</th><th>Driver</th><th>Team</th><th>Time</th><th>Pts</th></tr></thead>
                                            <tbody>{selectedRaceResults.data.map((res) => (<tr key={res.position}><td className="fw-bold">{res.position}</td><td>{res.driver}</td><td>{res.team}</td><td>{res.time}</td><td className="fw-bold">+{res.points}</td></tr>))}</tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </div>
          );
          
          case 'telemetry': 
            return (
                <div className={`card shadow-sm border-0 ${isDarkMode ? 'bg-secondary text-white' : ''}`}>
                    <div className="card-header text-white" style={{backgroundColor: themeColor}}>
                        <h5 className="mb-0">📈 Advanced Telemetry Analysis</h5>
                    </div>
                    <div className="card-body">
                        <div className="row g-2 mb-4">
                            <div className="col-md-12 mb-2">
                                <label className="form-label fw-bold">Select Grand Prix</label>
                                <select className="form-select" value={selectedRaceTelemetry} onChange={e => setSelectedRaceTelemetry(e.target.value)}>
                                    {races.map(r => <option key={r.round} value={r.name}>{r.name}</option>)}
                                </select>
                            </div>
                            <div className="col-md-5">
                                <label className="form-label small">Driver 1</label>
                                <select className="form-select border-danger" value={compDriver1} onChange={e => setCompDriver1(e.target.value)}>
                                    {drivers.map(d => (<option key={d.id} value={d.surname.substring(0, 3).toUpperCase()}>{d.name}</option>))}
                                </select>
                            </div>
                            <div className="col-md-2 text-center align-self-end fw-bold">VS</div>
                            <div className="col-md-5">
                                <label className="form-label small">Driver 2</label>
                                <select className="form-select border-primary" value={compDriver2} onChange={e => setCompDriver2(e.target.value)}>
                                    {drivers.map(d => (<option key={d.id} value={d.surname.substring(0, 3).toUpperCase()}>{d.name}</option>))}
                                </select>
                            </div>
                            <div className="col-md-12 mt-3">
                                <button className="btn w-100 text-white" style={{backgroundColor: themeColor}} onClick={compareDrivers} disabled={loadingTelemetry}>
                                    {loadingTelemetry ? 'Downloading Data...' : 'Analyze Telemetry'}
                                </button>
                            </div>
                        </div>
                        {telemetryData && (
                            <div style={{ width: '100%', height: 400 }}>
                                <h5 className="text-center">{telemetryData.track} Speed</h5>
                                <ResponsiveContainer>
                                    <LineChart data={telemetryData.telemetry}>
                                        <CartesianGrid strokeDasharray="3 3" />
                                        <XAxis dataKey="dist" label={{ value: 'Distance (m)', position: 'insideBottomRight', offset: 0 }} />
                                        <YAxis domain={[0, 360]} />
                                        <Tooltip contentStyle={{ backgroundColor: "#333", color: "#fff" }} />
                                        <Legend verticalAlign="top"/>
                                        <Line type="monotone" name={compDriver1} dataKey="d1_speed" stroke="#dc3545" strokeWidth={2} dot={false} />
                                        <Line type="monotone" name={compDriver2} dataKey="d2_speed" stroke="#0d6efd" strokeWidth={2} dot={false} />
                                    </LineChart>
                                </ResponsiveContainer>
                            </div>
                        )}
                    </div>
                </div>
            );

          // --- NEW: RACE LAB (Weather & Tyres) ---
          case 'analysis':
             // NEW: Dynamic colors for graphs
             const axisColor = isDarkMode ? "#ffffff" : "#333333";

             return (
                <div className={`card shadow-sm border-0 ${isDarkMode ? 'bg-secondary text-white' : ''}`}>
                    <div className="card-header text-white" style={{backgroundColor: themeColor}}>
                        <h5 className="mb-0"> Race Lab: Deep Dive</h5>
                    </div>
                    <div className="card-body">
                         <div className="mb-4 d-flex gap-2">
                             <select className="form-select" value={selectedAnalysisRound} onChange={e => setSelectedAnalysisRound(e.target.value)}>
                                 {races.map(r => <option key={r.round} value={r.round}>{r.name} (R{r.round})</option>)}
                             </select>
                             <button className="btn btn-primary text-nowrap" onClick={fetchAnalysisData} disabled={loadingAnalysis}>
                                 {loadingAnalysis ? "Loading..." : "Load Data"}
                             </button>
                         </div>
                         
                         {weatherData && (
                             <div className="mb-5">
                                 <h5 className="border-bottom pb-2">🌧️ Weather Conditions</h5>
                                 <div style={{ width: '100%', height: 300 }}>
                                    <ResponsiveContainer>
                                        <LineChart data={weatherData.weather}>
                                            {/* UPDATED: Dynamic grid and axis colors */}
                                            <CartesianGrid strokeDasharray="3 3" stroke={isDarkMode ? "#555" : "#ccc"} />
                                            <XAxis dataKey="time_min" label={{value: 'Time (min)', position: 'insideBottom', fill: axisColor}} stroke={axisColor} />
                                            <YAxis yAxisId="left" stroke={axisColor} label={{value: 'Temp (°C)', angle: -90, position: 'insideLeft', fill: axisColor}} domain={['auto', 'auto']} />
                                            <YAxis yAxisId="right" orientation="right" stroke="#82ca9d" label={{value: 'Humidity (%)', angle: 90, position: 'insideRight', fill: '#82ca9d'}} domain={[0, 100]} />
                                            <Tooltip contentStyle={{ backgroundColor: "#222", color: "#fff", border: "1px solid #555" }} />
                                            <Legend />
                                            <Line yAxisId="left" type="monotone" dataKey="track_temp" name="Track Temp" stroke="#ff7300" strokeWidth={2} dot={false} />
                                            <Line yAxisId="left" type="monotone" dataKey="air_temp" name="Air Temp" stroke="#387908" strokeWidth={2} dot={false} />
                                            <Line yAxisId="right" type="monotone" dataKey="humidity" name="Humidity" stroke="#82ca9d" strokeDasharray="5 5" dot={false} />
                                        </LineChart>
                                    </ResponsiveContainer>
                                 </div>
                             </div>
                         )}

                         {tyreData && (
                             <div>
                                 <h5 className={`border-bottom pb-2 ${isDarkMode ? 'text-white' : 'text-dark'}`}>🍩 Tyre Strategy (Top 10)</h5>
                                 <div className="mt-3">
                                     {tyreData.map(d => (
                                         <div key={d.driver} className="mb-2 d-flex align-items-center">
                                             {/* UPDATED: Driver name color based on mode */}
                                             <div style={{width: '50px', fontWeight: 'bold', color: isDarkMode ? 'white' : 'black'}}>{d.driver}</div>
                                             
                                             <div className="flex-grow-1 d-flex" style={{height: '35px', backgroundColor: '#444', borderRadius: '6px', overflow: 'hidden'}}>
                                                 {d.stints.map((stint, i) => (
                                                     <div 
                                                         key={i} 
                                                         className="d-flex justify-content-center align-items-center text-dark fw-bold small"
                                                         style={{
                                                             width: `${(stint.laps_driven / 60) * 100}%`, 
                                                             backgroundColor: getTyreColor(stint.compound),
                                                             borderRight: i < d.stints.length - 1 ? '3px solid #15151E' : 'none', 
                                                             fontSize: '0.8rem'
                                                         }}
                                                         title={`${stint.compound}: Laps ${stint.start_lap}-${stint.end_lap}`}
                                                      >
                                                         {stint.laps_driven > 3 ? stint.compound.substring(0,1) : ''}
                                                      </div>
                                                 ))}
                                             </div>
                                         </div>
                                     ))}
                                     <div className={`d-flex gap-3 small mt-2 justify-content-end ${isDarkMode ? 'text-white' : 'text-dark'}`}>
                                         <span className="badge text-dark" style={{backgroundColor: '#FF3333'}}>Soft</span>
                                         <span className="badge text-dark" style={{backgroundColor: '#FFD700'}}>Medium</span>
                                         <span className="badge text-dark" style={{backgroundColor: '#F0F0F0'}}>Hard</span>
                                         <span className="badge text-white" style={{backgroundColor: '#39B54A'}}>Inter</span>
                                         <span className="badge text-white" style={{backgroundColor: '#0072BB'}}>Wet</span>
                                     </div>
                                 </div>
                             </div>
                         )}
                    </div>
                </div>
             );

          default: return null;
      }
  };

  // LOGIN UI
  if (!user) {
      return (
          <div className="d-flex justify-content-center align-items-center min-vh-100 bg-dark">
              <div className="card p-5 shadow-lg bg-secondary text-white" style={{width: '400px'}}>
                  <h2 className="text-center mb-4 fw-bold">🏎️ F1 Fantasy</h2>
                  <form onSubmit={authMode === 'login' ? handleLogin : handleRegister}>
                      <div className="mb-3">
                          <label className="form-label">Username</label>
                          <input type="text" className="form-control" value={username} onChange={e => setUsername(e.target.value)} required />
                      </div>
                      <div className="mb-3">
                          <label className="form-label">Password</label>
                          <input type="password" className="form-control" value={password} onChange={e => setPassword(e.target.value)} required />
                      </div>
                      <button type="submit" className="btn btn-danger w-100 mb-3">{authMode === 'login' ? 'Login' : 'Register'}</button>
                  </form>
                  <div className="text-center">
                      <small className="opacity-75">
                          {authMode === 'login' ? "New here? " : "Already have an account? "}
                          <span className="text-info" style={{cursor: 'pointer'}} onClick={() => setAuthMode(authMode === 'login' ? 'register' : 'login')}>
                              {authMode === 'login' ? "Create Account" : "Login"}
                          </span>
                      </small>
                  </div>
              </div>
          </div>
      );
  }

  return (
    <div className={`min-vh-100 pb-5 ${isDarkMode ? 'bg-dark' : 'bg-light'}`} style={{transition: 'background 0.3s'}}>
      <nav className="navbar navbar-expand-lg navbar-dark shadow mb-4" style={{backgroundColor: themeColor, transition: 'background 0.3s'}}>
        <div className="container-fluid px-4">
          <span className="navbar-brand fw-bold">🏎️ F1 Hub</span>
          <div className="d-flex align-items-center text-white small me-3">
              <span className="me-2 d-none d-md-inline">NEXT RACE:</span>
              <span className="fw-bold bg-white text-dark px-2 rounded">{countdown}</span>
          </div>
          
          {/* UPDATED: Prominent Pill Buttons */}
          <div className="navbar-nav flex-row align-items-center gap-2">
            {['drivers', 'teams', 'races', 'myteam', 'leaderboard', 'telemetry', 'analysis'].map(tab => (
                <button 
                    key={tab}
                    className={`btn btn-sm rounded-pill fw-bold px-3 ${activeTab === tab ? 'btn-light text-dark shadow' : 'btn-outline-light border-0'}`} 
                    onClick={() => setActiveTab(tab)}
                    style={{transition: 'all 0.2s'}}
                >
                    {tab.charAt(0).toUpperCase() + tab.slice(1).replace("myteam", "Fantasy").replace("analysis", "Race Lab")}
                </button>
            ))}
            
            {/* ADMIN TAB */}
            {user && user.is_admin && (
                <button className={`btn btn-sm rounded-pill fw-bold px-3 ms-2 ${activeTab === 'admin' ? 'btn-warning text-dark' : 'btn-outline-warning'}`} onClick={() => setActiveTab('admin')}>Admin</button>
            )}
          </div>
          
          {/* USER PROFILE & THEME SELECTOR */}
          <div className="d-flex align-items-center gap-2 ms-auto">
              <div className="d-flex align-items-center bg-white bg-opacity-10 rounded-pill px-3 py-1">
                 <div className="d-flex flex-column me-2" style={{lineHeight: '1.1'}}>
                    <span className="text-white fw-bold small text-end">{user.username} {user.is_admin && "(Admin)"}</span>
                    <label className="text-white-50 small text-end" style={{fontSize: '0.65rem'}}> Theme</label>
                 </div>
                 <select 
                    className="form-select form-select-sm border-0 bg-white text-dark fw-bold rounded-pill" 
                    style={{width: '130px', fontSize:'0.75rem', height: '30px', boxShadow: 'none'}} 
                    value={favDriverId || ""} 
                    onChange={handleFavDriverChange}
                 >
                    <option value="" disabled>Select Driver</option>
                    {drivers.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
                </select>
              </div>
              
              <button className="btn btn-sm btn-outline-light rounded-circle" style={{width: '32px', height: '32px'}} onClick={() => setIsDarkMode(!isDarkMode)}>
                  {isDarkMode ? '☀️' : '🌙'}
              </button>
              
              {/* DELETE ACCOUNT BUTTON */}
              <button 
                  className="btn btn-sm btn-outline-danger ms-2" 
                  onClick={handleDeleteAccount} 
                  title="Delete Account"
              >
                  🗑️
              </button>

              <button className="btn btn-sm btn-outline-light" onClick={handleLogout}>Logout</button>
          </div>
        </div>
      </nav>

      <div className="container-fluid px-4">
        {renderContent()}
      </div>
    </div>
  );
}

export default App;