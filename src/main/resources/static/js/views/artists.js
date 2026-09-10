import { api, ApiError } from '../api.js';
import { fill, formValues, renderRows, renderView, template, toast } from '../dom.js';
import { navigate } from '../router.js';

/**
 * List of artists, with a name filter and the create/edit/delete actions.
 */
export async function artistListView(params) {
    const nameFilter = params.get('name') ?? '';
    renderView(template('tpl-artists'));

    const form = document.querySelector('.filter-bar');
    const input = form.querySelector('#artist-filter');
    input.value = nameFilter;

    form.addEventListener('submit', (event) => {
        event.preventDefault();
        const value = input.value.trim();
        navigate(value ? `#/artists?name=${encodeURIComponent(value)}` : '#/artists');
    });
    form.querySelector('.clear-filter').addEventListener('click', () => navigate('#/artists'));

    await refreshArtists(nameFilter);
}

async function refreshArtists(nameFilter) {
    const artists = await api.artists.list(nameFilter);

    renderRows(
        document.querySelector('.artist-rows'),
        artists,
        (artist) => fill(template('tpl-artist-row'), {
            '.name-link': { text: artist.name, href: `#/artists/${artist.id}` },
            '.description': artist.description || '-',
            '.edit-link': { href: `#/artists/${artist.id}/edit` },
            '.delete-button': { onClick: () => deleteArtist(artist, nameFilter) },
        }),
        nameFilter ? `No artist matches "${nameFilter}".` : 'No artists yet.',
        3,
    );
}

async function deleteArtist(artist, nameFilter) {
    if (!window.confirm(`Delete "${artist.name}"?`)) {
        return;
    }
    try {
        await api.artists.remove(artist.id);
        toast(`Artist "${artist.name}" deleted.`);
        await refreshArtists(nameFilter);
    } catch (error) {
        // A 409 here means the artist still owns LPs; the backend explains why.
        toast(error.message, 'error');
    }
}

/**
 * Detail of an artist: description, total number of LPs and the list of those LPs.
 */
export async function artistDetailView(id) {
    const [artist, lps] = await Promise.all([api.artists.get(id), api.artists.lps(id)]);

    const view = fill(template('tpl-artist-detail'), {
        '.artist-name': artist.name,
        '.description': artist.description || 'No description.',
        '.lp-count': artist.lpCount,
        '.edit-link': { href: `#/artists/${artist.id}/edit` },
        // Reuses the LP list filtered by this artist instead of duplicating the view.
        '.lps-link': { href: `#/lps?artistName=${encodeURIComponent(artist.name)}` },
    });
    renderView(view);

    renderRows(
        document.querySelector('.lp-rows'),
        lps,
        (lp) => fill(template('tpl-artist-lp-row'), {
            '.lp-link': { text: lp.name, href: `#/lps/${lp.id}` },
            '.description': lp.description || '-',
        }),
        'This artist has no LPs yet.',
        2,
    );
}

/**
 * Create and edit form. The same view serves both cases.
 */
export async function artistFormView(id) {
    const editing = id !== undefined;
    const artist = editing ? await api.artists.get(id) : null;

    const view = fill(template('tpl-artist-form'), {
        '.form-title': editing ? `Edit ${artist.name}` : 'New artist',
        '.cancel-link': { href: editing ? `#/artists/${id}` : '#/artists' },
    });
    renderView(view);

    const form = document.querySelector('.form');
    if (editing) {
        form.querySelector('#artist-name').value = artist.name;
        form.querySelector('#artist-description').value = artist.description ?? '';
    }

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const payload = formValues(form);
        try {
            const saved = editing
                ? await api.artists.update(id, payload)
                : await api.artists.create(payload);
            toast(`Artist "${saved.name}" saved.`);
            navigate(`#/artists/${saved.id}`);
        } catch (error) {
            toast(error.message, 'error');
        }
    });
}
