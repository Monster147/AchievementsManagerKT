import {useEffect, useReducer} from "react";
import {useNavigate, useParams} from "react-router";
import {api, ApiError} from "../api";
import {Game} from "../types/game/Game";
import {GameSource} from "../types/game/GameSource";
import {GamePlatform} from "../types/game/GamePlatform";
import {GameGenre} from "../types/game/GameGenre";
import {Achievement} from "../types/achievement/Achievement";
import {formatGameSource} from "../utils/formatGameSource.ts";

type GameForm = {
    externalGameId: string;
    name: string;
    genres: GameGenre[];
    platform: GamePlatform | "";
    releaseYear: string;
    source: GameSource;
    cover: string;
};

type TextField = "externalGameId" | "name" | "cover" | "releaseYear";

type EditGameState = {
    form: GameForm;
    achievements: Achievement[];
    stage: "loading" | "loaded" | "failed";
    saving: boolean;
    deleting: boolean;
    loadingAchievements: boolean;
    fetchingAchievements: boolean;
    error?: string;
    notice?: string;
};

type EditGameAction =
    | { type: "loaded"; game: Game }
    | { type: "load-error"; message: string }
    | { type: "set-text"; field: TextField; value: string }
    | { type: "set-source"; source: GameSource }
    | { type: "set-platform"; platform: GamePlatform | "" }
    | { type: "toggle-genre"; genre: GameGenre }
    | { type: "save-start" }
    | { type: "save-success"; game: Game }
    | { type: "save-error"; message: string }
    | { type: "delete-start" }
    | { type: "delete-error"; message: string }
    | { type: "achievements-loading" }
    | { type: "achievements-loaded"; achievements: Achievement[] }
    | { type: "achievements-error"; message: string }
    | { type: "fetch-start" }
    | { type: "fetch-done"; message: string }
    | { type: "fetch-error"; message: string };

function toForm(game: Game): GameForm {
    return {
        externalGameId: game.externalGameId,
        name: game.name,
        genres: game.genre ?? [],
        platform: game.platform ?? "",
        releaseYear: game.releaseYear ?? "",
        source: game.source,
        cover: game.cover ?? "",
    };
}

const emptyForm: GameForm = {
    externalGameId: "",
    name: "",
    genres: [],
    platform: "",
    releaseYear: "",
    source: GameSource.STEAM,
    cover: "",
};

const initialState: EditGameState = {
    form: emptyForm,
    achievements: [],
    stage: "loading",
    saving: false,
    deleting: false,
    loadingAchievements: false,
    fetchingAchievements: false,
};

function reduce(state: EditGameState, action: EditGameAction): EditGameState {
    switch (action.type) {
        case "loaded":
            return {...state, form: toForm(action.game), stage: "loaded", error: undefined};

        case "load-error":
            return {...state, stage: "failed", error: action.message};

        case "set-text":
            return {...state, form: {...state.form, [action.field]: action.value}, notice: undefined};

        case "set-source":
            return {...state, form: {...state.form, source: action.source}, notice: undefined};

        case "set-platform":
            return {...state, form: {...state.form, platform: action.platform}, notice: undefined};

        case "toggle-genre": {
            const has = state.form.genres.includes(action.genre);
            const genres = has
                ? state.form.genres.filter(g => g !== action.genre)
                : [...state.form.genres, action.genre];
            return {...state, form: {...state.form, genres}, notice: undefined};
        }

        case "save-start":
            return {...state, saving: true, error: undefined, notice: undefined};

        case "save-success":
            return {...state, saving: false, form: toForm(action.game), notice: "Game saved."};

        case "save-error":
            return {...state, saving: false, error: action.message};

        case "delete-start":
            return {...state, deleting: true, error: undefined};

        case "delete-error":
            return {...state, deleting: false, error: action.message};

        case "achievements-loading":
            return {...state, loadingAchievements: true};

        case "achievements-loaded":
            return {...state, loadingAchievements: false, achievements: action.achievements};

        case "achievements-error":
            return {...state, loadingAchievements: false, error: action.message};

        case "fetch-start":
            return {...state, fetchingAchievements: true, error: undefined, notice: undefined};

        case "fetch-done":
            return {...state, fetchingAchievements: false, notice: action.message};

        case "fetch-error":
            return {...state, fetchingAchievements: false, error: action.message};

        default:
            return state;
    }
}

function errorMessage(e: unknown, fallback: string): string {
    return e instanceof ApiError ? e.message : fallback;
}

