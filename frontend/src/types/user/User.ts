import {UserRole} from "./UserRole.ts";

export interface User {
    id: number;
    name: string;
    email: string;
    role: UserRole;
}
