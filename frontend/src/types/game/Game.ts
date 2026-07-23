import {GameSource} from "./GameSource.ts";
import {GamePlatform} from "./GamePlatform.ts";
import {GameGenre} from "./GameGenre.ts";

export interface Game {
    id: number;
    externalGameId: string;
    name: string;
    genre?: GameGenre[];
    platform?: GamePlatform;
    releaseYear?: String;
    source: GameSource;
    cover?: string;
}