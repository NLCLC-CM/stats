/**
 * Handle the `input` event of simple text-searches and highlights them.
 *
 * Assumes that everything is case-insensitive.
 *
 * @param {Event} e
 * @param {HTMLCollectionOf<Element>} thingsToSearchThrough
 */
export function handleHighlightSearch(e, thingsToSearchThrough) {
    /** @var {string} query */
    const query = e.target.value.toLowerCase();
    for (const element of thingsToSearchThrough) {
        const content = element.getElementsByClassName('content')[0];
        const contentLowerCase = content.textContent.toLowerCase();
        if (query.length > 0 && contentLowerCase.includes(query)) {
            element.style.display = 'block';
            // doesn't matter because we display everything in uppercase anyways
            content.innerHTML = contentLowerCase.replace(query, `<span class="text-warning">${query}</span>`);
        } else if (query.length === 0) {
            element.style.display = 'block';
            content.textContent = element.dataset.content;
        } else {
            element.style.display = 'none';
            content.textContent = element.dataset.content;
        }
    }
}
