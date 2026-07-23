import {useEffect, useReducer} from "react";
import {useAuth} from "../AuthContext";
import {api, ApiError} from "../api";
import {Game} from "../types/game/Game";
import {GameSource} from "../types/game/GameSource";
import {formatGameSource} from "../utils/formatGameSource.ts";
import {UserRole} from "../types/user/UserRole.ts";
import {Link} from "react-router";

type ViewMode = "grid" | "list";

type GamesState = {
    games: Game[];
    filteredGames: Game[];
    search: string;
    source: "ALL" | GameSource;
    view: ViewMode;
    stage: "loading" | "loaded" | "failed";
    error?: string;
};

type GamesAction =
    | {
    type: "load";
}
    | {
    type: "success";
    games: Game[];
}
    | {
    type: "error";
    message: string;
}
    | {
    type: "set-search";
    value: string;
}
    | {
    type: "set-source";
    source: "ALL" | GameSource;
}
    | {
    type: "set-view";
    view: ViewMode;
};

function applyFilters(
    games: Game[],
    search: string,
    source: "ALL" | GameSource,
): Game[] {
    return games.filter(game => {
        const matchesName =
            game.name
                .toLowerCase()
                .includes(search.toLowerCase());

        const matchesSource =
            source === "ALL" ||
            game.source === source;

        return matchesName && matchesSource;
    });
}

function reduce(
    state: GamesState,
    action: GamesAction,
): GamesState {
    switch (action.type) {
        case "load":
            return {
                ...state,
                stage: "loading",
                error: undefined,
            };

        case "success":
            return {
                ...state,
                games: action.games,
                filteredGames: applyFilters(
                    action.games,
                    state.search,
                    state.source,
                ),
                stage: "loaded",
            };

        case "error":
            return {
                ...state,
                stage: "failed",
                error: action.message,
            };

        case "set-search":
            return {
                ...state,
                search: action.value,
                filteredGames: applyFilters(
                    state.games,
                    action.value,
                    state.source,
                ),
            };

        case "set-source":
            return {
                ...state,
                source: action.source,
                filteredGames: applyFilters(
                    state.games,
                    state.search,
                    action.source,
                ),
            };

        case "set-view":
            return {
                ...state,
                view: action.view,
            };

        default:
            return state;
    }
}

const initialState: GamesState = {
    games: [],
    filteredGames: [],
    search: "",
    source: "ALL",
    view: "grid",
    stage: "loading",
};

export function GamesList() {
    const { user } = useAuth();
    const [state, dispatch] = useReducer(reduce, initialState);
    const isUserAdmin = user?.role === UserRole.ADMIN;

    useEffect(() => {
        dispatch({
            type: "load",
        });
        api.getGames()
            .then(games =>
                dispatch({
                    type: "success",
                    games,
                }),
            )
            .catch(err =>
                dispatch({
                    type: "error",
                    message:
                        err instanceof ApiError
                            ? err.message
                            : "Failed to load games.",
                }),
            );
    }, []);

    const handleAddGame = async (
        gameId: number,
    ) => {
        try {
            await api.addGameToLibrary({
                gameId,
            });
            alert(
                "Game added to your library!",
            );
        } catch (e) {
            alert(
                e instanceof ApiError
                    ? e.message
                    : "Failed to add game.",
            );
        }
    };

    if (state.stage === "loading") {
        return <p>Loading games...</p>;
    }

    if (state.stage === "failed") {
        return (
            <div className="form-error">
                {state.error}
            </div>
        );
    }

    return (
        <div className="games-page">
            <div className="games-card">
                <h1>Games</h1>
                <div className="games-toolbar">
                    <div className="form-group games-search">
                        <label>Search</label>
                        <input
                            value={state.search}
                            placeholder="Search game..."
                            onChange={(e) =>
                                dispatch({
                                    type: "set-search",
                                    value: e.target.value,
                                })
                            }
                        />
                    </div>
                    <div className="form-group">
                        <label>Source</label>
                        <select
                            value={state.source}
                            onChange={(e) =>
                                dispatch({
                                    type: "set-source",
                                    source:
                                        e.target.value ===
                                        "ALL"
                                            ? "ALL"
                                            : e.target
                                                .value as GameSource,
                                })
                            }
                        >
                            <option value="ALL">All</option>
                            <option value={GameSource.STEAM}>Steam</option>
                            <option value={GameSource.PSN}>PlayStation</option>
                            <option value={GameSource.RETROACHIEVEMENTS}>RetroAchievements</option>
                        </select>
                    </div>
                    <div className="games-view">
                        <button
                            className={
                                state.view ===
                                "grid"
                                    ? "create-game-button"
                                    : "games-button"
                            }
                            onClick={() =>
                                dispatch({
                                    type: "set-view",
                                    view: "grid",
                                })
                            }
                        >
                            Grid
                        </button>
                        <button
                            className={
                                state.view ===
                                "list"
                                    ? "create-game-button"
                                    : "games-button"
                            }
                            onClick={() =>
                                dispatch({
                                    type: "set-view",
                                    view: "list",
                                })
                            }
                        >
                            List
                        </button>
                    </div>
                </div>
                {state.filteredGames.length === 0 ? (
                    <div className="empty-games">
                        No games found.
                    </div>
                ) : (
                    <div
                        className={
                            state.view ===
                            "grid"
                                ? "games-grid"
                                : "games-list"
                        }
                    >
                        {state.filteredGames.map(
                            game => (
                                <div
                                    key={game.id}
                                    className={
                                        state.view ===
                                        "grid"
                                            ? "game-card"
                                            : "game-card-list"
                                    }
                                >
                                    <img
                                        className={
                                            state.view ===
                                            "grid"
                                                ? "game-cover"
                                                : "game-cover-list"
                                        }
                                        src={
                                            game.cover ??
                                            "/no-cover.png"
                                        }
                                        alt={game.name}
                                    />
                                    <div className="game-info">
                                        <h3>{game.name}</h3>
                                        <p>
                                            <strong>
                                                Source:
                                            </strong>{" "}
                                            {formatGameSource(game.source)}
                                        </p>
                                        {game.platform && (
                                            <p>
                                                <strong>
                                                    Platform:
                                                </strong>{" "}
                                                {
                                                    game.platform
                                                }
                                            </p>
                                        )}
                                        {game.releaseYear && (
                                            <p>
                                                <strong>
                                                    Release:
                                                </strong>{" "}
                                                {
                                                    game.releaseYear
                                                }
                                            </p>
                                        )}
                                    </div>
                                    <div className="game-actions">
                                        {user && (
                                            <button
                                                className="create-game-button"
                                                onClick={() => handleAddGame(game.id)}
                                            >
                                                Add to Library
                                            </button>
                                        )}

                                        {isUserAdmin && (
                                            <Link
                                                className="games-button"
                                                to={`/games/${game.id}/edit`}
                                            >
                                                Edit
                                            </Link>
                                        )}
                                    </div>
                                </div>
                            ),
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}