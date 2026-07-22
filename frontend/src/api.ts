import {getErrorDescription} from "./errorDescriptions";
import {UserInput} from "./types/user/UserInput.ts";
import {UserLoginInputModel} from "./types/user/UserLoginInputModel.ts";
import {UserLoginOutputModel} from "./types/user/UserLoginOutputModel.ts";
import {UserHomeOutputModel} from "./types/user/UserHomeOutputModel.ts";
import {UpdateGameInput} from "./types/game/UpdateGameInput.ts";
import {Game} from "./types/game/Game.ts";
import {GameSource} from "./types/game/GameSource.ts";
import {CreateGameInput} from "./types/game/CreateGameInput.ts";
import {CreateAchievementInput} from "./types/achievement/CreateAchievementInput.ts";
import {Achievement} from "./types/achievement/Achievement.ts";
import {GetAchievementInput} from "./types/achievement/GetAchievementInput.ts";
import {ClearCompletedAchievements} from "./types/gameProgress/ClearCompletedAchievements.ts";
import {GameProgress} from "./types/gameProgress/GameProgress.ts";
import {CompletedAchievementInput} from "./types/gameProgress/CompletedAchievementInput.ts";
import {CreateGameProgressInput} from "./types/gameProgress/CreateGameProgressInput.ts";
import {SearchedGame} from "./types/game/SearchedGame.ts";
import {SearchGameRequest} from "./types/search/SearchGameRequest.ts";
import {IntegrationsConfig} from "./types/config/IntegrationsConfig.ts";
import {UpdateIntegrationsConfigInput} from "./types/config/UpdatedIntegrationsConfigInput.ts";
import {UserGameInput} from "./types/library/UserGameInput.ts";
import {UserGame} from "./types/library/UserGame.ts";

const API_BASE_URL = "/api";

class ApiError extends Error {
    constructor(public status: number, message: string) {
        super(message);
    }
}

export function getAuthHeaders(): HeadersInit {
    const token = localStorage.getItem("token");
    return token ? {Authorization: `Bearer ${token}`} : {};
}

export async function fetchApi<T>(
    endpoint: string,
    options: RequestInit = {}
): Promise<T> {
    await delay(1000)
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...options.headers,
        },
    });
    if (!response.ok) {
        const error = await response
            .json()
            .catch(() => ({title: "Unknown error"}));
        const errorMessage = error.title
            ? getErrorDescription(error.title)
            : response.statusText;
        throw new ApiError(response.status, errorMessage);
    }

    if (response.status === 204) {
        return undefined as T;
    }
    const text = await response.text();

    if (!text) {
        return undefined as T;
    }

    return JSON.parse(text);

}

