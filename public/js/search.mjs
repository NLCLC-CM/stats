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
        const contentLowerCase = element.textContent.toLowerCase();
        if (contentLowerCase.includes(query)) {
            element.style.display = '';
            // doesn't matter because we display everything in uppercase anyways
            element.innerHTML = contentLowerCase.replace(query, `<span class="text-warning">${query}</span>`);
        } else {
            element.style.display = 'none';
            // probably effectively delete the text styling
            element.innerHTML = element.textContent;
        }
    }
}
