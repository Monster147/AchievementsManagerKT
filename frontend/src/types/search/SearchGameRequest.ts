import {GameSource} from "../game/GameSource.ts";

export interface SearchGameRequest {
    gameName: string;
    source: GameSource;
}