import {GameSource} from "./GameSource.ts";

export interface SearchedGame {
    id: number;
    externalGameId: string;
    name: string;
    source: GameSource;
    cover?: string;
}