import { api } from '../api.js';
import { fill, renderRows, renderView, template } from '../dom.js';

/**
 * Home page: the discography report plus the shortcuts to manage artists and LPs.
 */
export async function homeView() {
    const view = template('tpl-home');
    const tbody = view.querySelector('.report-rows');
    renderView(view);

    const report = await api.report.discography();

    renderRows(
        document.querySelector('.report-rows') ?? tbody,
        report,
        (row) => fill(template('tpl-report-row'), {
            '.lp-link': { text: row.lpName, href: `#/lps/${row.lpId}` },
            '.artist': row.artistName,
            '.songs': row.songCount,
            // The API returns the authors as a list; joining them is a presentation concern.
            '.authors': row.authors.join(', ') || '-',
        }),
        'No LPs yet. Create an artist and then an LP to see the report.',
        4,
    );
}
