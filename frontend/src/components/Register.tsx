import React, {useReducer} from "react";
import {useNavigate, Link} from "react-router";
import {useAuth} from "../AuthContext";
import {api, ApiError} from "../api";

type RegisterState = {
    name: string;
    email: string;
    password: string;
    error: string | undefined;
    stage: "editing" | "posting" | "succeed" | "failed";
};

type RegisterAction =
    | { type: "input-change"; name: string; email: string; password: string; }
    | { type: "post" }
    | { type: "success" }
    | { type: "error"; message: string };

function reduce(state: RegisterState, action: RegisterAction): RegisterState {
    switch (action.type) {
        case "input-change":
            return {
                ...state,
                name: action.name,
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
                name: "",
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

const initState: RegisterState = {
    name: "",
    email: "",
    password: "",
    error: undefined,
    stage: "editing",
};

export function Register() {
    const [state, dispatch] = useReducer(reduce, initState)
    const navigate = useNavigate();
    const {login} = useAuth();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        dispatch({type: "post"});

        try {
            await api.createUser({
                name: state.name,
                email: state.email,
                password: state.password,
            });
        } catch (e) {
            if (e instanceof ApiError) {
                dispatch({type: "error", message: e.message})
            } else {
                dispatch({
                    type: "error",
                    message: "Ocorreu um erro durante o registo"
                })
            }
            return;
        }

        try {
            const response = await api.createToken({
                email: state.email,
                password: state.password
            });
            await login(response.token);
            dispatch({type: "success"});
            navigate("/");
        } catch (e) {
            if (e instanceof ApiError) {
                dispatch({type: "error", message: e.message})
            } else {
                dispatch({
                    type: "error",
                    message: "Ocorreu um erro ao iniciar sessão"
                })
            }
        }
    }

    return (
        <div className="auth-page">
            <div className="auth-card register-card">
                <div className="auth-header">
                    <h1 className="auth-title">
                        Criar Conta
                    </h1>

                    <p className="auth-description">
                        Preenche os dados para criares
                        a tua conta e começares a
                        acompanhar as tuas conquistas.
                    </p>
                </div>

                <form
                    className="auth-form"
                    onSubmit={handleSubmit}
                >
                    <div className="form-group">
                        <label htmlFor="name">
                            Nome de Utilizador
                        </label>

                        <input
                            id="name"
                            type="text"
                            name="name"
                            value={state.name}
                            onChange={(e) =>
                                dispatch({
                                    type: "input-change",
                                    name: e.target.value,
                                    email: state.email,
                                    password: state.password,
                                })
                            }
                            placeholder="João Silva"
                            required
                            autoComplete="name"
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="email">
                            Endereço de email
                        </label>

                        <input
                            id="email"
                            type="email"
                            name="email"
                            value={state.email}
                            onChange={(e) =>
                                dispatch({
                                    type: "input-change",
                                    name: state.name,
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
                        <label htmlFor="password">
                            Palavra-passe
                        </label>

                        <input
                            id="password"
                            type="password"
                            name="password"
                            value={state.password}
                            onChange={(e) =>
                                dispatch({
                                    type: "input-change",
                                    name: state.name,
                                    email: state.email,
                                    password: e.target.value,
                                })
                            }
                            placeholder="Introduz uma senha forte"
                            required
                            autoComplete="new-password"
                        />
                    </div>

                    {state.error && (
                        <div className="form-error">
                            {state.error}
                        </div>
                    )}

                    <button
                        className="register-button"
                        type="submit"
                        disabled={state.stage === "posting"}
                    >
                        {state.stage === "posting"
                            ? "A criar conta..."
                            : "Criar Conta"}
                    </button>
                </form>

                <div className="auth-footer">
                    <span>
                        Já tens conta?
                    </span>

                    <Link
                        className="login-button"
                        to="/login"
                    >
                        Iniciar sessão
                    </Link>
                </div>
            </div>
        </div>
    );
}

