import { api } from '../api.js';
import { fill, formValues, renderRows, renderView, template, toast } from '../dom.js';
import { navigate } from '../router.js';

/**
 * List of LPs with the "filter by artist name" requirement.
 */
export async function lpListView(params) {
    const artistFilter = params.get('artistName') ?? '';
    renderView(template('tpl-lps'));

    const form = document.querySelector('.filter-bar');
    const input = form.querySelector('#lp-filter');
    input.value = artistFilter;

    form.addEventListener('submit', (event) => {
        event.preventDefault();
        const value = input.value.trim();
        navigate(value ? `#/lps?artistName=${encodeURIComponent(value)}` : '#/lps');
    });
    form.querySelector('.clear-filter').addEventListener('click', () => navigate('#/lps'));

    await refreshLps(artistFilter);
}

async function refreshLps(artistFilter) {
    const lps = await api.lps.list(artistFilter);

    renderRows(
        document.querySelector('.lp-rows'),
        lps,
        (lp) => fill(template('tpl-lp-row'), {
            '.name-link': { text: lp.name, href: `#/lps/${lp.id}` },
            '.artist-link': { text: lp.artistName, href: `#/artists/${lp.artistId}` },
            '.description': lp.description || '-',
            '.edit-link': { href: `#/lps/${lp.id}/edit` },
            '.delete-button': { onClick: () => deleteLp(lp, artistFilter) },
        }),
        artistFilter ? `No LP belongs to an artist matching "${artistFilter}".` : 'No LPs yet.',
        4,
    );
}

async function deleteLp(lp, artistFilter) {
    if (!window.confirm(`Delete "${lp.name}" and its songs?`)) {
        return;
    }
    try {
        await api.lps.remove(lp.id);
        toast(`LP "${lp.name}" deleted.`);
        await refreshLps(artistFilter);
    } catch (error) {
        toast(error.message, 'error');
    }
}

/**
 * Detail of an LP, where its songs and their authors are managed.
 */
export async function lpDetailView(id) {
    const [lp, authors] = await Promise.all([api.lps.get(id), api.authors.list()]);

    const view = fill(template('tpl-lp-detail'), {
        '.lp-name': lp.name,
        '.artist-link': { text: lp.artistName, href: `#/artists/${lp.artistId}` },
        '.description': lp.description || 'No description.',
        '.edit-link': { href: `#/lps/${lp.id}/edit` },
    });
    renderView(view);

    // Feeds the datalist so existing authors get reused instead of retyped.
    const datalist = document.querySelector('#author-options');
    datalist.replaceChildren(...authors.map((author) => {
        const option = document.createElement('option');
        option.value = author.name;
        return option;
    }));

    renderSongs(lp);

    document.querySelector('.add-song-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        const values = formValues(event.target);
        const authorNames = values.authorNames
            .split(',')
            .map((name) => name.trim())
            .filter(Boolean);

        if (authorNames.length === 0) {
            toast('A song needs at least one author.', 'error');
            return;
        }

        try {
            await api.lps.addSong(id, { name: values.name, authorNames });
            toast(`Song "${values.name}" added.`);
            await lpDetailView(id);
        } catch (error) {
            toast(error.message, 'error');
        }
    });
}

function renderSongs(lp) {
    renderRows(
        document.querySelector('.song-rows'),
        lp.songs,
        (song) => fill(template('tpl-song-row'), {
            '.name': song.name,
            '.authors': song.authors.join(', '),
            '.delete-button': { onClick: () => deleteSong(lp, song) },
        }),
        'This LP has no songs yet.',
        3,
    );
}

async function deleteSong(lp, song) {
    if (!window.confirm(`Delete the song "${song.name}"?`)) {
        return;
    }
    try {
        await api.lps.removeSong(lp.id, song.id);
        toast(`Song "${song.name}" deleted.`);
        await lpDetailView(lp.id);
    } catch (error) {
        toast(error.message, 'error');
    }
}

/**
 * Create and edit form for LPs.
 */
export async function lpFormView(id) {
    const editing = id !== undefined;
    const [lp, artists] = await Promise.all([
        editing ? api.lps.get(id) : Promise.resolve(null),
        api.artists.list(),
    ]);

    const view = fill(template('tpl-lp-form'), {
        '.form-title': editing ? `Edit ${lp.name}` : 'New LP',
        '.cancel-link': { href: editing ? `#/lps/${id}` : '#/lps' },
    });
    renderView(view);

    const form = document.querySelector('.form');
    const select = form.querySelector('#lp-artist');

    if (artists.length === 0) {
        toast('Create an artist first: every LP belongs to one.', 'error');
    }

    select.replaceChildren(...artists.map((artist) => {
        const option = document.createElement('option');
        option.value = artist.id;
        option.textContent = artist.name;
        return option;
    }));

    if (editing) {
        form.querySelector('#lp-name').value = lp.name;
        form.querySelector('#lp-description').value = lp.description ?? '';
        select.value = lp.artistId;
    }

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const values = formValues(form);
        const payload = {
            name: values.name,
            description: values.description,
            artistId: Number(values.artistId),
        };
        try {
            const saved = editing ? await api.lps.update(id, payload) : await api.lps.create(payload);
            toast(`LP "${saved.name}" saved.`);
            navigate(`#/lps/${saved.id}`);
        } catch (error) {
            toast(error.message, 'error');
        }
    });
}
