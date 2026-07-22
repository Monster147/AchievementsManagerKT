import {UserGameStats} from "./UserGameStats.ts";

export type UserStats = {
    totalGames: number;
    gamesWithAchievements: number;
    totalAchievements: number;
    unlockedAchievements: number;
    lockedAchievements: number;
    completionPercentage: number;
    perGameStats: UserGameStats[];
};