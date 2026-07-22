import { useReducer } from "react";
import { useNavigate } from "react-router";
import { api, ApiError } from "../api";
import { GameSource } from "../types/game/GameSource";

type CreateGameState = {
    name: string;
    externalGameId: string;
    source: GameSource;
    cover: string;

    stage: "editing" | "posting" | "success" | "failed";
    error?: string;
};

type CreateGameAction =
    | {
    type: "input-change";
    name: string;
    externalGameId: string;
    source: GameSource;
    cover: string;
}
    | {
    type: "post";
}
    | {
    type: "success";
}
    | {
    type: "error";
    message: string;
};

function reduce(
    state: CreateGameState,
    action: CreateGameAction,
): CreateGameState {
    switch (action.type) {

        case "input-change":
            return {
                ...state,
                name: action.name,
                externalGameId: action.externalGameId,
                source: action.source,
                cover: action.cover,
            };

        case "post":
            return {
                ...state,
                stage: "posting",
                error: undefined,
            };

        case "success":
            return {
                name: "",
                externalGameId: "",
                source: GameSource.STEAM,
                cover: "",
                stage: "success",
            };

        case "error":
            return {
                ...state,
                stage: "failed",
                error: action.message,
            };

        default:
            return state;
    }
}

const initialState: CreateGameState = {
    name: "",
    externalGameId: "",
    source: GameSource.STEAM,
    cover: "",
    stage: "editing",
};

export function CreateGame() {

    const [state, dispatch] =
        useReducer(reduce, initialState);

    const navigate = useNavigate();

    const handleSubmit = async (
        e: React.FormEvent,
    ) => {

        e.preventDefault();

        dispatch({
            type: "post",
        });

        try {
            await api.createGame({
                name: state.name,
                externalGameId: state.externalGameId,
                source: state.source,
                cover: state.cover || undefined,
            });

            dispatch({
                type: "success",
            });

            navigate("/games");

        } catch (e) {

            dispatch({
                type: "error",
                message:
                    e instanceof ApiError
                        ? e.message
                        : "Failed to create game.",
            });

        }

    };

    return (
        <div className="auth-page">
            <div className="auth-card">
                <div className="auth-header">
                    <h1 className="auth-title">
                        Create Custom Game
                    </h1>
                    <p className="auth-description">
                        Add a custom game manually to the
                        database.
                    </p>
                </div>
                <form
                    className="auth-form"
                    onSubmit={handleSubmit}
                >
                    <div className="form-group">
                        <label>
                            Game Name
                        </label>
                        <input
                            value={state.name}
                            placeholder="Minecraft"
                            onChange={(e) =>
                                dispatch({
                                    type: "input-change",
                                    name: e.target.value,
                                    externalGameId:
                                    state.externalGameId,
                                    source: state.source,
                                    cover: state.cover,
                                })
                            }
                        />
                    </div>
                    <div className="form-group">
                        <label>
                            External Game Id
                        </label>
                        <input
                            value={state.externalGameId}
                            placeholder="minecraft"
                            onChange={(e) =>
                                dispatch({
                                    type: "input-change",
                                    name: state.name,
                                    externalGameId:
                                    e.target.value,
                                    source: state.source,
                                    cover: state.cover,
                                })
                            }
                        />
                    </div>
                    <div className="form-group">
                        <label>
                            Source
                        </label>
                        <select
                            value={state.source}
                            onChange={(e) =>
                                dispatch({
                                    type: "input-change",
                                    name: state.name,
                                    externalGameId:
                                    state.externalGameId,
                                    source:
                                        e.target
                                            .value as GameSource,
                                    cover: state.cover,
                                })
                            }
                        >
                            <option value={GameSource.STEAM}>
                                Steam
                            </option>
                            <option value={GameSource.PSN}>
                                PlayStation
                            </option>
                            <option
                                value={
                                    GameSource.RETROACHIEVEMENTS
                                }
                            >
                                RetroAchievements
                            </option>
                        </select>

                    </div>

                    <div className="form-group">

                        <label>
                            Cover URL
                        </label>

                        <input
                            value={state.cover}
                            placeholder="https://..."
                            onChange={(e) =>
                                dispatch({
                                    type: "input-change",
                                    name: state.name,
                                    externalGameId:
                                    state.externalGameId,
                                    source: state.source,
                                    cover: e.target.value,
                                })
                            }
                        />

                    </div>

                    {state.error && (
                        <div className="form-error">
                            {state.error}
                        </div>
                    )}

                    <button
                        className="create-game-button"
                        disabled={
                            state.stage === "posting"
                        }
                    >
                        {state.stage === "posting"
                            ? "Creating..."
                            : "Create Game"}
                    </button>

                </form>

            </div>

        </div>
    );
}