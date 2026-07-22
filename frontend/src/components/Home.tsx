import { Link } from "react-router";
import {useAuth} from "../AuthContext.tsx";
import {UserRole} from "../types/user/UserRole.ts";

export function Home() {
    const { user } = useAuth();
    const isUserAdmin = user?.role === UserRole.ADMIN;
    return (
        <>
            <div className="intro-text">
                <h2>Welcome to Achievements Management</h2>
                <p>Manage your games and achievements from here!</p>
                <Link className="games-button" to="/games">
                    All Games
                </Link>
            </div>
            {isUserAdmin && (
                <div className="admin-options">
                    <h2>Admin Options</h2>
                    <div className="admin-options-buttons">
                        <Link className="search-button" to="/search-games">
                            Search Games
                        </Link>

                        <Link className="create-game-button" to="/create-game">
                            Create a Custom Game
                        </Link>
                    </div>
                </div>
            )
            }
        </>
    );
}