import { useEffect, useReducer } from "react";
import { api, ApiError } from "../api";
import { GameSource } from "../types/game/GameSource";
import { SearchedGame } from "../types/game/SearchedGame";

type SearchState = {
    gameName: string;
    source: GameSource;
    results: SearchedGame[];
    stage: "editing" | "searching" | "finished" | "failed";
    error?: string;
};

type SearchAction =
    | {
    type: "input-change";
    gameName: string;
    source: GameSource;
}
    | {
    type: "search";
}
    | {
    type: "success";
    results: SearchedGame[];
}
    | {
    type: "error";
    message: string;
}
    | {
    type: "remove-result";
    id: number;
}
    | {
    type: "load-cache";
    results: SearchedGame[];
};

function reduce(
    state: SearchState,
    action: SearchAction,
): SearchState {
    switch (action.type) {
        case "input-change":
            return {
                ...state,
                gameName: action.gameName,
                source: action.source,
            };

        case "search":
            return {
                ...state,
                stage: "searching",
                error: undefined,
            };

        case "success":
            return {
                ...state,
                stage: "finished",
                results: action.results,
            };

        case "error":
            return {
                ...state,
                stage: "failed",
                error: action.message,
            };

        case "remove-result":
            return {
                ...state,
                results: state.results.filter(
                    g => g.id !== action.id,
                ),
            };

        case "load-cache":
            return {
                ...state,
                results: action.results,
                stage: "editing",
            };

        default:
            return state;
    }
}

const initialState: SearchState = {
    gameName: "",
    source: GameSource.STEAM,
    results: [],
    stage: "editing",
};

export function SearchGames() {
    const [state, dispatch] = useReducer(reduce, initialState);

    useEffect(() => {
        const loadCache = async () => {
            try {
                const cache = await api.getSearchCache();
                dispatch({
                    type: "load-cache",
                    results: cache,
                });
            } catch {
            }
        };
        loadCache();
    }, []);

    const handleSearch = async (
        e: React.FormEvent,
    ) => {
        e.preventDefault();
        dispatch({ type: "search" });
        try {
            await api.searchGames({
                gameName: state.gameName,
                source: state.source,
            });
            const games = await api.getSearchCache();
            dispatch({
                type: "success",
                results: games,
            });
        } catch (e) {
            dispatch({
                type: "error",
                message:
                    e instanceof ApiError
                        ? e.message
                        : "Failed to search games.",
            });
        }
    };

    const handleAddGame = async (
        id: number,
    ) => {
        try {
            console.log("Id (searched game): ", id)
            await api.addGameFromCache(id);
            dispatch({
                type: "remove-result",
                id,
            });
            alert("Game added successfully!");
        } catch (e) {
            alert(
                e instanceof ApiError
                    ? e.message
                    : "Failed to add game.",
            );
        }
    };

    return (
        <div className="search-page">
            <div className="search-card">
                <h1>Search Games</h1>
                <form className="search-form" onSubmit={handleSearch}>
                    <div className="form-group">
                        <label>Game Name</label>
                        <input
                            value={state.gameName}
                            onChange={(e) =>
                                dispatch({
                                    type: "input-change",
                                    gameName: e.target.value,
                                    source: state.source,
                                })
                            }
                            placeholder="Elden Ring..."
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label>Platform</label>
                        <select
                            value={state.source}
                            onChange={(e) =>
                                dispatch({
                                    type: "input-change",
                                    gameName: state.gameName,
                                    source:
                                        e.target
                                            .value as GameSource,
                                })
                            }
                        >
                            <option value={GameSource.STEAM}>Steam</option>
                            <option value={GameSource.PSN}>PlayStation</option>
                        </select>
                    </div>
                    <button
                        className="search-button"
                        disabled={
                            state.stage ===
                            "searching"
                        }
                    >
                        {state.stage ===
                        "searching"
                            ? "Searching..."
                            : "Search"}
                    </button>
                </form>
                {state.error && (
                    <div className="form-error">
                        {state.error}
                    </div>
                )}
                <div className="search-results">
                    {state.results.map(game => (
                        <div key={game.id} className="search-result-card">
                            <img
                                src={
                                    game.cover ??
                                    "/no-cover.png"
                                }
                                alt={game.name}
                            />
                            <div className="search-result-info">
                                <h3>{game.name}</h3>
                                <p>{game.source}</p>
                            </div>
                            <button
                                className="create-game-button"
                                onClick={() =>
                                    handleAddGame(
                                        game.id,
                                    )
                                }
                            >
                                Add Game
                            </button>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}