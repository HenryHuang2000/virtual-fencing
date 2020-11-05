window.onload = init;

function init() {
    displayHttpGet("/api/records");
    document.getElementById('filterForm').addEventListener('submit', (event) => {
        event.preventDefault();
        applyFilters(document.getElementById('filterForm').elements);
    });
    setupColumnSorting();
}

function setupColumnSorting() {
    const getCellValue = (tr, idx) => tr.children[idx].dataset.value;

    const comparerFactory = (idx, asc) => (a, b) => {
        const v1 = getCellValue(asc ? a : b, idx);
        const v2 = getCellValue(asc ? b : a, idx);

        const bothNumbers = v1 !== '' && v2 !== '' && !isNaN(v1) && !isNaN(v2);
        return bothNumbers ? v1 - v2 : v1.toString().localeCompare(v2);
    };

    document.querySelectorAll('th').forEach((th, tableHeaderIndex) => th.addEventListener('click', (() => {

        // Reset all sorting arrows on other columns.
        document.querySelectorAll('th').forEach(th => th.removeAttribute('data-sortDirection'));
        // Set the sort direction for the clicked table header.
        this.asc ? th.setAttribute('data-sortDirection', 'asc') : th.setAttribute('data-sortDirection', 'desc');
        const table = th.closest('table');
        const tbody = table.querySelector('tbody');
        Array.from(tbody.querySelectorAll('tr'))
            .sort(comparerFactory(tableHeaderIndex, this.asc = !this.asc))
            .forEach(tr => tbody.appendChild(tr) );
    })));
}

function applyFilters(filters) {
    let queries = [];
    if (filters.name.value.length > 0) {
        queries.push("name=" + filters.name.value);
    }
    if (filters.userId.value.length > 0) {
        queries.push("userId=" + filters.userId.value);
    }
    if (filters.location.value.length > 0) {
        queries.push("location=" + filters.location.value);
    }
    if (filters.timestampFirst.value.length > 0) {
        const date = new Date(filters.timestampFirst.value);
        queries.push("timestampFirst=" + date.getTime());
    }
    if (filters.timestampLast.value.length > 0) {
        const date = new Date(filters.timestampLast.value);
        queries.push("timestampLast=" + date.getTime());
    }

    let url = "/api/records";
    if (queries.length > 0) {
        url += "?" + queries.join('&');
    }
    displayHttpGet(url);
}

function displayHttpGet(url) {

    // Reset all sorting arrows on other columns.
    document.querySelectorAll('th').forEach(th => th.removeAttribute('data-sortDirection'));
    const table = document.getElementById("recordsTable");
    const newBody = document.createElement("tbody");
    const oldBody = table.getElementsByTagName('tbody')[0];

    fetch(url)
        .then(response => response.json())
        .then(records => {
            for (const record of records) {

                row = newBody.insertRow();

                // Name cell.
                row.insertCell(0).appendChild(document.createTextNode(record.name));
                row.children[0].dataset.value = record.name;

                // Id cell.
                row.insertCell(1).appendChild(document.createTextNode(record.userId));
                row.children[1].dataset.value = record.userId;

                // Location cell.
                row.insertCell(2).appendChild(document.createTextNode(record.location));
                row.children[2].dataset.value = record.location;

                // Timestamp cell.
                row.insertCell(3).appendChild(document.createTextNode(record.timestamp));
                row.children[3].dataset.value = Date.parse(record.timestamp);
            }

        });

    table.replaceChild(newBody, oldBody);
}