export const api = {

    // Users
    async createUser(input: UserInput): Promise<string> {
        const response = await fetch(`${API_BASE_URL}/users`, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(input),
        });

        if (!response.ok) {
            const error = await response
                .json()
                .catch(() => ({title: "Unknown error"}));
            const errorMessage = error.title
                ? getErrorDescription(error.title)
                : response.statusText;
            throw new ApiError(response.status, errorMessage);
        }

        return response.headers.get("Location") || "";
    },

    async createToken(
        input: UserLoginInputModel
    ): Promise<UserLoginOutputModel> {
        return fetchApi<UserLoginOutputModel>(`/users/login`, {
            method: "POST",
            body: JSON.stringify(input),
        });
    },

    async logout(): Promise<void> {
        return fetchApi<void>("/users/logout", {
            method: "POST",
            headers: getAuthHeaders(),
        });
    },

    async getMe(): Promise<UserHomeOutputModel> {
        return fetchApi<UserHomeOutputModel>("/users/me", {
            headers: getAuthHeaders(),
        });
    },

    // Games

    async createGame(input: CreateGameInput): Promise<Game> {
        return fetchApi<Game>("/games", {
            method: "POST",
            headers: getAuthHeaders(),
            body: JSON.stringify(input),
        });
    },

    async getGames(): Promise<Game[]> {
        return fetchApi<Game[]>("/games");
    },

    async getGameById(gameId: number): Promise<Game> {
        return fetchApi<Game>(`/games/${gameId}`);
    },

    async getGameByExternalId(
        externalId: string,
        source: GameSource
    ): Promise<Game> {
        return fetchApi<Game>(
            `/games/externalId/${externalId}/source/${source}`
        );
    },

    async updateGame(
        gameId: number,
        input: UpdateGameInput
    ): Promise<Game> {
        return fetchApi<Game>(`/games/update/${gameId}`, {
            method: "POST",
            headers: getAuthHeaders(),
            body: JSON.stringify(input),
        });
    },

    async deleteGame(gameId: number): Promise<void> {
        return fetchApi<void>(`/games/${gameId}`, {
            method: "DELETE",
            headers: getAuthHeaders(),
        });
    },

    // Achievements

    async createAchievement(
        input: CreateAchievementInput
    ): Promise<void> {
        return fetchApi<void>("/achievements", {
            method: "POST",
            headers: getAuthHeaders(),
            body: JSON.stringify(input),
        });
    },

    async getAchievementsByGameId(
        gameId: number
    ): Promise<Achievement[]> {
        return fetchApi<Achievement[]>(
            `/achievements/${gameId}`
        );
    },

    async deleteAchievements(gameId: number): Promise<void> {
        return fetchApi<void>(`/achievements/${gameId}`, {
            method: "DELETE",
            headers: getAuthHeaders(),
        });
    },

    async fetchAchievements(
        input: GetAchievementInput
    ): Promise<void> {
        return fetchApi<void>("/getAchievements", {
            method: "POST",
            headers: getAuthHeaders(),
            body: JSON.stringify(input),
        });
    },

    // Progress

    async createProgress(
        input: CreateGameProgressInput
    ): Promise<GameProgress> {
        return fetchApi<GameProgress>(
            `/progress/${input.gameId}`,
            {
                method: "POST",
                headers: getAuthHeaders(),
                body: JSON.stringify(input),
            }
        );
    },

    async getProgress(
        gameId: number,
        userId: number
    ): Promise<GameProgress> {
        return fetchApi<GameProgress>(
            `/progress/game/${gameId}/user/${userId}`
        );
    },

    async getUserProgress(
        userId: number
    ): Promise<GameProgress[]> {
        return fetchApi<GameProgress[]>(
            `/progress/user/${userId}`
        );
    },

    async addCompletedAchievement(
        input: CompletedAchievementInput
    ): Promise<GameProgress> {
        return fetchApi<GameProgress>(
            "/progress/achievement",
            {
                method: "POST",
                body: JSON.stringify(input),
            }
        );
    },

    async removeCompletedAchievement(
        input: CompletedAchievementInput
    ): Promise<GameProgress> {
        return fetchApi<GameProgress>(
            "/progress/achievement",
            {
                method: "DELETE",
                body: JSON.stringify(input),
            }
        );
    },

    async clearCompletedAchievements(
        input: ClearCompletedAchievements
    ): Promise<GameProgress> {
        return fetchApi<GameProgress>(
            "/progress",
            {
                method: "DELETE",
                body: JSON.stringify(input),
            }
        );
    },

    // Search

    async searchGames(
        input: SearchGameRequest
    ): Promise<SearchedGame[]> {
        return fetchApi<SearchedGame[]>(
            "/searchGames",
            {
                method: "POST",
                headers: getAuthHeaders(),
                body: JSON.stringify(input),
            }
        );
    },

    async getSearchCache(): Promise<SearchedGame[]> {
        return fetchApi<SearchedGame[]>(
            "/searchGames/cache",
            {
                method: "POST",
            }
        );
    },

    async addGameFromCache(
        cacheGameId: number
    ): Promise<Game> {
        return fetchApi<Game>(
            `/searchGames/addGame/${cacheGameId}`,
            {
                method: "POST",
                headers: getAuthHeaders(),
            }
        );
    },

    // Configs

    async getConfigs(): Promise<IntegrationsConfig> {
        return fetchApi<IntegrationsConfig>(
            "/configs",
            {
                headers: getAuthHeaders(),
            }
        );
    },

    async updateConfigs(
        input: UpdateIntegrationsConfigInput
    ): Promise<IntegrationsConfig> {
        return fetchApi<IntegrationsConfig>(
            "/configs",
            {
                method: "POST",
                headers: getAuthHeaders(),
                body: JSON.stringify(input),
            }
        );
    },

    // Library

    async addGameToLibrary(
        input: UserGameInput
    ): Promise<UserGame> {
        return fetchApi<UserGame>(
            "/library",
            {
                method: "POST",
                headers: getAuthHeaders(),
                body: JSON.stringify(input),
            }
        );
    },

    async getUserLibrary(
        userId: number
    ): Promise<UserGame[]> {
        return fetchApi<UserGame[]>(
            `/library/${userId}`
        );
    },

    async getUserGame(
        userId: number,
        gameId: number
    ): Promise<UserGame> {
        return fetchApi<UserGame>(
            `/library/user/${userId}/game/${gameId}`
        );
    },

    async toggleSync(
        gameId: number
    ): Promise<UserGame> {
        return fetchApi<UserGame>(
            `/library/user/game/${gameId}/updateSync`,
            {
                method: "POST",
                headers: getAuthHeaders(),
            }
        );
    },

    async removeAllGames(): Promise<void> {
        return fetchApi<void>(
            "/library/user",
            {
                method: "DELETE",
                headers: getAuthHeaders(),
            }
        );
    },

    async removeGame(
        gameId: number
    ): Promise<void> {
        return fetchApi<void>(
            `/library/user/game/${gameId}`,
            {
                method: "DELETE",
                headers: getAuthHeaders(),
            }
        );
    },
}

export {ApiError};

export function delay(delayInMs: number) {
    return new Promise((resolve) => {
        setTimeout(() => resolve(undefined), delayInMs);
    });
}