function getBaseUrl() {
    const href = window.location.href;
    if (href.endsWith('index.html')) {
        return href.substring(0, href.length - 'index.html'.length);
    } else {
        return href;
    }
}

const baseUrl = getBaseUrl();

function onReady() {
    const years = document.getElementsByClassName('year');
    for (const year of years) {
        const prev = year.getElementsByClassName('prev')[0];
        const currentYear = year.getElementsByClassName('year-selector')[0];
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
        currentYear.addEventListener('change', () => {
            year.style.display = 'none';
            document.querySelector(`.year[data-year="${currentYear.value}"]`).style.display = '';
            // reset everything for when we come back to it
            currentYear.value = currentYear.dataset.year;
        });
    }

    const detailsBtns = document.getElementsByClassName('details');
    for (const detailBtn of detailsBtns) {
        detailBtn.addEventListener('click', e => {
            const parentContainer = e.target.parentElement;
            const songElements = parentContainer.getElementsByClassName('song');
            const peopleElements = parentContainer.getElementsByClassName('entry-role');
            const sermonNameElement = parentContainer.querySelector('.lecture-name');
            const timeElement = parentContainer.querySelector('time');

            const songs = [];
            const participants = {};
            for (const songElement of songElements) {
                songs.push(songElement.textContent);
            }
            for (const peopleElement of peopleElements) {
                const roleElements = peopleElement.getElementsByClassName('role');
                if (roleElements.length !== 1) {
                    continue;
                }

                const personElements = peopleElement.getElementsByClassName('person');
                const people = [];
                for (const personElement of personElements) {
                    people.push(personElement.textContent);
                }

                participants[roleElements[0].textContent] = people;
            }

            populateDetailsDialog(timeElement.dateTime, sermonNameElement.textContent, songs, participants);
        });
    }
}

function populateDetailsDialog(time, sermonTitle, songs, participants) {
    const detailsDialog = document.getElementById('details-box');
    const selectedTime = document.getElementById('selected-time');
    const sermonTitleTD = document.getElementById('selected-sermon-title');
    const participantsTD = document.getElementById('selected-participants');
    const songsTD = document.getElementById('selected-songs');

    selectedTime.datetime = time;
    selectedTime.textContent = time;
    sermonTitleTD.textContent = sermonTitle;
    participantsTD.replaceChildren(...toParticipantsElements(participants));
    songsTD.replaceChildren(...toSongsElements(songs));

    detailsDialog.showModal();
}

function toSongsElements(songs) {
    const elements = [];

    for (const song of songs) {
        const songElement = document.createElement('a');
        songElement.href = `${baseUrl}songs/${song}.html`;
        songElement.textContent = song;
        elements.push(songElement);
    }

    return elements;
}

/**
 * @param {array<string, string[]>} participants
 */
function toParticipantsElements(participants) {
    const keys = Object.keys(participants);
    keys.sort();

    const elements = [];

    for (const role of keys) {
        const container = document.createElement('div');
        container.classList.add('row');
        const roleContainer = document.createElement('span');
        roleContainer.classList.add('col-6');
        roleContainer.textContent = role;
        const peopleContainer = document.createElement('div');
        peopleContainer.classList.add('col-6');
        peopleContainer.append(...toRolePeopleElements(participants[role]));

        container.append(roleContainer, peopleContainer);
        elements.push(container);
    }

    return elements;
}

function toRolePeopleElements(people) {
    const elements = [];

    for (const person of people) {
        const link = document.createElement('a');
        link.classList.add('row');
        link.textContent = person;
        link.href = `${baseUrl}people/${person}.html`;
        elements.push(link);
    }

    return elements;
}

onReady();
