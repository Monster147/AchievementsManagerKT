import {GameSource} from "./GameSource.ts";

export interface CreateGameInput {
    externalGameId: string;
    name: string;
    source: GameSource;
    cover?: string;
}