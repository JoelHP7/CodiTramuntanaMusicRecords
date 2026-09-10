/**
 * Session state of the frontend.
 *
 * Tokens are kept in localStorage so a reload does not log the user out. That is the usual
 * trade-off for a token based SPA: it is readable by scripts on the same origin, which is
 * acceptable here because the API is same-origin and has no third party scripts.
 */

const ACCESS_TOKEN_KEY = 'discography.accessToken';
const REFRESH_TOKEN_KEY = 'discography.refreshToken';
const USER_KEY = 'discography.user';

const listeners = new Set();

/**
 * Registers a callback fired whenever the session starts or ends, so the header and the
 * current view can react without polling.
 */
export function onSessionChange(listener) {
    listeners.add(listener);
    return () => listeners.delete(listener);
}

function notify() {
    listeners.forEach((listener) => listener(getUser()));
}

export function getAccessToken() {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function getRefreshToken() {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function getUser() {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
}

export function isAuthenticated() {
    return getAccessToken() !== null;
}

/**
 * Stores the payload returned by /api/auth/login or /api/auth/refresh.
 */
export function startSession(authResponse) {
    localStorage.setItem(ACCESS_TOKEN_KEY, authResponse.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, authResponse.refreshToken);
    localStorage.setItem(USER_KEY, JSON.stringify({
        username: authResponse.username,
        role: authResponse.role,
    }));
    notify();
}

/**
 * Updates only the tokens, keeping the current user, after a silent refresh.
 */
export function updateTokens(authResponse) {
    localStorage.setItem(ACCESS_TOKEN_KEY, authResponse.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, authResponse.refreshToken);
}

export function clearSession() {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    notify();
}
