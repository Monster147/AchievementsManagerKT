import { useReducer } from "react";
import { useAuth } from "../AuthContext.tsx";
import { Link, useNavigate } from "react-router";
import { api, ApiError } from "../api.ts";

type LoginState = {
    email: string;
    password: string;
    error: string | undefined;
    stage: "editing" | "posting" | "succeed" | "failed";
};

type LoginAction =
    | { type: "input-change"; email: string; password: string }
    | { type: "post" }
    | { type: "success" }
    | { type: "error"; message: string };

function reduce(state: LoginState, action: LoginAction): LoginState {
    switch (action.type) {
        case "input-change":
            return {
                ...state,
                email: action.email,
                password: action.password,
            };

        case "post":
            return {
                ...state,
                stage: "posting",
                error: undefined,
            };

        case "success":
            return {
                email: "",
                password: "",
                error: undefined,
                stage: "succeed",
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

const initState: LoginState = {
    email: "",
    password: "",
    error: undefined,
    stage: "editing",
};

export function Login() {
    const [state, dispatch] = useReducer(reduce, initState);

    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        dispatch({ type: "post" });

        try {
            const response = await api.createToken({
                email: state.email,
                password: state.password,
            });

            console.log(response);

            await login(response.token);

            dispatch({ type: "success" });
            navigate("/");
        } catch (err) {
            if (err instanceof ApiError) {
                dispatch({ type: "error", message: err.message });
            } else {
                dispatch({
                    type: "error",
                    message: "Ocorreu um erro ao iniciar sessão.",
                });
            }
        }
    };

    return (
        <div className="auth-page">
            <div className="auth-card">
                <div className="auth-header">
                    <h1 className="auth-title">Iniciar Sessão</h1>

                    <p className="auth-description">
                        Entra na tua conta para acederes à tua biblioteca, progresso e conquistas.
                    </p>
                </div>

                <form className="auth-form" onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="email">Endereço de email</label>

                        <input
                            id="email"
                            type="email"
                            name="email"
                            value={state.email}
                            onChange={(e) =>
                                dispatch({
                                    type: "input-change",
                                    email: e.target.value,
                                    password: state.password,
                                })
                            }
                            placeholder="nome@exemplo.com"
                            required
                            autoComplete="email"
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">Palavra-passe</label>

                        <input
                            id="password"
                            type="password"
                            name="password"
                            value={state.password}
                            onChange={(e) =>
                                dispatch({
                                    type: "input-change",
                                    email: state.email,
                                    password: e.target.value,
                                })
                            }
                            placeholder="Introduz a tua palavra-passe"
                            required
                            autoComplete="current-password"
                        />
                    </div>

                    {state.error && (
                        <div className="form-error">
                            {state.error}
                        </div>
                    )}

                    <button
                        className="login-button"
                        type="submit"
                        disabled={state.stage === "posting"}
                    >
                        {state.stage === "posting"
                            ? "A iniciar sessão..."
                            : "Entrar"}
                    </button>
                </form>

                <div className="auth-footer">
                    <span>Ainda não tens conta?</span>

                    <Link className="register-button" to="/register">
                        Criar conta
                    </Link>
                </div>
            </div>
        </div>
    );
}