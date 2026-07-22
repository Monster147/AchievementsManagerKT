import {useAuth} from "../AuthContext.tsx";
import {useEffect, useState} from "react";
import {api, ApiError} from "../api.ts";
import {useNavigate} from "react-router";
import {FaUserCircle} from "react-icons/fa";
import { IntegrationSettings } from "./IntegrationSettings";

export function UserProfile() {
    const {user, logout} = useAuth();
    const navigate = useNavigate();
    const [userStats, setUserStats] = useState<any>(null);
    const [loadingStats, setLoadingStats] = useState(true);
    const [errorStats, setErrorStats] = useState<string | null>(null);

    useEffect(() => {
        if (!user) return;
        api.getUserStats(user.id)
            .then((data) => setUserStats(data))
            .catch((err) => setErrorStats(err instanceof ApiError ? err.message : "Failed to load stats"))
            .finally(() => setLoadingStats(false));
    }, [user]);

    const handleLogout = () => {
        logout();
        navigate("/login");
    };

    if (!user) return <div>Loading...</div>;
    if (loadingStats|| !userStats) return <div>Loading stats...</div>;
    if (errorStats) return <div>Error loading stats: {errorStats}</div>;

    return (
        <div className="profile-page">
            <div className="profile-card">

                <div className="profile-header">
                    <FaUserCircle size={90} />

                    <h2>{user.name}</h2>

                    <p>{user.email}</p>

                    <span className="profile-role">
                        {user.role}
                    </span>
                </div>

                <section className="stats-section">
                    <h3 className="section-title">
                        Overview
                    </h3>

                    <div className="profile-stats">
                        <div className="stat-card">
                            <h3>{userStats.totalGames}</h3>
                            <p>Games</p>
                        </div>

                        <div className="stat-card">
                            <h3>{userStats.gamesWithAchievements}</h3>
                            <p>Games Started</p>
                        </div>

                        <div className="stat-card">
                            <h3>{userStats.completionPercentage.toFixed(1)}%</h3>
                            <p>Completion</p>
                        </div>
                    </div>
                </section>

                <section className="stats-section">
                    <h3 className="section-title">
                        Achievements
                    </h3>

                    <div className="profile-stats">
                        <div className="stat-card">
                            <h3>{userStats.totalAchievements}</h3>
                            <p>Total</p>
                        </div>

                        <div className="stat-card">
                            <h3>{userStats.unlockedAchievements}</h3>
                            <p>Unlocked</p>
                        </div>

                        <div className="stat-card">
                            <h3>{userStats.lockedAchievements}</h3>
                            <p>Locked</p>
                        </div>
                    </div>
                </section>

                <IntegrationSettings />

                <section className="stats-section">
                    <h3 className="section-title">
                        Account
                    </h3>

                    <button
                        className="logout-button"
                        onClick={handleLogout}
                    >
                        Logout
                    </button>
                </section>

            </div>
        </div>
    );
}