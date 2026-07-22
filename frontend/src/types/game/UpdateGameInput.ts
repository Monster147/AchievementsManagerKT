import {GameSource} from "./GameSource.ts";

export interface UpdateGameInput {
    externalGameId: string;
    name: string;
    genres?: string[];
    platform?: string;
    releaseYear?: number;
    source: GameSource;
    cover?: string;
}