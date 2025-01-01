import { handleHighlightSearch } from './search.mjs';

function onReady() {
    const query = document.getElementById('query');
    const people = document.getElementsByClassName('name');
    query.addEventListener('input', e => {
        handleHighlightSearch(e, people);
    });

    query.dispatchEvent(new CustomEvent('input'));
}

onReady();

