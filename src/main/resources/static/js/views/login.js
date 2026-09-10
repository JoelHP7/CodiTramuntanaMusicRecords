import { api } from '../api.js';
import { startSession } from '../auth.js';
import { fill, formValues, renderView, template, toast } from '../dom.js';
import { navigate } from '../router.js';

/**
 * Login screen. After a successful login the user is sent back to wherever they were trying
 * to go, which is passed in the hash as ?redirect=.
 */
export async function loginView(params) {
    const redirect = params.get('redirect') ?? '#/';

    renderView(fill(template('tpl-login'), {
        '.cancel-link': { href: '#/' },
    }));

    const form = document.querySelector('.login-form');
    form.querySelector('#login-username').focus();

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const credentials = formValues(form);

        try {
            const session = await api.auth.login(credentials);
            startSession(session);
            toast(`Signed in as ${session.username}.`);
            navigate(redirect);
        } catch (error) {
            // 401 here is a wrong password, not a broken session: stay on the form.
            toast(error.message, 'error');
            form.querySelector('#login-password').value = '';
            form.querySelector('#login-password').focus();
        }
    });
}
