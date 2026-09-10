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

async function request(method, path, body) {
    const response = await fetch(BASE_URL + path, {
        method,
        headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
        body: body === undefined ? undefined : JSON.stringify(body),
    });

    if (response.status === 204) {
        return null;
    }

    const isJson = response.headers.get('content-type')?.includes('application/json');
    const payload = isJson ? await response.json() : null;

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
