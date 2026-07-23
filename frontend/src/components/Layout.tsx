import { Outlet } from "react-router";
import { Header } from "./Header";
import {LoadingScreen} from "./LoadingScreen.tsx";
import {useAuth} from "../AuthContext.tsx";

export function Layout() {
    const { isLoading } = useAuth();

    if (isLoading) {
        return <LoadingScreen />;
    }

    return (
        <div className="page">
            <div className="layout">
                <Header />
                <main className="content">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}