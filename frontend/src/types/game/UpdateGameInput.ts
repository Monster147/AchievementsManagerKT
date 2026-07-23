import {GameSource} from "./GameSource.ts";
import {GameGenre} from "./GameGenre.ts";
import {GamePlatform} from "./GamePlatform.ts";

export interface UpdateGameInput {
    externalGameId: string;
    name: string;
    genres?: GameGenre[];
    platform?: GamePlatform;
    releaseYear?: String;
    source: GameSource;
    cover?: string;
}