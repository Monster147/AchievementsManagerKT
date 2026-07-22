import { Link } from "react-router";
import { FaUserCircle } from "react-icons/fa";
import {useAuth} from "../AuthContext.tsx";
import {useState} from "react";

export function Header() {
    const { user } = useAuth();

    const [theme, setTheme] = useState(() => {
        return document.body.className || "light";
    });

    const toggleTheme = () => {
        const next =
            theme === "light"
                ? "theme-dark-blue"
                : theme === "theme-dark-blue"
                    ? "theme-dark-red"
                    : "light";

        document.body.className = next === "light" ? "" : next;
        setTheme(next);
    };

    return (
        <header className="header">
            <div className="header-logo">
                <h1>Achievements Manager</h1>
            </div>

            <nav className="header-nav">
                <Link className="nav-button" to="/">
                    Home
                </Link>

                <Link className="nav-button" to="/my-games">
                    My Games
                </Link>
            </nav>

            <div className="header-auth">
                {user ? (
                    <>
                        <Link className="user-info" to="/profile">
                            <FaUserCircle size={24} />
                            <span>
                                Bem-vindo, <strong>{user.name}</strong>
                            </span>
                        </Link>
                    </>
                ) : (
                    <>
                        <Link className="login-button" to="/login">
                            Login
                        </Link>

                        <Link className="register-button" to="/register">
                            Register
                        </Link>
                    </>
                )}
            </div>

            <button className="theme-button" onClick={toggleTheme}>
                🎨
            </button>
        </header>
    );
}