export function EditGame() {
    const {id} = useParams();
    const gameId = Number(id);
    const navigate = useNavigate();
    const [state, dispatch] = useReducer(reduce, initialState);

    const loadAchievements = async () => {
        dispatch({type: "achievements-loading"});
        try {
            const achievements = await api.getAchievementsByGameId(gameId);
            dispatch({type: "achievements-loaded", achievements});
        } catch (e) {
            dispatch({type: "achievements-error", message: errorMessage(e, "Failed to load achievements.")});
        }
    };

    useEffect(() => {
        if (Number.isNaN(gameId)) {
            dispatch({type: "load-error", message: "Invalid game id."});
            return;
        }
        api.getGameById(gameId)
            .then(game => dispatch({type: "loaded", game}))
            .catch(e => dispatch({type: "load-error", message: errorMessage(e, "Failed to load game.")}));
        loadAchievements();
    }, [gameId]);

    const setText = (field: TextField) => (
        e: React.ChangeEvent<HTMLInputElement>,
    ) => dispatch({type: "set-text", field, value: e.target.value});

    const handleSave = async (e: React.FormEvent) => {
        e.preventDefault();
        dispatch({type: "save-start"});
        try {
            const game = await api.updateGame(gameId, {
                externalGameId: state.form.externalGameId,
                name: state.form.name,
                genres: state.form.genres.length > 0 ? state.form.genres : undefined,
                platform: state.form.platform || undefined,
                releaseYear: state.form.releaseYear || undefined,
                source: state.form.source,
                cover: state.form.cover || undefined,
            });
            dispatch({type: "save-success", game});
        } catch (e) {
            dispatch({type: "save-error", message: errorMessage(e, "Failed to save game.")});
        }
    };

    const handleDelete = async () => {
        if (!confirm("Delete this game? This cannot be undone.")) {
            return;
        }
        dispatch({type: "delete-start"});
        try {
            await api.deleteGame(gameId);
            navigate("/games");
        } catch (e) {
            dispatch({type: "delete-error", message: errorMessage(e, "Failed to delete game.")});
        }
    };

    const handleFetchAchievements = async () => {
        dispatch({type: "fetch-start"});
        try {
            await api.fetchAchievements({
                internalGameId: gameId,
                externalGameId: state.form.externalGameId,
                source: state.form.source,
            });
            dispatch({type: "fetch-done", message: "Achievements fetched from source."});
            await loadAchievements();
        } catch (e) {
            dispatch({type: "fetch-error", message: errorMessage(e, "Failed to fetch achievements.")});
        }
    };

    if (state.stage === "loading") {
        return <p>Loading game...</p>;
    }

    if (state.stage === "failed") {
        return <div className="form-error">{state.error}</div>;
    }

    return (
        <div className="edit-game-page">
            <div className="edit-game-card">
                <div className="auth-header">
                    <h1 className="auth-title">Edit Game</h1>
                    <p className="auth-description">Edit or delete this game.</p>
                </div>

                <form className="auth-form" onSubmit={handleSave}>
                    <div className="form-group">
                        <label>Game Name</label>
                        <input value={state.form.name} onChange={setText("name")}/>
                    </div>
                    <div className="form-group">
                        <label>External Game Id</label>
                        <input value={state.form.externalGameId} onChange={setText("externalGameId")}/>
                    </div>
                    <div className="form-group">
                        <label>Source</label>
                        <select
                            value={state.form.source}
                            onChange={e => dispatch({type: "set-source", source: e.target.value as GameSource})}
                        >
                            <option value={GameSource.STEAM}>Steam</option>
                            <option value={GameSource.PSN}>PlayStation</option>
                            <option value={GameSource.RETROACHIEVEMENTS}>RetroAchievements</option>
                        </select>
                    </div>
                    <div className="form-group">
                        <label>Platform</label>
                        <select
                            value={state.form.platform}
                            onChange={e =>
                                dispatch({type: "set-platform", platform: e.target.value as GamePlatform | ""})
                            }
                        >
                            <option value="">— Select platform —</option>
                            {Object.values(GamePlatform).map(p => (
                                <option key={p} value={p}>{p}</option>
                            ))}
                        </select>
                    </div>
                    <div className="form-group">
                        <label>Genres</label>
                        <div className="genre-picker">
                            {Object.values(GameGenre).map(g => {
                                const selected = state.form.genres.includes(g);
                                return (
                                    <button
                                        type="button"
                                        key={g}
                                        className={selected ? "genre-chip selected" : "genre-chip"}
                                        onClick={() => dispatch({type: "toggle-genre", genre: g})}
                                    >
                                        {g}
                                    </button>
                                );
                            })}
                        </div>
                        {state.form.genres.length > 0 && (
                            <span className="genre-count">
                                {state.form.genres.length} selected
                            </span>
                        )}
                    </div>
                    <div className="form-group">
                        <label>Release Year</label>
                        <select
                            value={state.form.releaseYear}
                            onChange={e =>
                                dispatch({
                                    type: "set-text",
                                    field: "releaseYear",
                                    value: e.target.value,
                                })
                            }
                        >
                            <option value="">— Select year —</option>

                            {Array.from(
                                { length: 2050 - 1990 + 1 },
                                (_, i) => 1990 + i
                            ).map(year => (
                                <option key={year} value={year}>
                                    {year}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div className="form-group">
                        <label>Cover URL</label>
                        <input value={state.form.cover} placeholder="https://..." onChange={setText("cover")}/>
                    </div>

                    {state.error && <div className="form-error">{state.error}</div>}
                    {state.notice && <p className="edit-game-notice">{state.notice}</p>}

                    <div className="edit-game-actions">
                        <button className="create-game-button" disabled={state.saving}>
                            {state.saving ? "Saving..." : "Save Changes"}
                        </button>
                        <button
                            type="button"
                            className="delete-game-button"
                            onClick={handleDelete}
                            disabled={state.deleting}
                        >
                            {state.deleting ? "Deleting..." : "Delete Game"}
                        </button>
                    </div>
                </form>
            </div>

            <div className="edit-game-card">
                <div className="edit-achievements-header">
                    <h2 className="section-title">Achievements</h2>
                    <button
                        className="create-game-button"
                        onClick={handleFetchAchievements}
                        disabled={state.fetchingAchievements}
                    >
                        {state.fetchingAchievements
                            ? "Fetching..."
                            : `Fetch from ${formatGameSource(state.form.source)}`}
                    </button>
                </div>

                {state.loadingAchievements ? (
                    <p>Loading achievements...</p>
                ) : state.achievements.length === 0 ? (
                    <div className="empty-games">No achievements found.</div>
                ) : (
                    <div className="achievements-list">
                        {state.achievements.map(a => (
                            <div key={a.id} className="achievement-card">
                                <img
                                    className="achievement-icon"
                                    src={a.icon ?? "/no-cover.png"}
                                    alt={a.name}
                                />
                                <div className="achievement-info">
                                    <h3>{a.name}</h3>
                                    <p>{a.description}</p>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}
