export const errorDescriptions: Record<string, string> = {
    "email-already-in-use":
        "There is already a user with given email address.",
    "insecure-password":
        "Password must have 8 or more characters, a special character, one uppercase character, one lowercase character and a digit.",
    "invalid-request-content":
        "The request content is invalid or malformed. Please check the request body and ensure all required fields are provided with valid values.",
    "user-or-password-are-invalid":
        "The provided email or password is invalid. Please check your credentials and try again.",
    "unknown-error":
        "An unknown error has occurred. Please try again later.",
};

export function getErrorDescription(errorType: string): string {
    return errorDescriptions[errorType] || errorType;
}