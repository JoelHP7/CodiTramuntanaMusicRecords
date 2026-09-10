import { route, startRouter } from './router.js';
import { homeView } from './views/home.js';
import { artistDetailView, artistFormView, artistListView } from './views/artists.js';
import { lpDetailView, lpFormView, lpListView } from './views/lps.js';

/**
 * Route table. Order matters: the "new" routes are declared before the ones capturing an
 * identifier, so "#/artists/new" is not read as an artist whose id is "new".
 */
route(/^\/$/, homeView);

route(/^\/artists$/, artistListView);
route(/^\/artists\/new$/, () => artistFormView());
route(/^\/artists\/(\d+)\/edit$/, artistFormView);
route(/^\/artists\/(\d+)$/, artistDetailView);

route(/^\/lps$/, lpListView);
route(/^\/lps\/new$/, () => lpFormView());
route(/^\/lps\/(\d+)\/edit$/, lpFormView);
route(/^\/lps\/(\d+)$/, lpDetailView);

startRouter();
