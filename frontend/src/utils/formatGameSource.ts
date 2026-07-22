import {GameSource} from "../types/game/GameSource.ts";

export function formatGameSource(source: GameSource): string {
    switch (source) {
        case GameSource.STEAM:
            return "Steam";
        case GameSource.RETROACHIEVEMENTS:
            return "RetroAchievements";
        case GameSource.PSN:
            return "PSN";
        default:
            return source;
    }
}