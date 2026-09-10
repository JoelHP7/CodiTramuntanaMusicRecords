import { api } from './api.js';
import { clearSession, getRefreshToken, getUser, onSessionChange } from './auth.js';
import { fill, template, toast } from './dom.js';
import { route, startRouter } from './router.js';
import { homeView } from './views/home.js';
import { artistDetailView, artistFormView, artistListView } from './views/artists.js';
import { lpDetailView, lpFormView, lpListView } from './views/lps.js';
import { loginView } from './views/login.js';

/**
 * Route table. Order matters: the "new" routes are declared before the ones capturing an
 * identifier, so "#/artists/new" is not read as an artist whose id is "new".
 */
route(/^\/$/, homeView);

route(/^\/login$/, loginView);

route(/^\/artists$/, artistListView);
route(/^\/artists\/new$/, () => artistFormView(), { requiresAuth: true });
route(/^\/artists\/(\d+)\/edit$/, artistFormView, { requiresAuth: true });
route(/^\/artists\/(\d+)$/, artistDetailView);

route(/^\/lps$/, lpListView);
route(/^\/lps\/new$/, () => lpFormView(), { requiresAuth: true });
route(/^\/lps\/(\d+)\/edit$/, lpFormView, { requiresAuth: true });
route(/^\/lps\/(\d+)$/, lpDetailView);

/**
 * Renders the session block of the header. It lives outside the container the router
 * repaints, so it is redrawn on session changes instead.
 */
function renderSessionBox() {
    const box = document.getElementById('session-box');
    const user = getUser();

    // Drives the visibility of every .auth-only control through CSS, so views do not each
    // have to know about the session.
    document.body.classList.toggle('authenticated', user !== null);

    if (!user) {
        box.replaceChildren(template('tpl-session-anonymous'));
        return;
    }

    const fragment = fill(template('tpl-session-active'), {
        '.session-user': `${user.username} (${user.role.toLowerCase()})`,
        '.logout-button': { onClick: signOut },
    });
    box.replaceChildren(fragment);
}

async function signOut() {
    const refreshToken = getRefreshToken();
    try {
        if (refreshToken) {
            await api.auth.logout(refreshToken);
        }
    } catch {
        // Revoking server side is best effort: the local session is dropped either way.
    }
    clearSession();
    toast('Signed out.');
}

onSessionChange(renderSessionBox);
renderSessionBox();

startRouter();
