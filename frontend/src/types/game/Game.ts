import {GameSource} from "./GameSource.ts";

export interface Game {
    id: number;
    externalGameId: string;
    name: string;
    genres?: string[];
    platform?: string;
    releaseYear?: number;
    source: GameSource;
    cover?: string;
}