import "./styles/App.css";
import React from "react";
import {createBrowserRouter, RouterProvider} from "react-router";
import {createRoot} from "react-dom/client";
import {AuthProvider} from "./AuthContext.tsx";
import {Home} from "./components/Home.tsx";
import {Login} from "./components/Login.tsx";
import {Register} from "./components/Register.tsx";
import {Layout} from "./components/Layout.tsx";
import {ProtectedRoute} from "./components/ProtectedRoute.tsx";
import {UserProfile} from "./components/UserProfile.tsx";
import {SearchGames} from "./components/SearchGames.tsx";
import {CreateGame} from "./components/CreateGame.tsx";
import {GamesList} from "./components/GamesList.tsx";
import {EditGame} from "./components/EditGame.tsx";

const router = createBrowserRouter([
    {
        element: <Layout />,
        children: [
            {
                path: "/",
                element: <Home />,
            },
            {
                path: "/login",
                element: <Login />
            },
            {
                path: "/register",
                element: <Register />
            },
            {
                path: "/games",
                element: <GamesList />
            },
            {
                path: "/profile",
                element: (
                    <ProtectedRoute>
                        <UserProfile/>
                    </ProtectedRoute>
                ),
            },
            {
                path: "/search-games",
                element: (
                    <ProtectedRoute>
                        <SearchGames/>
                    </ProtectedRoute>
                ),
            },
            {
                path: "/create-game",
                element: (
                    <ProtectedRoute>
                        <CreateGame/>
                    </ProtectedRoute>
                ),
            },
            {
                path: "/games/:id/edit",
                element: (
                    <ProtectedRoute>
                        <EditGame/>
                    </ProtectedRoute>
                ),
            },
        ]
    },
]);

const root = document.getElementById("container")!;

createRoot(root).render(
    <AuthProvider>
        <RouterProvider router={router}/>
    </AuthProvider>
)