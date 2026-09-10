/**
 * Small helpers built on the native <template> element, used as the templating system of
 * the application. No library is involved.
 */

/**
 * Clones the content of a <template> by id.
 */
export function template(id) {
    const node = document.getElementById(id);
    if (!node) {
        throw new Error(`Unknown template: ${id}`);
    }
    return node.content.cloneNode(true);
}

/**
 * Fills a cloned fragment: every key is a CSS selector and every value is either text or
 * a set of attributes/properties to apply.
 *
 * Text is always written through textContent, never innerHTML, so a record named
 * "<script>" is rendered as text instead of being executed.
 */
export function fill(fragment, values) {
    for (const [selector, value] of Object.entries(values)) {
        const element = fragment.querySelector(selector);
        if (!element) {
            continue;
        }
        if (value === null || typeof value !== 'object') {
            element.textContent = value ?? '';
            continue;
        }
        for (const [key, attribute] of Object.entries(value)) {
            if (key === 'text') {
                element.textContent = attribute ?? '';
            } else if (key === 'onClick') {
                element.addEventListener('click', attribute);
            } else {
                element.setAttribute(key, attribute);
            }
        }
    }
    return fragment;
}

/**
 * Replaces the rows of a table body in a single DOM insertion, rendering a placeholder
 * row when there is nothing to show.
 */
export function renderRows(tbody, items, renderItem, emptyMessage, columns) {
    const fragment = document.createDocumentFragment();

    if (items.length === 0) {
        const empty = fill(template('tpl-empty-row'), { '.message': emptyMessage });
        empty.querySelector('td').colSpan = columns;
        fragment.append(empty);
    } else {
        items.forEach((item) => fragment.append(renderItem(item)));
    }

    tbody.replaceChildren(fragment);
}

/**
 * Renders a whole view into the main container.
 */
export function renderView(fragment) {
    document.getElementById('app').replaceChildren(fragment);
}

let toastTimer;

/**
 * Shows a transient message. Errors keep a distinct accent colour.
 */
export function toast(message, variant = 'info') {
    const element = document.getElementById('toast');
    element.textContent = message;
    element.dataset.variant = variant;
    element.hidden = false;

    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => {
        element.hidden = true;
    }, 4000);
}

/**
 * Reads a form as a plain object, trimming every text value.
 */
export function formValues(form) {
    return Object.fromEntries(
        [...new FormData(form).entries()].map(([key, value]) => [key, value.trim()]),
    );
}
