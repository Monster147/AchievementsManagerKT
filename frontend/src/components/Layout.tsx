import { Outlet } from "react-router";
import { Header } from "./Header";

export function Layout() {
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