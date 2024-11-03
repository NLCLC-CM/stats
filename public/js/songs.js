function onReady() {
    document.getElementById('query').addEventListener('input', e => {
        const query = e.target.value;
        const songs = document.getElementsByClassName('song');
        for (const song of songs) {
            if (song.textContent.includes(query)) {
                song.style.display = '';
            } else {
                song.style.display = 'none';
            }
        }
    });
}
