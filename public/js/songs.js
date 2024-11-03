function onReady() {
    const query = document.getElementById('query');
    query.addEventListener('input', e => {
        const query = e.target.value;
        const songs = document.getElementsByClassName('song');
        for (const song of songs) {
            if (song.textContent.toLowerCase().includes(query.toLowerCase())) {
                song.style.display = '';
                // doesn't matter because we display everything in uppercase anyways
                song.innerHTML = song.textContent.toLowerCase().replace(query.toLowerCase(), `<span class="text-warning">${query.toLowerCase()}</span>`);
            } else {
                song.style.display = 'none';
                // probably effectively delete the text styling
                song.innerHTML = song.textContent;
            }
        }
    });

    query.dispatchEvent(new CustomEvent('input'));
}

onReady();
