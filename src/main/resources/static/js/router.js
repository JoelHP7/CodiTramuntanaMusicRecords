import { renderView, template, fill } from './dom.js';
import { isAuthenticated } from './auth.js';

/**
 * Minimal hash based router.
 *
 * Hash routing is used on purpose: with the History API, reloading a deep link such as
 * /lps/3 would ask the server for that path and Spring would answer 404 unless a
 * forwarding controller were added. With hashes the browser always requests "/", so deep
 * links and page reloads work with no server side configuration at all.
 */
const routes = [];

/**
 * Registers a route. Pass `{ requiresAuth: true }` for the screens that can only submit
 * their work with a session, so the user is sent to the login form instead of filling in a
 * whole form and hitting a 401 on save.
 */
export function route(pattern, view, options = {}) {
    routes.push({ pattern, view, requiresAuth: options.requiresAuth === true });
}

function parseHash() {
    const hash = window.location.hash || '#/';
    const [path, search] = hash.slice(1).split('?');
    return { path: path || '/', params: new URLSearchParams(search ?? '') };
}

function match(path) {
    for (const { pattern, view, requiresAuth } of routes) {
        const result = pattern.exec(path);
        if (result) {
            return { view, requiresAuth, args: result.slice(1) };
        }
    }
    return null;
}

function highlightActiveLink(path) {
    const current = `#${path}`;
    document.querySelectorAll('.main-nav a[data-route]').forEach((link) => {
        const target = link.dataset.route;
        const active = target === '#/'
            ? current === '#/'
            : current.startsWith(target);
        if (active) {
            link.setAttribute('aria-current', 'page');
        } else {
            link.removeAttribute('aria-current');
        }
    });
}

function showMessage(title, detail) {
    renderView(fill(template('tpl-message'), { '.title': title, '.detail': detail }));
}

async function resolve() {
    const { path, params } = parseHash();
    highlightActiveLink(path);

    const matched = match(path);
    if (!matched) {
        showMessage('Page not found', `There is nothing at ${path}.`);
        return;
    }

    if (matched.requiresAuth && !isAuthenticated()) {
        navigate(`#/login?redirect=${encodeURIComponent('#' + path)}`);
        return;
    }

    try {
        await matched.view(...matched.args, params);
    } catch (error) {
        showMessage('Something went wrong', error.message);
    }
    window.scrollTo({ top: 0 });
}

export function navigate(hash) {
    if (window.location.hash === hash) {
        resolve();
    } else {
        window.location.hash = hash;
    }
}

export function startRouter() {
    window.addEventListener('hashchange', resolve);
    resolve();
}
