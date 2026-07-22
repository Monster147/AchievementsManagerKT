import {GameSource} from "../game/GameSource.ts";

export interface GetAchievementInput {
    internalGameId: number;
    externalGameId: string;
    source: GameSource;
}