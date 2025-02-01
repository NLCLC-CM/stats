import { handleHighlightSearch } from './search.mjs';

function sortSongs(sortMethod) {
    const container = document.getElementById('songs');

    if (sortMethod === 'title.asc') {
        [...container.children]
            .sort((a, b) => a.dataset.content < b.dataset.content ? -1 : 1)
            .forEach(e => container.append(e));
    } else if (sortMethod === 'title.desc') {
        [...container.children]
            .sort((a, b) => a.dataset.content < b.dataset.content ? 1 : -1)
            .forEach(e => container.append(e));
    } else if (sortMethod === 'hits.desc') {
        [...container.children]
            .sort((a, b) => parseInt(a.dataset.total) < parseInt(b.dataset.total) ? 1 : -1)
            .forEach(e => container.append(e));
    }
}

function onReady() {
    const query = document.getElementById('query');
    const songs = document.getElementsByClassName('song');
    query.addEventListener('input', e => {
        handleHighlightSearch(e, songs);
    });

    query.dispatchEvent(new CustomEvent('input'));

    const sortBtns = document.getElementsByClassName('sort-btns');
    for (const btn of sortBtns) {
        btn.addEventListener('change', () => {
            if (!btn.checked) {
                return;
            }

            const sortMethod = btn.value;
            sortSongs(sortMethod);
        });
    }
}

onReady();
