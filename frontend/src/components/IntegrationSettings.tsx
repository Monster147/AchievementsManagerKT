import { useEffect, useReducer } from "react";
import { api, ApiError } from "../api";
import { IntegrationsConfig } from "../types/config/IntegrationsConfig";
import {UpdateIntegrationsConfigInput} from "../types/config/UpdatedIntegrationsConfigInput.ts";

type IntegrationsState = {
    steamApiKey: string;
    steamUserId: string;
    retroApiKey: string;
    retroUsername: string;
    psnApiKey: string;
    error?: string;
    stage: "loading" | "editing" | "posting" | "failed";
};

type IntegrationsAction =
    | {
    type: "load";
    config: IntegrationsConfig;
}
    | {
    type: "input-change";
    field: keyof UpdateIntegrationsConfigInput;
    value: string;
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

const initState: IntegrationsState = {
    steamApiKey: "",
    steamUserId: "",
    retroApiKey: "",
    retroUsername: "",
    psnApiKey: "",
    stage: "loading",
};

function reducer(
    state: IntegrationsState,
    action: IntegrationsAction,
): IntegrationsState {
    switch (action.type) {
        case "load": {
            return {
                steamApiKey: action.config.steamApiKey ?? "",
                steamUserId: action.config.steamUserId ?? "",
                retroApiKey: action.config.retroApiKey ?? "",
                retroUsername: action.config.retroUsername ?? "",
                psnApiKey: action.config.psnApiKey ?? "",
                stage: "editing",
            };
        }

        case "input-change":
            return {
                ...state,
                [action.field]: action.value,
            };

        case "post":
            return {
                ...state,
                stage: "posting",
                error: undefined,
            };

        case "success":
            return {
                ...state,
                stage: "editing",
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

export function IntegrationSettings() {
    const [state, dispatch] = useReducer(reducer, initState);

    useEffect(() => {
        api.getConfigs()
            .then((config) => {
                    console.log("Config: ", config);
                    dispatch({
                        type: "load",
                        config,
                    })
                }
            )
            .catch((err) =>
                dispatch({
                    type: "error",
                    message:
                        err instanceof ApiError
                            ? err.message
                            : "Failed to load integrations.",
                }),
            );
    }, []);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        dispatch({ type: "post" });

        try {
            await api.updateConfigs({
                steamApiKey: state.steamApiKey || undefined,
                steamUserId: state.steamUserId || undefined,
                retroApiKey: state.retroApiKey || undefined,
                retroUsername: state.retroUsername || undefined,
                psnApiKey: state.psnApiKey || undefined,
            });

            dispatch({ type: "success" });
        } catch (err) {
            dispatch({
                type: "error",
                message:
                    err instanceof ApiError
                        ? err.message
                        : "Failed to save integrations.",
            });
        }
    };

    if (state.stage === "loading") {
        return <p>Loading integrations...</p>;
    }

    console.log("Estado: ", state);

    return (
        <section className="stats-section">
            <h3 className="section-title">
                Integrations
            </h3>

            <form
                className="auth-form"
                onSubmit={handleSubmit}
            >
                <h4>Steam</h4>

                <div className="form-group">
                    <label htmlFor="steamApiKey">
                        Steam API Key
                    </label>

                    <input
                        id="steamApiKey"
                        value={state.steamApiKey}
                        onChange={(e) =>
                            dispatch({
                                type: "input-change",
                                field: "steamApiKey",
                                value: e.target.value,
                            })
                        }
                    />
                </div>

                <div className="form-group">
                    <label htmlFor="steamUserId">
                        Steam User ID
                    </label>

                    <input
                        id="steamUserId"
                        value={state.steamUserId}
                        onChange={(e) =>
                            dispatch({
                                type: "input-change",
                                field: "steamUserId",
                                value: e.target.value,
                            })
                        }
                    />
                </div>

                <h4>RetroAchievements</h4>

                <div className="form-group">
                    <label htmlFor="retroApiKey">
                        API Key
                    </label>

                    <input
                        id="retroApiKey"
                        value={state.retroApiKey}
                        onChange={(e) =>
                            dispatch({
                                type: "input-change",
                                field: "retroApiKey",
                                value: e.target.value,
                            })
                        }
                    />
                </div>

                <div className="form-group">
                    <label htmlFor="retroUsername">
                        Username
                    </label>

                    <input
                        id="retroUsername"
                        value={state.retroUsername}
                        onChange={(e) =>
                            dispatch({
                                type: "input-change",
                                field: "retroUsername",
                                value: e.target.value,
                            })
                        }
                    />
                </div>

                <h4>PlayStation Network</h4>

                <div className="form-group">
                    <label htmlFor="psnApiKey">
                        API Key
                    </label>

                    <input
                        id="psnApiKey"
                        value={state.psnApiKey}
                        onChange={(e) =>
                            dispatch({
                                type: "input-change",
                                field: "psnApiKey",
                                value: e.target.value,
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
                    type="submit"
                    disabled={state.stage === "posting"}
                >
                    {state.stage === "posting"
                        ? "Saving..."
                        : "Save Integrations"}
                </button>
            </form>
        </section>
    );
}