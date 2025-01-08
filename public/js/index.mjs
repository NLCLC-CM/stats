function onReady() {
    const years = document.getElementsByClassName('year');
    for (const year of years) {
        const prev = year.getElementsByClassName('prev')[0];
        const next = year.getElementsByClassName('next')[0];
        prev.addEventListener('click', () => {
            if (year.previousElementSibling) {
                year.style.display = 'none';
                year.previousElementSibling.style.display = '';
            }
        });
        next.addEventListener('click', () => {
            if (year.nextElementSibling) {
                year.style.display = 'none';
                year.nextElementSibling.style.display = '';
            }
        });
    }
}

onReady();
