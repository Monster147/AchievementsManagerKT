import "./styles/App.css";
import React from "react";
import {createBrowserRouter, RouterProvider} from "react-router";
import {createRoot} from "react-dom/client";
import {AuthProvider} from "./AuthContext.tsx";
import {Home} from "./components/Home.tsx";
import {Login} from "./components/Login.tsx";
import {Register} from "./components/Register.tsx";
import {Layout} from "./components/Layout.tsx";

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
        ]
    },
]);

const root = document.getElementById("container")!;

createRoot(root).render(
    <AuthProvider>
        <RouterProvider router={router}/>
    </AuthProvider>
)