import { handleHighlightSearch } from './search.mjs';

function onReady() {
    const query = document.getElementById('query');
    const songs = document.getElementsByClassName('song');
    query.addEventListener('input', e => {
        handleHighlightSearch(e, songs);
    });

    query.dispatchEvent(new CustomEvent('input'));
}

onReady();
