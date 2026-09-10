import { clearSession, getAccessToken, getRefreshToken, updateTokens } from './auth.js';

const BASE_URL = '/api';

/**
 * Error carrying the HTTP status and the message produced by the backend, so views can
 * show the very same explanation the API returned (for instance why an artist that still
 * owns LPs cannot be deleted).
 */
export class ApiError extends Error {
    constructor(status, message) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
    }
}

function buildHeaders(body, withAuth) {
    const headers = {};
    if (body !== undefined) {
        headers['Content-Type'] = 'application/json';
    }
    const token = getAccessToken();
    if (withAuth && token) {
        headers.Authorization = `Bearer ${token}`;
    }
    return headers;
}

async function send(method, path, body, withAuth) {
    return fetch(BASE_URL + path, {
        method,
        headers: buildHeaders(body, withAuth),
        body: body === undefined ? undefined : JSON.stringify(body),
    });
}

async function toPayload(response) {
    if (response.status === 204) {
        return null;
    }
    const isJson = response.headers.get('content-type')?.includes('application/json');
    return isJson ? response.json() : null;
}

/**
 * Tries to exchange the refresh token for a new pair. Returns true when the session could
 * be renewed. Concurrent calls share the same in-flight request so a burst of 401s does not
 * fire several refreshes.
 */
let refreshInFlight = null;

async function tryRefresh() {
    const refreshToken = getRefreshToken();
    if (!refreshToken) {
        return false;
    }

    if (!refreshInFlight) {
        refreshInFlight = send('POST', '/auth/refresh', { refreshToken }, false)
            .then(async (response) => {
                if (!response.ok) {
                    return false;
                }
                updateTokens(await response.json());
                return true;
            })
            .catch(() => false)
            .finally(() => {
                refreshInFlight = null;
            });
    }
    return refreshInFlight;
}

async function request(method, path, body, options = {}) {
    const withAuth = options.withAuth !== false;
    let response = await send(method, path, body, withAuth);

    // A single retry after renewing the session: an expired access token should be
    // invisible to the user, but a second 401 means the session is really over.
    if (response.status === 401 && withAuth && !options.isRetry) {
        const renewed = await tryRefresh();
        if (renewed) {
            response = await send(method, path, body, true);
        } else {
            clearSession();
        }
    }

    const payload = await toPayload(response);
    if (!response.ok) {
        throw new ApiError(response.status, payload?.message ?? response.statusText);
    }
    return payload;
}

const query = (params) => {
    const search = new URLSearchParams(
        Object.entries(params).filter(([, value]) => value !== undefined && value !== ''),
    ).toString();
    return search ? `?${search}` : '';
};

export const api = {
    auth: {
        // Login and logout must not carry a stale Authorization header.
        login: (credentials) => request('POST', '/auth/login', credentials, { withAuth: false }),
        logout: (refreshToken) => request('POST', '/auth/logout', { refreshToken }, { withAuth: false }),
        me: () => request('GET', '/auth/me'),
    },
    artists: {
        list: (name) => request('GET', `/artists${query({ name })}`),
        get: (id) => request('GET', `/artists/${id}`),
        lps: (id) => request('GET', `/artists/${id}/lps`),
        create: (artist) => request('POST', '/artists', artist),
        update: (id, artist) => request('PUT', `/artists/${id}`, artist),
        remove: (id) => request('DELETE', `/artists/${id}`),
    },
    lps: {
        list: (artistName) => request('GET', `/lps${query({ artistName })}`),
        get: (id) => request('GET', `/lps/${id}`),
        create: (lp) => request('POST', '/lps', lp),
        update: (id, lp) => request('PUT', `/lps/${id}`, lp),
        remove: (id) => request('DELETE', `/lps/${id}`),
        addSong: (lpId, song) => request('POST', `/lps/${lpId}/songs`, song),
        removeSong: (lpId, songId) => request('DELETE', `/lps/${lpId}/songs/${songId}`),
    },
    authors: {
        list: () => request('GET', '/authors'),
    },
    report: {
        discography: () => request('GET', '/report/discography'),
    },
};
